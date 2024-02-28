package com.example.whatsappclone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.whatsappclone.databinding.ActivitySignUpBinding
import com.example.whatsappclone.model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySignUpBinding
    private var currentImage:Uri?=null
    private lateinit var profileImageUrl:String
    private var editProfile=false
    private  var currentUser:User?=null
    private var phoneNumber:Long=100

    private val storage by lazy {
        FirebaseStorage.getInstance()
    }
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }
    private val currentUid by lazy {
        FirebaseAuth.getInstance().uid
    }
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Handle the returned Uri
        if(uri==null){
            if(currentImage==null) {
                if(editProfile){
                    binding.btnNext.isEnabled=true
                    Glide.with(this).load(profileImageUrl).placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar).into(binding.profilePhoto)
                }else{
                    binding.profilePhoto.setImageResource(R.drawable.default_avatar)
                }
            }
            else{
                binding.profilePhoto.setImageURI(currentImage)
            }
        }else{
            currentImage=uri
            binding.profilePhoto.setImageURI(uri)
            uploadImage(uri)
        }
    }

    private fun uploadImage(uri: Uri) {
        val ref=storage.reference.child("profiles/"+auth.uid.toString())
        val uploadTask=ref.putFile(uri)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task->
                if(!task.isSuccessful){
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            }).addOnCompleteListener {
                binding.btnNext.isEnabled=true
            if(it.isSuccessful){
                profileImageUrl=it.result.toString()
            }
        }.addOnFailureListener {
            Toast.makeText(this,it.message,Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        editProfile=intent.getBooleanExtra(EDIT,false)
        if(editProfile){
            initialise()
        }
        phoneNumber=intent.getStringExtra(PHONE_NUMBER)!!.toLong()
        setOnClickListener()
    }
    private fun initialise(){
        profileImageUrl=intent.getStringExtra(URL)!!
        binding.etProfileName.setText(intent.getStringExtra(NAME))
        binding.etStatus.setText(intent.getStringExtra(STATUS))
        binding.btnNext.text= getString(R.string.save)
        binding.btnNext.isEnabled=true
        phoneNumber=intent.getStringExtra(PHONE_NUMBER)!!.toLong()
        binding.tvNote.visibility= View.GONE
        Glide.with(this).load(profileImageUrl).placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar).into(binding.profilePhoto)
    }

    private fun setOnClickListener() {
        binding.profilePhoto.setOnClickListener {
            checkPermissions()
        }
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnNext.setOnClickListener {
            val name=binding.etProfileName.text.trim().toString()
            val status=binding.etStatus.text.trim().toString()
            if(name.isEmpty()){
                Toast.makeText(this,"Please Provide Name",Toast.LENGTH_LONG).show()
            }
            else if(!::profileImageUrl.isInitialized){
                Toast.makeText(this,"Please Select Profile Photo",Toast.LENGTH_LONG).show()
            }else{
                registerUser(name,status)
            }
        }
    }

    private fun registerUser(name:String,status:String?) {
        var user= User(name,profileImageUrl,profileImageUrl,auth.uid!!,phoneNumber)
        if(status!=null){
            user=User(name,profileImageUrl,profileImageUrl,auth.uid!!,status,phoneNumber)
        }
        firestore.collection("users").document(auth.uid!!).set(user).addOnCompleteListener {
            startActivity(Intent(this,MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }.addOnFailureListener {
            Toast.makeText(this,it.message,Toast.LENGTH_LONG).show()
        }
    }

    private val permissionsLauncher=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ map->
        var permissionGranted=true
        for(permission in map){
            if(!permission.value){
                permissionGranted=false
            }
        }
        if(permissionGranted){
            openGallery()
        }else{
            Toast.makeText(this,"Please Provide Permissions",Toast.LENGTH_LONG).show()
        }
    }
    private fun checkPermissions() {
        val permissions=if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_VIDEO)
        }else{
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(!hasPermissions(permissions[0])){
            permissionsLauncher.launch(permissions)
        }else{
            openGallery()
        }
    }

    private fun hasPermissions(permission: String):Boolean{
        return ContextCompat.checkSelfPermission(this,permission)==PackageManager.PERMISSION_GRANTED
    }

    private fun openGallery() {
        binding.btnNext.isEnabled=false
        getContent.launch("image/*")
    }
}