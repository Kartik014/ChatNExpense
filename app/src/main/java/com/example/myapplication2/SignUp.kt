package com.example.startup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication2.MainActivity
import com.example.myapplication2.databinding.ActivitySignUpBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Date

class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedImage: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        storage= FirebaseStorage.getInstance()

        binding.userImg.setOnClickListener {
            val intent= Intent()
            intent.action= Intent.ACTION_GET_CONTENT
            intent.type="image/*"
            startActivityForResult(intent,1)
        }

        binding.btSignUp.setOnClickListener {
            val name = binding.edname.text.toString()
            val email = binding.edemail.text.toString()
            val PhoneNo= binding.phoneNo.text.toString()
            val password = binding.edpassword.text.toString()
            var imageUrl: String=""
            val reference= storage.reference.child("Profile").child(Date().time.toString())

            GlobalScope.launch(Dispatchers.IO) {
                reference.putFile(selectedImage).addOnCompleteListener{
                    if(it.isSuccessful){
                        reference.downloadUrl.addOnSuccessListener{task->
                            imageUrl= task.toString()
                            signup(name, email, PhoneNo, password, imageUrl)
                        }
                    }
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(data!=null){
            if(data.data!=null){
                selectedImage=data.data!!
                binding.userImg.setImageURI(selectedImage)
            }
        }
    }

    private fun signup(name: String, email: String, PhoneNo:String, password: String, imageUrl: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            return@OnCompleteListener
                        }
                        val fcmToken = task.result
                        addUserToDatabase(name, email, mAuth.currentUser?.uid!!, PhoneNo,"", imageUrl, fcmToken)

                    })

                    val intent = Intent(this@SignUp, MainActivity::class.java)
                    finish()
                    startActivity(intent)
                } else {
                    val errorMessage = task.exception?.message
                    Toast.makeText(
                        this@SignUp,
                        "Authenticatication Failed:$errorMessage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun addUserToDatabase(
        name: String,
        email: String,
        uid: String,
        PhoneNo: String,
        groupname: String,
        imageUrl: String,
        fcmToken: String
    ) {
        dbRef = FirebaseDatabase.getInstance().getReference()
        dbRef.child("user").child(uid).setValue(User(name, email, uid, PhoneNo, groupname, imageUrl,fcmToken))
    }
}