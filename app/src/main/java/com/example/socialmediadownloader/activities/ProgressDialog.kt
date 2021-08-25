package com.example.socialmediadownloader.activities

import android.app.Activity
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmediadownloader.R
import com.example.socialmediadownloader.model.DownloadModel
import com.example.socialmediadownloader.utils.functions
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_progress_dialog.*


class ProgressDialog(val a: Activity, val dm: DownloadManager, val downloadmodel: DownloadModel) : Dialog(a) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_dialog)

        val config = RealmConfiguration.Builder()
            .allowWritesOnUiThread(true)
            .build()


        progressDialog_progress.setProgress(0)
        pd_downloaded_size.setText("0 KB/")
        pd_total_size.text = downloadmodel.total_size
        pd_percentage.setText("0%")

        cancel_download.setOnClickListener {
            Log.e("Button Status", "preessed")

            val realm = Realm.getInstance(config)

            realm.executeTransactionAsync(object : Realm.Transaction {
                override fun execute(realm: Realm) {
                    val rows: RealmResults<DownloadModel> = realm.where<DownloadModel>(DownloadModel::class.java).equalTo("id", downloadmodel.id).findAll()
                    rows.deleteAllFromRealm()
                }
            })


            downloadmodel.is_paused = true
            dm.remove(downloadmodel.downloadId)
            Toast.makeText(a, "Download Cancelled", Toast.LENGTH_LONG).show()
            dismiss()
        }
    }


    inner class DownloadTask(val model: DownloadModel, val context: Context) : AsyncTask<String, String, String>() {


        override fun doInBackground(vararg params: String?): String {
            downloadFileProcess(params[0])
            return ""
        }

        fun downloadFileProcess(downloadid: String?){
            val dm : DownloadManager =  context.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
            while(!model.is_paused){
                val query : DownloadManager.Query = DownloadManager.Query()
                val id : Long = (downloadid.toString()).toLong()
                query.setFilterById(id)
                val cursor = dm.query(query)
                cursor.moveToFirst()
                var previousprogress = 0

                var bytesDownloaded  = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                var toatlBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                if(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    model.is_paused = false
                }

                if(bytesDownloaded <= 0){
                    bytesDownloaded = 1
                }

                if(toatlBytes <= 0){
                    toatlBytes = 1
                }

                if(bytesDownloaded > 1){
                    val progress : Int = ((bytesDownloaded*100L)/toatlBytes).toInt()
                    if(progress == 100){
                        model.is_paused = false
                    }else{
                        if(progress >= (previousprogress + 1)){
                            publishProgress(progress.toString(), bytesDownloaded.toString())
                            previousprogress = progress
                        }
                    }
                }

                cursor.close()
            }
        }

        override fun onProgressUpdate(vararg values: String?) {
            super.onProgressUpdate(*values)
            ( context as Activity).runOnUiThread(
                object : Runnable {
                    override fun run() {
                        pd_downloaded_size.text = (functions().bytesIntoHumanReadable(
                            values[1].toString().toLong()
                        ).toString() + "/")
                        progressDialog_progress.setProgress(Integer.parseInt(values[0].toString()))
                        pd_percentage.text = (values[0] + "%")
                    }
                }
            )
        }
    }

    fun runTask(ds: DownloadTask, id: String){
        try{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                ds.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, id)
            }else{
                ds.execute(id)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}
