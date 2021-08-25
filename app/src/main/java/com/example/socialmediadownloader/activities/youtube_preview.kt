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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.socialmediadownloader.R
import com.example.socialmediadownloader.model.DownloadModel
import com.example.socialmediadownloader.utils.NetworkChangeReceiver
import com.example.socialmediadownloader.utils.functions
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_youtube_preview.*
import java.net.URL
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*


class youtube_preview : AppCompatActivity() {

    lateinit var sizetext : TextView
    var downloadurl : String = " "
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
        setContentView(R.layout.activity_youtube_preview)

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
            window.statusBarColor = resources.getColor(R.color.youtube_dark)
        }

        back_y.setOnClickListener {
            onBackPressed()
        }

        val thumbnail_data = intent.getStringExtra("thumbnail")
        title = intent.getStringExtra("title").toString()
        downloadurl = intent.getStringExtra("downloadurl").toString()
        val channelname = intent.getStringExtra("channel_name")

        val size = GetSize()
        size.execute(downloadurl)

        sizetext = d_size

        Glide.with(this).load(thumbnail_data).placeholder(R.drawable.placeholder).into(thumbnail)
        video_title.setText(title)
        c_name.setText(channelname)

        download_button_youtube.setOnClickListener {
            checkForStoragePermission()
        }

        play_button_youtube.setOnClickListener{
            val intent = Intent(this,video_play_activity::class.java)
            intent.putExtra("header",R.color.youtube_light)
            intent.putExtra("isuri",true)
            intent.putExtra("path",downloadurl)
            intent.putExtra("toolbar", R.color.youtube_dark)
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

    private fun downloadFromUrl(youtubeDlUrl: String, downloadTitle: String, file_Name: String) {
        val re = Regex("[^A-Za-z0-9% ]")
        val fileName = re.replace(file_Name,"-")
        val uri = Uri.parse(youtubeDlUrl)
        val request = DownloadManager.Request(uri)
        request.setTitle(downloadTitle)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        if(Build.VERSION.SDK_INT > 29){
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "/DL- All In One Downloader/YouTube/" + fileName + ".mp4"
            )
        }else{
            request.setDestinationInExternalPublicDir(
                "DL- All In One Downloader/YouTube",
                fileName + ".mp4"
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

        var filepath = Environment.getExternalStoragePublicDirectory("DL- All In One Downloader/YouTube").absolutePath + "/" + fileName + ".mp4"
        if(Build.VERSION.SDK_INT > 29){
            filepath = "/storage/emulated/0/" + Environment.DIRECTORY_DOWNLOADS.toString() + "/DL- All In One Downloader/YouTube/" + fileName + ".mp4"
        }

        Log.e("path = " , filepath.toString())


        newdownloadmodel = DownloadModel()
        newdownloadmodel.id = nextid.toLong()
        newdownloadmodel.downloadId = downloadid.toString().toLong()
        newdownloadmodel.title = fileName
        newdownloadmodel.file_path = filepath
        newdownloadmodel.file_size = "0"
        newdownloadmodel.status = "Downloading"
        newdownloadmodel.progress = "0"
        newdownloadmodel.is_paused = false
        newdownloadmodel.total_size = sizetext.text.toString()
        newdownloadmodel.platfrom = R.drawable.youtubeinputscreen
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

    inner class GetSize : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String?): String {
            val url = URL(params[0])
            val urlConnection: URLConnection = url.openConnection()
            urlConnection.connect()
            val file_size : Long = ((urlConnection.contentLength) as Number).toLong()

            return (functions().bytesIntoHumanReadable(file_size).toString());
        }

        override fun onPostExecute(result: String?) {
            sizetext.setText(result)
            progress_size.visibility = View.GONE
        }
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
                                this@youtube_preview,
                                "Download Complete",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }

                        DownloadManager.STATUS_FAILED -> {
                            progress_custom.dismiss()
                            Toast.makeText(
                                this@youtube_preview,
                                "Download Cancelled",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                }
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
