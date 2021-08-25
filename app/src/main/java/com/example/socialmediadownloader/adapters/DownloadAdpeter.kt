package com.example.socialmediadownloader.adapters

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.socialmediadownloader.R
import com.example.socialmediadownloader.model.DownloadModel
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.download_item.view.*
import java.util.*

open class DownloadAdpeter(private val context: Context, private var list: ArrayList<DownloadModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var onClickListener : OnClickListener? = null

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder){
            holder.itemView.download_title.setText(model.title)
            holder.itemView.platform.setImageResource(model.platfrom)
            holder.itemView.total_size.setText(model.total_size)
            holder.itemView.download_date.setText(model.date)

            holder.itemView.setOnClickListener{
                if(onClickListener != null){
                    onClickListener!!.onClick(position,model)
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.download_item,
                parent,
                false
            )
        )
    }

    interface OnClickListener {
        fun onClick(position: Int, model: DownloadModel)
    }

    fun setOnClickLIstener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }


    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

}