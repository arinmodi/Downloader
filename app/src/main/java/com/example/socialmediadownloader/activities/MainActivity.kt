package com.example.socialmediadownloader.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmediadownloader.R
import com.example.socialmediadownloader.adapters.Cat_Adapter
import com.example.socialmediadownloader.model.CategoryModel
import com.example.socialmediadownloader.utils.MyNewIntentService
import kotlinx.android.synthetic.main.activity_main.*
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import java.util.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs : SharedPreferences = getSharedPreferences("fisrtTime", 0)
        val editor = prefs.edit()
        val firstrun = prefs.getBoolean("firstRun", true)
        if(firstrun){

            val alrmIntent = Intent(this, MyNewIntentService::class.java)
            val pi = PendingIntent.getBroadcast(this, 0, alrmIntent, 0)

            val am = getSystemService(ALARM_SERVICE) as AlarmManager

            val alarmstartTime = Calendar.getInstance()
            val now = Calendar.getInstance()
            alarmstartTime.set(Calendar.HOUR_OF_DAY,21)
            alarmstartTime.set(Calendar.MINUTE,30)
            alarmstartTime.set(Calendar.SECOND,0)
            if(now.after(alarmstartTime)){
                Log.d("Hey","Added a day");
                alarmstartTime.add(Calendar.DATE, 1);
            }

            am.setRepeating(AlarmManager.RTC_WAKEUP,alarmstartTime.timeInMillis,AlarmManager.INTERVAL_DAY, pi)

            showIntro()
            editor.putBoolean("firstRun", false)
            editor.apply()
        }

        val cat_list : ArrayList<CategoryModel> = ArrayList<CategoryModel>()

        cat_list.add(
            CategoryModel(
                R.drawable.tube_input_screen,
                "Video",
                R.drawable.tube_icon,
                R.color.tube_dark,
                R.color.tube_light,
                R.drawable.tube_input_screen,
                "e.g. Paste Link Here",
                "Enter the Tube link, here"
            )
        )

        cat_list.add(
            CategoryModel(
                R.drawable.facebookhd,
                "Face-Book",
                R.drawable.facebook_icon,
                R.color.facebook_dark,
                R.color.facebook_light,
                R.drawable.facebookhd,
                "e.g. https://www.facebook.com/",
                "Enter the URL of Facebook Post"
            )
        )

        cat_list.add(
            CategoryModel(
                R.drawable.instagramhd,
                "Instagram",
                R.drawable.insta_icon,
                R.color.ig_dark,
                R.color.ig_light,
                R.drawable.instagramhd,
                "e.g. https://www.instagram.com/",
                "Enter the URL of Instagram Post"
            )
        )

        cat_list.add(
            CategoryModel(
                R.drawable.whatsapp,
                "Whatsapp",
                R.drawable.whtasapp_icon,
                R.color.whatsapp_dark,
                R.color.whatsapp_light,
                R.drawable.whatsapp,
                "",
                ""
            )
        )

        cat_list.add(
            CategoryModel(
                R.drawable.headphone,
                "Audio",
                R.drawable.songs_icon,
                R.color.audio_dark,
                R.color.audio_light,
                R.drawable.headphone,
                "audio-extractor",
                "audio_extractor"
            )
        )

        val cat_adapter = Cat_Adapter(this, cat_list)
        lv.adapter = cat_adapter

        download_list.setOnClickListener {
            val intent = Intent(this, Download::class.java)
            startActivity(intent)
        }

    }

    private fun showIntro(){
        GuideView.Builder(this)
            .setTitle("Downloads")
            .setTitleTypeFace(Typeface.DEFAULT_BOLD)
            .setContentText("you can check your downloads here...")
            .setTargetView(download_list)
            .setContentTextSize(14)
            .setTitleTextSize(16)
            .setGravity(GuideView.Gravity.center)
            .setDismissType(GuideView.DismissType.anywhere)
            .build()
            .show()
    }

}
