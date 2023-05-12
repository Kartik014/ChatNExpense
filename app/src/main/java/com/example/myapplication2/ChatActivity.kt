package com.example.myapplication2

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
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
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#301E67")))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageBox = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sentButton)
        splitButton = findViewById(R.id.splitButton)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)
        user1_info = findViewById(R.id.user_info1)
        user2_info = findViewById(R.id.user_info2)
        user1_amount = findViewById(R.id.bill_info1)
        user2_amount = findViewById(R.id.bill_info2)

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

            val message = messageBox.text.toString()
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
            messageBox.setText("")
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Request_Code && resultCode == Activity.RESULT_OK) {
            TextMessage = data?.getStringExtra("message")
            amount = data?.getStringExtra("amount")?.toDouble()!!
            Toast.makeText(this@ChatActivity, "$amount", Toast.LENGTH_SHORT).show()
            temp_amount += amount / 2
            user2_amount.setText("${(temp_amount)}")
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
                        //Error Message
                    }
                })
            return true
        } else if (item.itemId == R.id.split) {

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