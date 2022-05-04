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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.android.quakereport.QueryUtils
import com.example.socialmediadownloader.R
import com.example.socialmediadownloader.adapters.InstaAdapter
import com.example.socialmediadownloader.model.DownloadModel
import com.example.socialmediadownloader.model.InstaModel
import com.example.socialmediadownloader.utils.NetworkChangeReceiver
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_instagram_preview.*
import java.text.SimpleDateFormat
import java.util.*

class instagram_preview : AppCompatActivity() {

    lateinit var instaadpater : InstaAdapter
    var downloadurl = ""
    var title = ""
    var is_video = false
    var size = ""
    lateinit var realm: Realm
    lateinit var progress_custom : ProgressDialog
    lateinit var downloadmodel : DownloadModel
    lateinit var instamodel : InstaModel
    lateinit var networkChangeReciver : NetworkChangeReceiver
    lateinit var no_intenet : no_internet_dialog

    val config = RealmConfiguration.Builder()
        .allowWritesOnUiThread(true)
        .build()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instagram_preview)

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

        back_insta.setOnClickListener {
            onBackPressed()
        }

        if(Build.VERSION.SDK_INT > 21){
            window.statusBarColor = resources.getColor(R.color.ig_dark)
        }

        val url = intent.getStringExtra("url")
        instaadpater = InstaAdapter(this, ArrayList<InstaModel>())
        insta_post.adapter = instaadpater

        InstaTask().execute(url)

        instaadpater.setOnClickListener(object : InstaAdapter.OnClickListener{
            override fun onClick(position: Int, model: InstaModel) {
                downloadurl = model.downloadurl
                title = model.id
                is_video = model.is_video
                size = model.size
                instamodel = model
                checkForStoragePermission()
            }
        })
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
        var file_name = fileName
        if(is_video == true){
            file_name = file_name + ".mp4"
        }else{
            file_name = file_name + ".png"
        }

        if(Build.VERSION.SDK_INT >= 29){
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "/DL- All In One Downloader/Instagram/" + file_name
            )
        }else {
            request.setDestinationInExternalPublicDir(
                "DL- All In One Downloader/Instagram",
                file_name
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

        var filepath = Environment.getExternalStoragePublicDirectory("DL- All In One Downloader/Instagram").absolutePath + "/" + file_name
        if(Build.VERSION.SDK_INT >= 29){
            filepath = "/storage/emulated/0/" + Environment.DIRECTORY_DOWNLOADS + "/DL- All In One Downloader/Instagram/" + file_name
        }

        downloadmodel = DownloadModel()
        downloadmodel.id = nextid.toLong()
        downloadmodel.downloadId = downloadid.toString().toLong()
        downloadmodel.title = fileName
        downloadmodel.file_path = filepath
        downloadmodel.file_size = "0"
        downloadmodel.status = "Downloading"
        downloadmodel.progress = "0"
        downloadmodel.is_paused = false
        downloadmodel.total_size = size
        downloadmodel.platfrom = R.drawable.instagramhd
        downloadmodel.date = formattedDate

        realm.executeTransactionAsync(object : Realm.Transaction {
            override fun execute(realm: Realm) {
                realm.copyToRealm(downloadmodel)
            }
        })

        progress_custom = ProgressDialog(this, manager, downloadmodel)
        val task = progress_custom.DownloadTask(downloadmodel, this)
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
                            Toast.makeText(this@instagram_preview,"Download Complete", Toast.LENGTH_LONG).show()
                            instaadpater.remove(instamodel)
                            if(instaadpater.count == 0){
                                finish()
                            }
                        }

                        DownloadManager.STATUS_FAILED -> {
                            progress_custom.dismiss()
                            Toast.makeText(this@instagram_preview,"Download Cancelled", Toast.LENGTH_LONG).show()
                        }
                    }

                }
            }
        }

    }


    inner class InstaTask : AsyncTask<String, Void, List<InstaModel>>() {

        override fun doInBackground(vararg params: String?): List<InstaModel>? {
            if(params[0] == null){
                return null
            }

            val result = QueryUtils().fetchInstaPostData(params[0].toString())
            return result
        }

        override fun onPostExecute(result: List<InstaModel>?) {
            instaadpater.clear()
            progress_insta.visibility = View.GONE

            if (result != null) {
                if(!result.isEmpty()){
                    instaadpater.addAll(result)
                    Glide.with(this@instagram_preview).load(Uri.parse(result[0].user_pic)).into(profile_pic)
                    pic_con.visibility = View.VISIBLE
                    page_name.setText(result[0].username)
                    data_insta_post.visibility = View.VISIBLE
                }else{
                    private_post.visibility = View.VISIBLE
                }
            }else{
                private_post.visibility = View.VISIBLE
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
