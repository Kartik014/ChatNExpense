package com.example.myapplication2

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R.id.action_call
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var user1_info: TextView
    private lateinit var user1_amount: TextView
    private lateinit var user2_info: TextView
    private lateinit var user2_amount: TextView
    private lateinit var sendButton: ImageView
    private lateinit var splitButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var dbRef: DatabaseReference
    private lateinit var layout1: LinearLayout
    private lateinit var layout2: Button
    private lateinit var btnSettle: Button
    private var receiveruid: String? = null
    private var senderuid: String? = null
    private var fcmtoken: String? = ""
    private var senderTitle: String? = ""

    val Request_Code = 1
    var receiverRoom: String? = null
    var senderRoom: String? = null
    var TextMessage: String? = null
    var amount: Double = 0.0
    var temp_amount: Double = 0.0
    var flag: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        dbRef = FirebaseDatabase.getInstance().getReference()

        val name = intent.getStringExtra("name")
        receiveruid = intent.getStringExtra("uid")
        senderuid = FirebaseAuth.getInstance().currentUser?.uid

        senderRoom = receiveruid + senderuid
        receiverRoom = senderuid + receiveruid

        supportActionBar?.title = name
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#BE00EEEE")))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageBox = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sentButton)
        splitButton = findViewById(R.id.splitButton)
        user1_info = findViewById(R.id.user_info1)
        user2_info = findViewById(R.id.user_info2)
        user1_amount = findViewById(R.id.bill_info1)
        user2_amount = findViewById(R.id.bill_info2)
        layout1 = findViewById(R.id.bills_info)
        layout2 = findViewById(R.id.settle)
        btnSettle = findViewById(R.id.settle)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)

        user1_info.setText("You owe an amount of -")
        user2_info.setText(name + " owe an amount of -")

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        dbRef.child("user").child(receiveruid!!).child("fcmToken")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val token = dataSnapshot.getValue(String::class.java)
                    if (token != null) {
                        fcmtoken = token
                        //Log.d(TAG, "fcmToken: $fcmToken")
                    } else {
                        Log.e(TAG, "FCM token is null")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "Failed to get FCM token: ${databaseError.message}")
                }
            })

        dbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    messageList.clear()
                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        splitButton.setOnClickListener {
            Toast.makeText(this@ChatActivity, "Clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@ChatActivity, SplitBills::class.java)
            startActivityForResult(intent, Request_Code)
        }

        sendButton.setOnClickListener {

            var message = messageBox.text.toString()
            if (message != "") {
                dbRef.child("user").child(senderuid!!).child("name")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val title = dataSnapshot.getValue(String::class.java)
                            if (title != null) {
                                senderTitle = title
                                //Log.d(TAG, "fcmToken: $fcmToken")
                            } else {
                                Log.e(TAG, "Title is null")
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e(TAG, "Failed to get Title: ${databaseError.message}")
                        }
                    })

                val messageObject = Message(message, senderuid)

                dbRef.child("chats").child(senderRoom!!).child("messages").push()
                    .setValue(messageObject).addOnSuccessListener {
                        dbRef.child("chats").child(receiverRoom!!).child("messages").push()
                            .setValue(messageObject)
                    }

                PushNotification(
                    NotificationData(senderTitle.toString(), message),
                    fcmtoken.toString()
                ).also {
                    SendNotification(it)
                }
            }
            messageBox.setText("")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var tempAmount: String? = null

        if (requestCode == Request_Code && resultCode == Activity.RESULT_OK) {
            TextMessage = data?.getStringExtra("message")
            amount = data?.getStringExtra("amount")?.toDouble()!!
            temp_amount = amount / 2
            val sharedPreferences = getSharedPreferences("Amount", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            val oldAmount = sharedPreferences.getFloat("temp_amount", 0f)
            val newAmount = oldAmount + temp_amount.toFloat()
            editor.putFloat("temp_amount", newAmount)
            editor.apply()
            tempAmount = sharedPreferences.getFloat("temp_amount", 0f).toString()
        }

        val BillObject = Bills(tempAmount, senderuid)

        dbRef.child("amounts").child(senderRoom!!).child("bills").push()
            .setValue(BillObject).addOnSuccessListener {
                dbRef.child("amounts").child(receiverRoom!!).child("bills").push()
                    .setValue(BillObject)
            }

        dbRef.child("amounts").child(senderRoom!!).child("bills")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var senderBillAmount: String? = null
                    var receiverBillAmount: String? = null
                    for (billSnapshot in dataSnapshot.children) {
                        val billsData = billSnapshot.getValue(Bills::class.java)
                        if (billsData != null) {
                            val billAmount = billsData.amount
                            val senderId = billsData.senderId

                            if (senderuid == senderId) {
                                senderBillAmount = billAmount
                                user2_amount.text = billAmount
                            } else if (receiveruid == senderId) {
                                receiverBillAmount = billAmount
                                user1_amount.text = billAmount
                            }
                        }
                    }

                    if (senderBillAmount != null) {
                        user2_amount.text = senderBillAmount
                    }

                    if (receiverBillAmount != null) {
                        user1_amount.text = receiverBillAmount
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("TAG", "${error.message}")
                }
            })

        btnSettle.setOnClickListener {
            val sharedPreferences = getSharedPreferences("Amount", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putFloat("temp_amount", 0f)
            editor.apply()

            dbRef.child("amounts").child(senderRoom!!).child("bills")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (billSnapshot in dataSnapshot.children) {
                            val billsData = billSnapshot.getValue(Bills::class.java)
                            if (billsData != null && billsData.senderId == senderuid) {
                                billSnapshot.ref.removeValue()
                                val receiverBillsRef =
                                    dbRef.child("amounts").child(receiverRoom!!).child("bills")
                                receiverBillsRef.orderByChild("senderId").equalTo(senderuid)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(receiverSnapshot: DataSnapshot) {
                                            for (receiverBillSnapshot in receiverSnapshot.children) {
                                                receiverBillSnapshot.ref.removeValue()
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Log.d(
                                                "TAG",
                                                "Error removing receiver bill data: ${error.message}"
                                            )
                                        }
                                    })
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("TAG", "Error removing sender bill data: ${error.message}")
                    }
                })
        }

        if (TextMessage != null) {
            val message = TextMessage
            val messageObject = Message(message, senderuid)

            dbRef.child("chats").child(senderRoom!!).child("messages").push()
                .setValue(messageObject).addOnSuccessListener {
                    dbRef.child("chats").child(receiverRoom!!).child("messages").push()
                        .setValue(messageObject)
                }

            PushNotification(
                NotificationData(senderTitle.toString(), message),
                fcmtoken.toString()
            ).also {
                SendNotification(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_chat, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        } else if (item.itemId == action_call) {
            dbRef.child("user").child(receiveruid!!).child("phoneNo")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val phoneNumber = dataSnapshot.getValue(String::class.java)
                        if (phoneNumber != null) {
                            val callIntent = Intent(Intent.ACTION_CALL)
                            callIntent.data = Uri.parse("tel:$phoneNumber")
                            startActivity(callIntent)
                        } else {
                            Log.e(TAG, "Phone number is null")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
            return true
        } else if (item.itemId == R.id.split) {
            if (flag) {
                layout1.visibility = View.VISIBLE
                layout2.visibility = View.VISIBLE
                flag = false
            } else {
                layout1.visibility = View.GONE
                layout2.visibility = View.GONE
                flag = true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun SendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiUtilities.api.sendNotification(notification)
                if (response.isSuccessful) {
//                Log.d(TAG, "Response: ${Gson().toJson(response)}")
                } else {
                    val errorMessage = response.errorBody()?.string()
                    Log.e(TAG, "Error message: $errorMessage")
                    Log.e(TAG, "Response code: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
}