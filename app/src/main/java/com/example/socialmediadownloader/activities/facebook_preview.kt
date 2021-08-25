package com.example.socialmediadownloader.activities

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.socialmediadownloader.R
import com.example.socialmediadownloader.model.DownloadModel
import com.example.socialmediadownloader.utils.NetworkChangeReceiver
import com.example.socialmediadownloader.utils.functions
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_facebook_preview.*
import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URL
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*


class facebook_preview : AppCompatActivity() {

    var downloadurl = ""
    var title : String = " "
    lateinit var realm: Realm
    lateinit var progress_custom : ProgressDialog
    lateinit var newdownloadmodel : DownloadModel
    lateinit var networkChangeReciver : NetworkChangeReceiver
    lateinit var no_intenet : no_internet_dialog

    val config = RealmConfiguration.Builder()
        .allowWritesOnUiThread(true)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facebook_preview)

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

        realm = Realm.getInstance(config)
        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        if(Build.VERSION.SDK_INT > 21){
            window.statusBarColor = resources.getColor(R.color.facebook_dark)
        }

        back_fb_preview.setOnClickListener {
            onBackPressed()
        }

        val link = intent.getStringExtra("link")

        getFacebookData(link.toString())

        download_button_fb.setOnClickListener {
            checkForStoragePermission()
        }

        fb_play.setOnClickListener {
            val intent = Intent(this,video_play_activity::class.java)
            intent.putExtra("header",R.color.facebook_light)
            intent.putExtra("isuri",true)
            intent.putExtra("path",downloadurl)
            intent.putExtra("toolbar", R.color.facebook_dark)
            startActivity(intent)
        }
    }



    private fun checkForStoragePermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            downloadFromUrl(downloadurl, title, title)
        }else{
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                downloadFromUrl(downloadurl, title, title)
            }else{
                Toast.makeText(this, "We need Your Permission!", Toast.LENGTH_LONG).show();
            }
        }
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 1
    }

    private fun downloadFromUrl(fburl: String, downloadTitle: String, fileName: String) {
        var name_of_file : String =  fileName.replace("| Facebook", "")
        val re = Regex("[^A-Za-z0-9% ]")
        name_of_file = re.replace(name_of_file, "-")
        val uri = Uri.parse(fburl)
        val request = DownloadManager.Request(uri)
        request.setTitle(downloadTitle)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        if(Build.VERSION.SDK_INT > 29){
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "/DL- All In One Downloader/Facebook/" + name_of_file + ".mp4"
            )
        }else {
            request.setDestinationInExternalPublicDir(
                "DL- All In One Downloader/Facebook",
                name_of_file + ".mp4"
            )
        }

        val manager : DownloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadid : Long = manager.enqueue(request)

        val currentnum = realm.where<DownloadModel>(DownloadModel::class.java).max("id")
        var nextid  = 0

        if(currentnum == null){
            nextid = 1
        }else{
            nextid = currentnum.toInt() + 1
        }

        val c: Date = Calendar.getInstance().getTime()
        val df = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val formattedDate: String = df.format(c)


        var filepath = Environment.getExternalStoragePublicDirectory("DL- All In One Downloader/Facebook").absolutePath + "/" + name_of_file + ".mp4"
        if(Build.VERSION.SDK_INT > 29){
            filepath = "/storage/emulated/0/" + Environment.DIRECTORY_DOWNLOADS + "/DL- All In One Downloader/Facebook/" + name_of_file + ".mp4"
        }

        newdownloadmodel = DownloadModel();
        newdownloadmodel.id = nextid.toLong()
        newdownloadmodel.downloadId = downloadid.toString().toLong()
        newdownloadmodel.title = name_of_file
        newdownloadmodel.file_path = filepath
        newdownloadmodel.file_size = "0"
        newdownloadmodel.status = "Downloading"
        newdownloadmodel.progress = "0"
        newdownloadmodel.is_paused = false
        newdownloadmodel.total_size = facebook_download_size.text.toString()
        newdownloadmodel.platfrom = R.drawable.facebookhd
        newdownloadmodel.date = formattedDate

        realm.executeTransactionAsync(object : Realm.Transaction {
            override fun execute(realm: Realm) {
                realm.copyToRealm(newdownloadmodel)
            }

        })

        progress_custom = ProgressDialog(this, manager, newdownloadmodel)
        val task = progress_custom.DownloadTask(newdownloadmodel, this)
        progress_custom.runTask(task, downloadid.toString())
        progress_custom.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        progress_custom.setCancelable(false)
        progress_custom.show()
    }

    private val onComplete  =  object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if(intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE){
                val query = DownloadManager.Query()
                query.setFilterById(id)
                val dm : DownloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val cursor : Cursor = dm.query(DownloadManager.Query().setFilterById(id))
                if(cursor.moveToFirst()){
                    val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = cursor.getInt(columnIndex)
                    when(status){
                        DownloadManager.STATUS_SUCCESSFUL -> {

                            progress_custom.dismiss()
                            Toast.makeText(
                                this@facebook_preview,
                                "Download Complete",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }

                        DownloadManager.STATUS_FAILED -> {
                            progress_custom.dismiss()
                            Toast.makeText(
                                this@facebook_preview,
                                "Download Cancelled",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                }
            }
        }
    }

    fun getFacebookData(link : String){

        val request = StringRequest(Request.Method.GET, link,

            Response.Listener <String>{
                var res = it
                val doc : Document = Jsoup.parse(res)
                val elements = doc.select("meta[property=og:video]") as List<Element>
                val videos = elements.map { it.attr("content").replace("&amp;", "&") }
                Log.e("Videos : ", elements.toString())
                val thumbnail_elements = doc.select("meta[property=og:image]") as List<Element>
                val thumbnail = thumbnail_elements.map { it.attr("content").replace("&amp","&") }
                val title = doc.title()
                if(elements.size > 0 && thumbnail_elements.size > 0){
                    if(videos[0].contains(".mpd")){
                        scrapTask().execute("")
                    }else{
                        scrapTask().execute(videos[0],title,thumbnail[0])
                    }
                }else{

                }
                return@Listener

            },

            Response.ErrorListener {
                Log.e("Error : ", it.toString())
                scrapTask().execute("")
                return@ErrorListener
            })

        val queue = Volley.newRequestQueue(this@facebook_preview)
        queue.add(request)
    }


    inner class scrapTask : AsyncTask<String,Void,Array<String>>(){

        override fun doInBackground(vararg params: String?): Array<String> {

            if(params.size > 2 && params[0] != null){
                var data : Array<String> = arrayOf("","","","")
                if(params[0].toString().length > 5){
                    val url = URL(params[0])
                    val urlConnection: URLConnection = url.openConnection()
                    urlConnection.connect()
                    val file_size : Long = ((urlConnection.contentLength) as Number).toLong()
                    val downloadsize = functions().bytesIntoHumanReadable(file_size) + ""
                    data.set(0,params[0].toString())
                    data.set(1,params[1].toString())
                    data.set(2,params[2].toString())
                    data.set(3,downloadsize)
                }
                return data
            }else{
                return arrayOf("private")
            }

        }

        override fun onPostExecute(result: Array<String>) {
           if(result != null){
               if(result.get(0)  != "private"){
                   downloadurl = result.get(0)
                   title = result.get(1)
                   val thumbnail = result.get(2)
                   val downloadsize = result.get(3)

                   Glide.with(this@facebook_preview).asBitmap().placeholder(R.drawable.placeholder).load(thumbnail).into(facebook_video)
                   fb_video_title.setText(title)
                   facebook_download_size.setText(downloadsize)
                   progress_fb.visibility = View.GONE
                   facebook_video_data.visibility = View.VISIBLE
               }else{
                   progress_fb.visibility = View.GONE
                   facebook_error.visibility = View.VISIBLE
               }
           }else{
               progress_fb.visibility = View.GONE
               facebook_error.visibility = View.VISIBLE
           }
        }

    }

    override fun onDestroy() {
        unregisterReceiver(onComplete)
        unregisterReceiver(networkChangeReciver)
        realm.close()
        no_intenet.dismiss()
        super.onDestroy()
    }

}
