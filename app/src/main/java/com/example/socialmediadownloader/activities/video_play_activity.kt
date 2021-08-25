package com.example.socialmediadownloader.activities

import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmediadownloader.R
import com.example.socialmediadownloader.utils.NetworkChangeReceiver
import kotlinx.android.synthetic.main.activity_video_play_activity.*


class video_play_activity : AppCompatActivity() {

    lateinit var networkChangeReciver : NetworkChangeReceiver
    lateinit var no_intenet : no_internet_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play_activity)


        val bg = intent.getIntExtra("header", 0)
        val toolbar = intent.getIntExtra("toolbar", 0)
        val isUri = intent.getBooleanExtra("isuri", true)
        val path = intent.getStringExtra("path")

        no_intenet = no_internet_dialog(this)

        networkChangeReciver = NetworkChangeReceiver(object : NetworkChangeReceiver.NetworkChangeListener {
            override fun onNetworkConnectedStateChanged(connected: Boolean) {
                if(isUri ==  true){
                    if(connected){
                        no_intenet.dismiss()
                    }else{
                        video_view.pause()
                        no_intenet.show()
                    }
                }
            }
        })

        registerReceiver(networkChangeReciver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

        if(Build.VERSION.SDK_INT > 21){
            window.statusBarColor = resources.getColor(toolbar)
        }

        video_play_header.setBackgroundColor(resources.getColor(bg))
        video_load.setColor(resources.getColor(bg))

        val md = MediaController(this)
        video_view.setMediaController(md)
        md.setAnchorView(video_con)

        if(isUri == true){
            video_view.setVideoPath(path)
        }else{
            video_view.setVideoURI(Uri.parse(path))
        }

        video_view.requestFocus()

        video_view.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {
            override fun onPrepared(mp: MediaPlayer?) {
                Log.e("status", "preaperd")
                video_load.visibility = View.GONE
                video_con.alpha = 1F
                video_view.start()
            }
        })

        video_view.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
            override fun onCompletion(mp: MediaPlayer?) {
                if (isUri == true) {
                    video_view.setVideoPath(path)
                }else{
                    video_view.setVideoURI(Uri.parse(path))
                }
                video_view.start()
            }
        })


        back_video_play.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onDestroy() {
        video_view.stopPlayback()
        unregisterReceiver(networkChangeReciver)
        no_intenet.dismiss()
        super.onDestroy()
    }

}
