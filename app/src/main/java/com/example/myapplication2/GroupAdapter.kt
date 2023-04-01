package com.example.myapplication2

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroupAdapter(val context: Context,val groups: ArrayList<String>):RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int){

        }
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view: View= LayoutInflater.from(context).inflate(R.layout.activity_select_user, parent, false)
        return GroupViewHolder(view,mListener)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val currentGroup= groups[position]
            holder.txtName.text= currentGroup
            holder.itemView.setOnClickListener {
                val intent= Intent(context, ChatActivity::class.java)

                intent.putExtra("name",currentGroup)

                context.startActivity(intent)
            }
    }

    override fun getItemCount(): Int {
        return groups.size
    }

    class GroupViewHolder(itemView: View, listener: onItemClickListener): RecyclerView.ViewHolder(itemView){
        val txtName= itemView.findViewById<TextView>(R.id.txtname)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }
}