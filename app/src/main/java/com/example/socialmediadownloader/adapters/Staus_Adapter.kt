package com.example.socialmediadownloader.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socialmediadownloader.R
import com.example.socialmediadownloader.activities.whatsapp_preview
import com.example.socialmediadownloader.utils.functions
import kotlinx.android.synthetic.main.status_item.view.*
import java.io.File
import java.util.*

open class Staus_Adapter(val context: Activity, val files : ArrayList<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder> () {


    override fun getItemCount(): Int {
        return files.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentStatus = files[position]

        if(holder is MyViewHolder){


            Glide.with(context).asBitmap().placeholder(R.drawable.black).load(currentStatus).into(holder.itemView.status_image)

            if(currentStatus.endsWith(".mp4")){
                holder.itemView.status_play.visibility = View.VISIBLE
            }else{
                holder.itemView.status_play.visibility = View.GONE
            }

            holder.itemView.staus_item.setOnClickListener {
                val intent = Intent(context,whatsapp_preview::class.java)
                val isVideo : Boolean = currentStatus.endsWith(".mp4")
                val path = currentStatus
                val file = File(currentStatus)
                val file_length = file.length()
                val size = functions().bytesIntoHumanReadable(file_length)
                intent.putExtra("path",path)
                intent.putExtra("isVideo",isVideo)
                intent.putExtra("size", size)
                intent.putExtra("name",file.name)
                context.startActivity(intent)
            }

            holder.itemView.setOnClickListener {
                val intent = Intent(context,whatsapp_preview::class.java)
                val isVideo : Boolean = currentStatus.endsWith(".mp4")
                val path = currentStatus
                val file = File(currentStatus)
                val file_length = file.length()
                val size = functions().bytesIntoHumanReadable(file_length)
                intent.putExtra("path",path)
                intent.putExtra("isVideo",isVideo)
                intent.putExtra("size", size)
                intent.putExtra("name",file.name)
                context.startActivity(intent)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.status_item,
                parent,
                false
            )
        )
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}