package com.example.myapplication2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.databinding.ActivityGroupPageBinding
import com.example.startup.LogIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson


class GroupPage : AppCompatActivity() {

    private lateinit var binding: ActivityGroupPageBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var adapter: GroupAdapter
    private lateinit var dbRef: DatabaseReference
    private lateinit var GroupRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_page)
        binding= ActivityGroupPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth= FirebaseAuth.getInstance()
        dbRef= FirebaseDatabase.getInstance().getReference()

        val sharedPreferences: SharedPreferences = getSharedPreferences("arrayListString", Context.MODE_PRIVATE)
        var arrayListString: String? = sharedPreferences.getString("arrayListString",null)
        var groups: ArrayList<String> = if (arrayListString != null){
            Gson().fromJson(arrayListString, ArrayList::class.java) as ArrayList<String>
        }
        else{
            ArrayList()
        }

        adapter= GroupAdapter(this, groups)

        GroupRecyclerView=findViewById(R.id.GroupRecyclerView)
        GroupRecyclerView.layoutManager=LinearLayoutManager(this)
        GroupRecyclerView.adapter=adapter

        val mintent = Intent(this, MainActivity::class.java)
        adapter.setOnItemClickListener(object : GroupAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                mintent.putExtra("MESSAGES_CHILD",groups[position])
                startActivity(mintent)
            }
        })

        binding.UserDisplay.setOnClickListener {
            val intent= Intent(this@GroupPage,MainActivity::class.java)
            startActivity(intent)
        }

        binding.AddBtn.setOnClickListener {
            val intent= Intent(this@GroupPage,selectingUser::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.logout){
            mAuth.signOut()
            val intent= Intent(this@GroupPage, LogIn::class.java)
            finish()
            startActivity(intent)
            return true
        }
        return true
    }
}