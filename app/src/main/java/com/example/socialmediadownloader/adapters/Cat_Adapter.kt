package com.example.socialmediadownloader.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.socialmediadownloader.R
import com.example.socialmediadownloader.activities.audio_downloader_input
import com.example.socialmediadownloader.activities.input
import com.example.socialmediadownloader.activities.whatsapp_status_list
import com.example.socialmediadownloader.model.CategoryModel
import java.util.*


open class Cat_Adapter(context : Activity, categories : ArrayList<CategoryModel>) : ArrayAdapter<CategoryModel>(context, 0 , categories) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var item : View? = convertView

        if(item == null) item = LayoutInflater.from(context).inflate(R.layout.catgory_list,parent,false)



        val curruntItem : CategoryModel? = getItem(position)

        val icon_d = item?.findViewById( R.id.d_icon ) as ImageView
        val icon = item.findViewById( R.id.arrow_icon ) as ImageView
        val platformname = item.findViewById( R.id.donwloader_name) as TextView
        val card = item.findViewById( R.id.card) as LinearLayout
        val type = item.findViewById(R.id.type) as TextView

        if (curruntItem != null) {
            platformname.text = curruntItem.cat_name
            if(curruntItem.cat_name == "Audio"){
                type.setText("Extractor")
            }else{
                type.setText("Downloader")
            }
            icon.setImageResource(curruntItem.cat_download_icon)
            icon_d.setImageResource(curruntItem.cat_icon)

            card.setOnClickListener {
                when(curruntItem.cat_name){
                    "Audio" -> {
                        val intent = Intent(context, audio_downloader_input::class.java)
                        context.startActivity(intent)
                    }

                    "Whatsapp" -> {
                        val intent = Intent(context, whatsapp_status_list::class.java)
                        context.startActivity(intent)
                    }


                    else -> {
                        val intent = Intent(context, input::class.java)
                        intent.putExtra("downloader_data", curruntItem)
                        context.startActivity(intent)
                    }

                }
            }
        }


        return item
    }

}
