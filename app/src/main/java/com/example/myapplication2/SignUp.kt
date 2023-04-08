package com.example.startup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication2.MainActivity
import com.example.myapplication2.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class SignUp : AppCompatActivity() {

    private lateinit var edname: EditText
    private lateinit var edemail: EditText
    private lateinit var edpassword: EditText
    private lateinit var btSignUp: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mAuth = FirebaseAuth.getInstance()

        edname = findViewById(R.id.edname)
        edemail = findViewById(R.id.edemail)
        edpassword = findViewById(R.id.edpassword)
        btSignUp = findViewById(R.id.btSignUp)

        btSignUp.setOnClickListener {
            val name = edname.text.toString()
            val email = edemail.text.toString()
            val password = edpassword.text.toString()

            signup(name, email, password)
        }

    }

    private fun signup(name: String, email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            return@OnCompleteListener
                        }
                        val fcmToken = task.result

                        addUserToDatabase(name, email, mAuth.currentUser?.uid!!, "", fcmToken)

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
        groupname: String,
        fcmToken: String
    ) {
        dbRef = FirebaseDatabase.getInstance().getReference()
        dbRef.child("user").child(uid).setValue(User(name, email, uid, groupname, fcmToken))
    }
}