package com.example.bt

import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication00.R

class AdapterExample(private val exampleList: MutableList<ItemExample>, private val listener: OnItemClickListener): RecyclerView.Adapter<AdapterExample.ViewHolderExample>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderExample {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.example_item, parent, false)
        return ViewHolderExample(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolderExample, position: Int) {
        val currentItem = exampleList[position]

        holder.textView1.text = currentItem.firstLine
        holder.textView2.text = currentItem.secondLine
    }

    override fun getItemCount() = exampleList.size

    inner class ViewHolderExample(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val imageView: ImageView = itemView.findViewById(R.id.bluetoothIcon)
        val textView1: TextView = itemView.findViewById(R.id.firstLine)
        val textView2: TextView = itemView.findViewById(R.id.secondLine)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            listener.onItemClick(position)

        }
    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }
}