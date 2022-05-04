package com.example.socialmediadownloader.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.socialmediadownloader.R
import com.example.socialmediadownloader.model.DownloadModel
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_whatsapp_preview.*
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.content.ContentResolver
import android.os.*
import org.apache.commons.io.FileUtils
import java.io.*


class whatsapp_preview : AppCompatActivity() {

    lateinit var newdownloadmodel : DownloadModel
    lateinit var realm : Realm
    lateinit var name : String
    lateinit var path : String
    var isVideo : Boolean? = null
    val config = RealmConfiguration.Builder()
        .allowWritesOnUiThread(true)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whatsapp_preview)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Files Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        realm = Realm.getInstance(config)

        if(Build.VERSION.SDK_INT > 21){
            window.statusBarColor = resources.getColor(R.color.whatsapp_dark)
        }

        path = intent.getStringExtra("path").toString()
        isVideo = intent.getBooleanExtra("isVideo", true)
        val size = intent.getStringExtra("size")
        name = intent.getStringExtra("name").toString()

        Log.e("path : ", path)
        Log.e("isVideo : ", isVideo.toString())
        Log.e("size : ", size.toString())



        if(path.length > 0){
            Glide.with(this).asBitmap().placeholder(R.drawable.black).load(path).into(status_preview_image)
        }

        if(isVideo == true){
            status_preview_isvideo.visibility = View.VISIBLE
        }

        if(Build.VERSION.SDK_INT >= 30){
            size_lay.visibility = View.GONE
        }else{
            staus_file_size.setText(size)
        }

        back_whatsapp_preview.setOnClickListener{
            onBackPressed()
        }

        download_button_whtasap.setOnClickListener {
            checkForpermission()
        }

        status_preview_isvideo.setOnClickListener {
            val intent = Intent(this,video_play_activity::class.java)
            intent.putExtra("header",R.color.whatsapp_light)
            intent.putExtra("isuri",false)
            intent.putExtra("path",path)
            intent.putExtra("toolbar", R.color.whatsapp_dark)
            startActivity(intent)
        }
    }

    fun checkForpermission(){
        Log.e("Inside Permision", "true")
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            saveFileTask().execute(name,path)
        }else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_PERMISSION
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == WRITE_PERMISSION){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                saveFileTask().execute(name,path)
            }else{
                Toast.makeText(this, "We need Your Permission!", Toast.LENGTH_LONG).show();
            }
        }
    }


    inner class saveFileTask : AsyncTask<String,Void,Array<String>>(){

        override fun doInBackground(vararg params: String?): Array<String> {
            var dir = Environment.getExternalStoragePublicDirectory("DL- All In One Downloader/Whatsapp").absolutePath
            if(Build.VERSION.SDK_INT >= 29){
                dir = "/storage/emulated/0/" + Environment.DIRECTORY_DOWNLOADS.toString() + "/DL- All In One Downloader/Whatsapp"
            }
            val dstpath = dir + "/" + params[0]
            try{
                val src = File(params[1])
                val dst = File(dstpath)

                if(!dst.parentFile.exists()){
                    dst.parentFile.mkdirs()
                }

                if(!dst.exists()){
                    Log.e("File path:", dst.absolutePath)
                    dst.createNewFile()
                }

                var inputStream : FileChannel? = null
                var outputStream : FileChannel? = null
                var inputstream : InputStream? = null
                var outputstream : OutputStream?= null

                try{
                    if(Build.VERSION.SDK_INT >= 30){
                        val buffer  = ByteArray(1000)
                        val content: ContentResolver = this@whatsapp_preview.contentResolver
                        inputstream = content.openInputStream(Uri.parse(params[1]))
                        outputstream = FileOutputStream(dstpath);

                        if (inputstream != null) {
                            while(inputstream.read( buffer, 0, buffer.size) >= 0){
                                outputstream.write(buffer,0,buffer.size)
                            }
                        }
                    }else{
                        inputStream = FileInputStream(src).channel
                        outputStream = FileOutputStream(dst).channel
                        outputStream.transferFrom(inputStream,0,inputStream.size())
                        Log.e("Done", "Done")
                    }
                }finally {
                    if(inputStream!=null){
                        inputStream.close()
                    }

                    if(outputStream != null){
                        outputStream.close()
                    }

                    if (inputstream != null) {
                        inputstream.close()
                    }

                    if (outputstream != null) {
                        outputstream.close()
                    }
                }


                val filename = params[0]

                return arrayOf(filename.toString(),dstpath)

            }catch (e : Exception){
                e.printStackTrace()
                return arrayOf("")
            }

        }

        override fun onPostExecute(result: Array<String>?) {
            if(result != null){
                if(result.get(0).length > 0){
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

                    var filepath = result.get(1)
                    var title = result.get(0)

                    val re = Regex("[^A-Za-z0-9% ]")
                    title = title.substring(0,title.indexOf("."))
                    title = re.replace(title,"-")

                    newdownloadmodel = DownloadModel();
                    newdownloadmodel.id = nextid.toLong()
                    newdownloadmodel.downloadId = 123
                    newdownloadmodel.title = title
                    newdownloadmodel.file_path = filepath
                    newdownloadmodel.file_size = "0"
                    newdownloadmodel.status = "Downloading"
                    newdownloadmodel.progress = "0"
                    newdownloadmodel.is_paused = false
                    newdownloadmodel.total_size = staus_file_size.text.toString()
                    newdownloadmodel.platfrom = R.drawable.whatsapp
                    newdownloadmodel.date = formattedDate

                    realm.executeTransactionAsync(object : Realm.Transaction {
                        override fun execute(realm: Realm) {
                            realm.copyToRealm(newdownloadmodel)
                        }

                    })

                    Toast.makeText(this@whatsapp_preview,"Status Downloaded",Toast.LENGTH_SHORT).show()
                    val selectedUri = Uri.parse(filepath)
                    val intent = Intent(Intent.ACTION_VIEW)
                    if(filepath.endsWith(".mp4")){
                        intent.setDataAndType(selectedUri, "video/*")
                    }else if(filepath.endsWith(".jpg") || filepath.endsWith(".jpeg") || filepath.endsWith(".png")){
                        intent.setDataAndType(selectedUri, "image/*")
                    }else{
                        intent.setDataAndType(selectedUri, "*/*")
                    }
                    val contentintent = PendingIntent.getActivity(this@whatsapp_preview,0,intent, PendingIntent.FLAG_CANCEL_CURRENT)
                    val notification = NotificationCompat.Builder(this@whatsapp_preview, CHANNEL_ID)
                    notification.setContentTitle("DL-All In One Downloader")
                    notification.setContentText("Status Successfully Saved in Your Gallery")
                    notification.setSmallIcon(R.drawable.download)
                    if (Build.VERSION.SDK_INT > 23) {
                        notification.setColor(getColor(R.color.colorPrimary))
                    }
                    notification.setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    notification.setAutoCancel(true)
                    notification.setContentIntent(contentintent)

                    val managerCompat = NotificationManagerCompat.from(this@whatsapp_preview)
                    managerCompat.notify(1, notification.build())

                    finish()
                }else{
                    Toast.makeText(this@whatsapp_preview,"Something Bad Happen",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()
    }

    companion object {
        val CHANNEL_ID = "STATUSNOTIFY"
        val WRITE_PERMISSION = 45
    }
}
