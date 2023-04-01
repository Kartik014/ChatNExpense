package com.example.myapplication2

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.startup.User

class Addadapter(val context: Context, val userSelected: ArrayList<User>,val adduser: ArrayList<String>): RecyclerView.Adapter<Addadapter.UserViewHolder>() {

    private var selectedPositions = BooleanArray(userSelected.size) {false}
    private lateinit var mListener: onItemClickListener
    interface onItemClickListener{

        fun onItemClick(position: Int){

        }
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener=listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {

        val view: View = LayoutInflater.from(context).inflate(R.layout.activity_select_user, parent, false)
        return UserViewHolder(view,mListener)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser= userSelected[position]
        holder.txtName.text= currentUser.name

        if (selectedPositions.size != userSelected.size) {
            selectedPositions = BooleanArray(userSelected.size) {false}
        }

        if(position<=selectedPositions.size) {
            holder.linerlayout.setOnClickListener {
                selectedPositions[position] = !selectedPositions[position]
                notifyDataSetChanged()
            }

            if (selectedPositions[position]) {
                holder.linerlayout.setBackgroundColor(Color.parseColor("#1776FF"))
                holder.txtName.setTextColor(Color.parseColor("#ffffff"))
                if(!adduser.contains(currentUser.uid)){
                    adduser.add(currentUser.uid!!)
                }
            } else {
                holder.linerlayout.setBackgroundColor(Color.parseColor("#000000"))
                holder.txtName.setTextColor(Color.parseColor("#ffffff"))
                adduser.remove(currentUser.uid)
            }
        }
    }

    override fun getItemCount(): Int {
        size= userSelected.size
        return userSelected.size
    }

    class UserViewHolder(itemView: View,listener: onItemClickListener): RecyclerView.ViewHolder(itemView){
        val txtName= itemView.findViewById<TextView>(R.id.txtname)
        val linerlayout = itemView.findViewById<View>(R.id.LinearLayout) as RelativeLayout

        init{
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }
    companion object{
        var size:Int = 0
    }
}