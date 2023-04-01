package com.example.startup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication2.MainActivity
import com.example.myapplication2.R
import com.google.firebase.auth.FirebaseAuth

class LogIn : AppCompatActivity() {

    private lateinit var btLogIn: Button
    private lateinit var signup: TextView
    private lateinit var etEmail: EditText
    private lateinit var edPassword: EditText
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        mAuth= FirebaseAuth.getInstance()

        edPassword=findViewById(R.id.edPassword)
        btLogIn=findViewById(R.id.btLogIn)
        etEmail=findViewById(R.id.edEmail)
        signup=findViewById(R.id.signup)

        signup.setOnClickListener{
            val intent= Intent(this,SignUp::class.java)
            startActivity(intent)
        }

        btLogIn.setOnClickListener {
            val email=etEmail.text.toString()
            val password=edPassword.text.toString()

            login(email,password)
        }
    }

    override fun onStart() {
        super.onStart()
        if(mAuth.currentUser!=null){
            val intent= Intent(this@LogIn,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun login(email: String, password: String){
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){task->
                if(task.isSuccessful){
                    val intent=Intent(this@LogIn, MainActivity::class.java)
                    startActivity(intent)
                }
                else{
                    Toast.makeText(this@LogIn,"User does not exist",Toast.LENGTH_SHORT).show()
                }
            }
    }
}