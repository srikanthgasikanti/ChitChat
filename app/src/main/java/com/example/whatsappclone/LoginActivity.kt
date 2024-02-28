package com.example.whatsappclone

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.whatsappclone.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth

const val PHONE_NUMBER="phoneNumber"
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth=FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnVerify.isEnabled=false
        binding.countryCode.registerCarrierNumberEditText(binding.phone)
        binding.phone.addTextChangedListener {
            val enableButton=!((it.isNullOrEmpty() || (it.length < 11)))
            if(enableButton){
                binding.btnVerify.isEnabled=true
                val imm=getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.phone.windowToken,0)
            }else{
                binding.btnVerify.isEnabled=false
            }
        }


        binding.btnVerify.setOnClickListener {
            val fullNumber=binding.countryCode.fullNumberWithPlus
            val dialog=AlertDialog.Builder(this)
            dialog.setPositiveButton("OK"){ alert, _->
                alert.dismiss()
                val intent=Intent(this,OtpActivity::class.java)
                intent.putExtra(PHONE_NUMBER,fullNumber)
                startActivity(intent)
            }
            dialog.setNegativeButton("Edit"){ alert, _->
                alert.dismiss()
            }
            dialog.setMessage("We will be verifying the phone number:$fullNumber.\nIs this OK,or would you like to edit the number?")
            dialog.show()
        }
    }

    override fun onStart() {
        super.onStart()
        if(auth.currentUser!=null){
            startActivity(Intent(this,SignUpActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }
    }
}