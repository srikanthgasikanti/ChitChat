package com.example.whatsappclone

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.whatsappclone.R.string.verify
import com.example.whatsappclone.databinding.ActivityOtpBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.util.concurrent.TimeUnit

const val TAG="Srikanth"
class OtpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpBinding
    private var phoneNumber: String? = null

    private val auth=FirebaseAuth.getInstance()
    private val fireStore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val currentUid by lazy {
        FirebaseAuth.getInstance().uid
    }
    private var fVerificationId:String?=null
    private var fResendToken:PhoneAuthProvider.ForceResendingToken?=null
    private lateinit var fCallback:PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private val documentReference by lazy {
        fireStore.collection("users").document(currentUid.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialise()
        verifyUser(false)
        setOnClickListener()
    }

    private fun verifyUser(isResend:Boolean) {
        binding.progressBar.visibility=View.VISIBLE
        showTimer()
        val authOptions=PhoneAuthOptions.newBuilder()
            .setActivity(this)
            .setCallbacks(fCallback)
            .setPhoneNumber(phoneNumber!!)
            .setTimeout(60,TimeUnit.SECONDS)
        if(isResend){
            PhoneAuthProvider.verifyPhoneNumber(authOptions.setForceResendingToken(fResendToken!!).build())
        }else{
            PhoneAuthProvider.verifyPhoneNumber(authOptions.build())
        }
    }

    private fun setOnClickListener() {
        binding.btnResendOtp.setOnClickListener {
            binding.etOTP.setText("")
            verifyUser(true)
        }
        binding.btnSendOtp.setOnClickListener {
            binding.progressBar.visibility=View.VISIBLE
            val enteredOtp=binding.etOTP.text.trim().toString()
            val credential = PhoneAuthProvider.getCredential(fVerificationId!!,enteredOtp )
            signInWithPhoneAuthCredential(credential)
        }
    }

    private fun showTimer() {
        binding.btnResendOtp.isEnabled = false
        binding.tvTimer.visibility=View.VISIBLE
        object :CountDownTimer(60000,1000){
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimer.text= getString(R.string.seconds_remaining,(millisUntilFinished/1000).toString())
            }

            override fun onFinish() {
                binding.btnResendOtp.isEnabled=true
                binding.tvTimer.visibility=View.INVISIBLE
            }
        }.start()
    }

    private fun initialise() {
        phoneNumber = intent.getStringExtra(PHONE_NUMBER)
        binding.tvVerify.text = getString(verify, phoneNumber)
        setSpannableString()
        fCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                binding.etOTP.setText(credential.smsCode)
                binding.progressBar.visibility=View.INVISIBLE
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        // Invalid request
                        Toast.makeText(this@OtpActivity, e.message, Toast.LENGTH_LONG).show()
                        Log.d(TAG,e.message.toString())
                        binding.progressBar.visibility=View.INVISIBLE
                    }

                    is FirebaseTooManyRequestsException -> {
                        binding.progressBar.visibility=View.INVISIBLE
                        // The SMS quota for the project has been exceeded
                        Toast.makeText(this@OtpActivity, e.message, Toast.LENGTH_LONG).show()
                        Log.d(TAG,e.message.toString())
                    }

                    is FirebaseAuthMissingActivityForRecaptchaException -> {
                        // reCAPTCHA verification attempted with null Activity
                        binding.progressBar.visibility=View.INVISIBLE
                        Toast.makeText(this@OtpActivity, e.message, Toast.LENGTH_LONG).show()
                        Log.d(TAG,e.message.toString())
                    }

                    else -> {
                        // Show a message and update the UI
                        binding.progressBar.visibility=View.INVISIBLE
                        Toast.makeText(this@OtpActivity, e.message, Toast.LENGTH_LONG).show()
                        Log.d(TAG,e.message.toString())
                    }
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")
                // Save verification ID and resending token so we can use them later
                binding.progressBar.visibility=View.INVISIBLE
                fVerificationId = verificationId
                fResendToken = token
            }
        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener {task->
                if(task.isSuccessful){
                    binding.progressBar.visibility=View.INVISIBLE
                    Toast.makeText(this,"Sign In Successful",Toast.LENGTH_LONG).show()
                    saveDeviceToken()
                    startActivity(Intent(this,SignUpActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK).putExtra(
                        PHONE_NUMBER,phoneNumber!!.replace("+91","")))
                }else{
                    binding.progressBar.visibility=View.INVISIBLE
                    if(task.exception is FirebaseAuthInvalidCredentialsException){
                        Toast.makeText(this,"Entered OTP Is Invalid",Toast.LENGTH_LONG).show()
                    }else{
                        binding.progressBar.visibility=View.GONE
                        Toast.makeText(this,task.exception?.message.toString(),Toast.LENGTH_LONG).show()
                    }
                }
            }
    }
    private fun saveDeviceToken(){
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                // Get new FCM registration token
                val token = task.result
                documentReference.update(DEVICE_TOKEN,token)
            })
    }

    private fun setSpannableString() {
        val span = SpannableString(getString(R.string.waiting_tv, phoneNumber))
        val clickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color =getColor(R.color.pink)
            }

            override fun onClick(widget: View) {
                editNumber()
            }
        }
        span.setSpan(clickableSpan, span.length - 13, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvWaiting.movementMethod=LinkMovementMethod.getInstance()
        binding.tvWaiting.text = span
    }

    private fun editNumber() {
        finish()
    }
}