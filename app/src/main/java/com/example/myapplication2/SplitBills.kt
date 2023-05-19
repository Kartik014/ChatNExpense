package com.example.myapplication2

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication2.databinding.ActivitySplitBillsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplitBills : AppCompatActivity() {

    private lateinit var binding: ActivitySplitBillsBinding
    private lateinit var dbRef: DatabaseReference
    private var senderUid: String? = null
    private var name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplitBillsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbRef = FirebaseDatabase.getInstance().getReference()
        senderUid = FirebaseAuth.getInstance().currentUser?.uid

        dbRef.child("user").child(senderUid!!).child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val title = dataSnapshot.getValue(String::class.java)
                    if (title != null) {
                        name = title
                        //Log.d(TAG, "fcmToken: $fcmToken")
                    } else {
                        Log.e(ContentValues.TAG, "Title is null")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(ContentValues.TAG, "Failed to get Title: ${databaseError.message}")
                }
            })

        binding.btnSave.setOnClickListener {
            val amount = binding.edamount.text.toString()
            val reason = binding.edreason.text.toString()
            val message = generateText(amount, reason)
            val intent = Intent()
            intent.putExtra("message", message)
            intent.putExtra("amount", amount)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun generateText(amount: String, reason: String): String {
        val message = "${name.toString()} spent $amount for $reason."
        return message
    }

}
