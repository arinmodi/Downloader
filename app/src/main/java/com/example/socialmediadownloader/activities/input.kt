package com.example.socialmediadownloader.activities

import android.content.*
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.example.socialmediadownloader.R
import com.example.socialmediadownloader.model.CategoryModel
import com.example.socialmediadownloader.utils.NetworkChangeReceiver
import kotlinx.android.synthetic.main.activity_input.*
import kotlinx.android.synthetic.main.activity_main.*
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import java.util.*


class input : AppCompatActivity() {

    lateinit var networkChangeReciver : NetworkChangeReceiver
    lateinit var no_intenet : no_internet_dialog


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        val prefs : SharedPreferences = getSharedPreferences("fisrtTimeInput",0)
        val editor = prefs.edit()
        val firstrun = prefs.getBoolean("firstRunInput",true)
        if(firstrun){
            showIntro()
            editor.putBoolean("firstRunInput",false)
            editor.apply()
        }

        no_intenet = no_internet_dialog(this)

        networkChangeReciver = NetworkChangeReceiver(object : NetworkChangeReceiver.NetworkChangeListener {
            override fun onNetworkConnectedStateChanged(connected: Boolean) {
                if(connected){
                    no_intenet.dismiss()
                }else{
                    no_intenet.show()
                }
            }
        })

        registerReceiver(networkChangeReciver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

        val data = intent.getSerializableExtra("downloader_data") as CategoryModel

        if(Build.VERSION.SDK_INT > 21){
            window.statusBarColor = resources.getColor(data.tool_bar)
        }


        paste_button.setCardBackgroundColor(resources.getColor(data.header))



        header.setBackgroundColor(resources.getColor(data.header))
        download_button.setCardBackgroundColor(resources.getColor(data.header))
        input_title.setText(data.input_title)
        url_input.hint = data.hint
        d_name.setText(data.cat_name)
        icon_input_hd.setImageResource(data.header_icon)

        back.setOnClickListener {
            onBackPressed()
        }

        paste_button.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val text = clipboard.primaryClip?.getItemAt(0)?.text
            if(text != null && text.length > 0){
                url_input.setText(text)
            }else{
                val toast = Toast.makeText(this, "No Text Copied!", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        }


        download_button.setOnClickListener {
            download_button.isEnabled = false
            val urlinput = url_input.text.toString()
            if(url_input.text.length == 0 || url_input.text == null ){
                download_button.isEnabled = true
                val toast = Toast.makeText(this, "Input Missing!", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }else{
                when(data.cat_name){
                    "You-Tube" -> {
                        if (urlinput.contains("youtube") || urlinput.contains("youtu.be")) {
                            progress.visibility = View.VISIBLE
                            GetYouTubeDownloadUrl(urlinput)
                        } else {
                            download_button.isEnabled = true
                            progress.visibility = View.GONE
                            Toast.makeText(
                                this,
                                "please provide a youtube video url!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    "Instagram" -> {
                        if (urlinput.contains("instagram")) {
                            var finalurl = ""
                            if (urlinput.contains("?utm_source=ig.web_copy_link")) {
                                val replace = "?utm_source=ig.web_copy_link"
                                finalurl = urlinput.replace(replace, "")
                            } else if (urlinput.contains("?utm_medium=copy_link")) {
                                val replace = "?utm_medium=copy_link"
                                finalurl = urlinput.replace(replace, "")
                            } else {
                                finalurl = urlinput
                            }

                            finalurl = finalurl + "?__a=1"

                            Log.e("Final URI", finalurl)

                            val intent = Intent(this, instagram_preview::class.java)
                            intent.putExtra("url", finalurl)
                            download_button.isEnabled = true
                            startActivity(intent)
                        } else {
                            download_button.isEnabled = true
                            Toast.makeText(
                                this,
                                "please provide a valid instagram url!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    "Face-Book" -> {
                        if (urlinput.contains("facebook")) {
                            val intent = Intent(this, facebook_preview::class.java)
                            intent.putExtra("link", urlinput)
                            download_button.isEnabled = true
                            startActivity(intent)
                        } else {
                            download_button.isEnabled = true
                            Toast.makeText(
                                this,
                                "please provide a valid facebook url!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun GetYouTubeDownloadUrl(link: String){
        val yeEx : YouTubeExtractor = object : YouTubeExtractor(this){

            override fun onExtractionComplete(
                ytFiles: SparseArray<YtFile>?,
                videoMeta: VideoMeta?
            ) {
                if(ytFiles!= null && videoMeta != null && ytFiles.size() > 0){
                    Log.e("Execution", "Start")
                    val itag = 22
                    Log.e("yt:", ytFiles.toString())
                    val iTags: List<Int> = Arrays.asList(22, 137, 18)
                    var finalitag = 0

                    for(itag in iTags){
                        val ytfile = ytFiles.get(itag)

                        if(ytfile!=null){
                            finalitag = itag
                        }
                    }

                    if(finalitag != 0){
                        val downloadUrl = ytFiles[finalitag].url
                        val video_title = videoMeta.title
                        val channelname = videoMeta.author
                        val thumb_url = "https://img.youtube.com/vi/" + videoMeta.videoId.toString() + "/hqdefault.jpg"
                        val intent = Intent(this@input, youtube_preview::class.java)
                        intent.putExtra("thumbnail", thumb_url)
                        intent.putExtra("title", video_title)
                        intent.putExtra("channel_name", channelname)
                        intent.putExtra("downloadurl", downloadUrl)
                        intent.putExtra("desc", videoMeta.shortDescription)
                        progress.visibility = View.GONE
                        download_button.isEnabled = true
                        startActivity(intent)
                    }else{
                        download_button.isEnabled = true
                        val popup = error_dialog(
                            this@input,
                            "video may be private or age restricated or deleted"
                        )
                        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        popup.show()
                    }
                }else{
                    download_button.isEnabled = true
                    progress.visibility = View.GONE
                    Toast.makeText(this@input, "enter a proper url", Toast.LENGTH_LONG).show()

                }
            }

        }

        yeEx.execute(link)
    }


    override fun onDestroy() {
        unregisterReceiver(networkChangeReciver)
        no_intenet.dismiss()
        super.onDestroy()
    }

    private fun showIntro(){
        GuideView.Builder(this)
            .setTitle("Paste Button")
            .setTitleTypeFace(Typeface.DEFAULT_BOLD)
            .setContentText("Use this button to paste link...")
            .setTargetView(paste_button)
            .setContentTextSize(14)
            .setTitleTextSize(16)
            .setGravity(GuideView.Gravity.center)
            .setDismissType(GuideView.DismissType.anywhere)
            .build()
            .show()
    }

}
