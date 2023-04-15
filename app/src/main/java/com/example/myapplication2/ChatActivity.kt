package com.example.myapplication2

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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var sendButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var dbRef: DatabaseReference
    private var receiveruid: String? = null
    private var senderuid: String? = null
    private var fcmtoken: String? = ""
    private var senderTitle: String? = ""

    var receiverRoom: String? = null
    var senderRoom: String? = null

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
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_chat, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_call -> {
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
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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