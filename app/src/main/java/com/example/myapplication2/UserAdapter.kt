package com.example.myapplication2;

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.startup.User

class UserAdapter(val context: Context, var userList: ArrayList<User>): RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    fun setFilteredList(userList: ArrayList<User>){
        this.userList= userList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View= LayoutInflater.from(context).inflate(R.layout.user_layout, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser= userList[position]
        holder.txtName.text= currentUser.name
        Glide.with(context).load(currentUser.imageUrl).into(holder.image)

        holder.itemView.setOnClickListener {
            val intent= Intent(context, ChatActivity::class.java)

            intent.putExtra("name",currentUser.name)
            intent.putExtra("uid", currentUser.uid)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserViewHolder(itemView:View): RecyclerView.ViewHolder(itemView){
        val txtName= itemView.findViewById<TextView>(R.id.txt_name)
        val image= itemView.findViewById<ImageView>(R.id.userImage)
    }
}
