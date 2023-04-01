package com.example.myapplication2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication2.databinding.ActivityGroupCreationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson


class GroupCreation : AppCompatActivity() {

    private lateinit var binding: ActivityGroupCreationBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var GroupName: String
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityGroupCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth= FirebaseAuth.getInstance()
        val userSelected = intent.getSerializableExtra("userSelected") as? ArrayList<String> ?: ArrayList()
        val currentUserUid: String=mAuth.currentUser?.uid.toString()
        userSelected.add(currentUserUid)

        val sharedPreferences: SharedPreferences = getSharedPreferences("arrayListString", Context.MODE_PRIVATE)
        var prefs: SharedPreferences.Editor = sharedPreferences.edit()
        var arrayListString: String? = sharedPreferences.getString("arrayListString",null)
        var groups = if(arrayListString != null){
            Gson().fromJson(arrayListString, ArrayList::class.java) as ArrayList<String>
        }
        else{
            ArrayList()
        }

        binding.btnCreate.setOnClickListener {
            GroupName= binding.nameGroup.text.toString().trim()

            dbRef= FirebaseDatabase.getInstance().getReference()
            if(GroupName.isNotEmpty() && userSelected.isNotEmpty() && userSelected.size>1){
                for(user in userSelected){
                    dbRef.child("user").child(user).child("groupname").setValue(GroupName)
                }
                groups.add(GroupName)
                arrayListString = Gson().toJson(groups)
                prefs.putString("arrayListString", arrayListString).apply()
                val intent= Intent(this@GroupCreation,GroupPage::class.java)
                startActivity(intent)
            }
            else{
                Toast.makeText(this, "Please enter a group name and select users",Toast.LENGTH_SHORT).show()
            }
        }
    }
}