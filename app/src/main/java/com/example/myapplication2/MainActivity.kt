package com.example.myapplication2

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.databinding.ActivityMainBinding
import com.example.startup.LogIn
import com.example.startup.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: UserAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.groupDisplay.setOnClickListener {
            val intent= Intent(this@MainActivity,GroupPage::class.java)
            startActivity(intent)
        }

        mAuth= FirebaseAuth.getInstance()
        dbRef= FirebaseDatabase.getInstance().getReference()
        userList=ArrayList()
        adapter= UserAdapter(this,userList)

        userRecyclerView=findViewById(R.id.userRecyclerView)

        userRecyclerView.layoutManager= LinearLayoutManager(this)
        userRecyclerView.adapter=adapter


        dbRef.child("user").addValueEventListener(object : ValueEventListener {
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

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return false
            }

        })

    }

    private fun filterList(query: String?) {
        if(query!=null){
            val filteredList= ArrayList<User>()
            for(i in userList){
                if(i.name?.lowercase(Locale.ROOT)!!.contains(query)){
                    filteredList.add(i)
                }
            }
            if(filteredList.isEmpty()){
                Toast.makeText(this@MainActivity,"No Data found",Toast.LENGTH_SHORT).show()
            }
            else{
                adapter.setFilteredList(filteredList)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.logout){
            mAuth.signOut()
            val intent= Intent(this@MainActivity,LogIn::class.java)
            finish()
            startActivity(intent)
            return true
        }
        return true
    }

}