package com.example.myapplication2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.databinding.ActivitySelectingUserBinding
import com.example.startup.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class selectingUser : AppCompatActivity() {

    private lateinit var binding: ActivitySelectingUserBinding
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var userSelected: ArrayList<String>
    private lateinit var adapter: Addadapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivitySelectingUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth= FirebaseAuth.getInstance()
        mDbRef= FirebaseDatabase.getInstance().getReference()
        userList=ArrayList()
        userSelected=ArrayList()
        adapter= Addadapter(this,userList,userSelected)

        userRecyclerView=findViewById(R.id.UserRecyclerView)

        userRecyclerView.layoutManager= LinearLayoutManager(this)
        userRecyclerView.adapter=adapter


        mDbRef.child("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                userList.clear()
                for(postSnapshot in snapshot.children){
                    val currentUser= postSnapshot.getValue(User::class.java)

                    if(mAuth.currentUser?.uid!=currentUser?.uid){
                        userList.add(currentUser!!)
                    }

                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        adapter.setOnItemClickListener(object : Addadapter.onItemClickListener{
            override fun onItemClick(position: Int) {

            }

        })

        binding.btnNext.setOnClickListener {
            val intent= Intent(this@selectingUser,GroupCreation::class.java)
            intent.putExtra("userSelected",userSelected)
            startActivity(intent)
        }
    }
}