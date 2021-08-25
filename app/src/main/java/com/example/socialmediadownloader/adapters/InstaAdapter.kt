package com.example.socialmediadownloader.adapters

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.socialmediadownloader.R
import com.example.socialmediadownloader.activities.video_play_activity
import com.example.socialmediadownloader.model.InstaModel
import java.util.*


open class InstaAdapter(context : Activity, post : ArrayList<InstaModel>) : ArrayAdapter<InstaModel>(context, 0 , post) {


    private var onClickListener : OnClickListener? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var item : View? = convertView

        if(item == null) item = LayoutInflater.from(context).inflate(R.layout.insta_post,parent,false)



        val curruntItem : InstaModel? = getItem(position)

        val image = item?.findViewById( R.id.display_image ) as ImageView
        val size = item?.findViewById( R.id.insta_download_size ) as TextView
        val video = item?.findViewById(R.id.video_insta) as ImageView
        val db = item?.findViewById(R.id.download_button_insta) as CardView

        if (curruntItem != null) {
            if(curruntItem.product_type == "clips"){
                image.layoutParams.height = dp_to_px(600)
            }

            Glide.with(context).load(Uri.parse(curruntItem.dispalayurl)).into(image)
            size.setText(curruntItem.size)
            Log.e("is_video", curruntItem.is_video.toString())
            if(curruntItem.is_video == true){
                video.visibility = View.VISIBLE
            }else{
                video.visibility = View.GONE
            }

            db.setOnClickListener{
                if(onClickListener != null){
                    onClickListener!!.onClick(position,curruntItem)
                }
            }

            video.setOnClickListener {
                val intent = Intent(context, video_play_activity::class.java)
                intent.putExtra("header",R.color.ig_light)
                intent.putExtra("isuri",true)
                intent.putExtra("path",curruntItem.downloadurl)
                intent.putExtra("toolbar", R.color.ig_dark)
                context.startActivity(intent)
            }
        }


        return item
    }

    interface OnClickListener {
        fun onClick(position: Int, model : InstaModel)
    }

    fun setOnClickListener(OnClickListener : OnClickListener){
        this.onClickListener = OnClickListener
    }

    private fun dp_to_px(dp : Int) : Int{
        val metrics = context.resources.displayMetrics
        return Math.round(dp * ( metrics.xdpi / DisplayMetrics.DENSITY_DEFAULT ))
    }

}
