package com.example.socialmediadownloader.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialmediadownloader.R
import com.example.socialmediadownloader.adapters.DownloadAdpeter
import com.example.socialmediadownloader.model.DownloadModel
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_download.*
import java.io.File
import java.util.*


class Download : AppCompatActivity() {


    lateinit var downloadAdapter : DownloadAdpeter
    var downloadmodels : ArrayList<DownloadModel> = ArrayList<DownloadModel>()
    lateinit var realm: Realm

    val config = RealmConfiguration.Builder()
        .allowWritesOnUiThread(true)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        realm = Realm.getInstance(config)

        val downloadmodellocals : List<DownloadModel> = getAllDownloads()
        downloadmodels.addAll(downloadmodellocals)
        downloadAdapter = DownloadAdpeter(this, downloadmodels)
        if(downloadmodellocals.size > 0){
            nodata.visibility = View.GONE
            download_list.visibility = View.VISIBLE
            delete.visibility = View.VISIBLE
            download_list.layoutManager = LinearLayoutManager(this)
            download_list.adapter = downloadAdapter
        }else{
            download_list.visibility = View.GONE
            nodata.visibility = View.VISIBLE
            delete.visibility = View.GONE

        }

        delete.setOnClickListener {
            var dialog = Confimtaio_Dialog(this)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setOnClickListener(object : Confimtaio_Dialog.OnClickListener{
                override fun onClick() {
                    realm.executeTransactionAsync(object : Realm.Transaction{
                        override fun execute(realm: Realm) {
                            realm.deleteAll()
                        }

                    }, Realm.Transaction.OnSuccess {
                        downloadmodels.clear()
                        download_list.visibility = View.GONE
                        nodata.visibility = View.VISIBLE
                        delete.visibility = View.GONE
                        dialog.dismiss()
                        Toast.makeText(this@Download, "Data Deleted!", Toast.LENGTH_LONG).show()
                    })
                }
            })
            dialog.show()
        }

        downloadAdapter.setOnClickLIstener(object : DownloadAdpeter.OnClickListener{
            override fun onClick(position: Int, model: DownloadModel) {
                val file = File(model.file_path)

                if(file.exists()){
                    val selectedUri = Uri.parse(model.file_path)
                    val intent = Intent(Intent.ACTION_VIEW)
                    if(model.file_path.contains(".mp4")){
                        intent.setDataAndType(selectedUri, "video/*")
                    }else if(model.file_path.contains(".png")){
                        intent.setDataAndType(selectedUri, "image/*")
                    }else{
                        intent.setDataAndType(selectedUri, "*/*")
                    }
                    startActivity(intent)
                }else{
                    val popup = error_dialog(this@Download,"File Deleted! or Moved to another location!")
                    popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    popup.show()
                }

            }

        })

        Log.e("data", downloadmodellocals.toString())
    }

    fun getAllDownloads() : RealmResults<DownloadModel>{
        val real = Realm.getInstance(config)
        return real.where<DownloadModel>(DownloadModel::class.java).findAll().sort("id", Sort.DESCENDING)
    }

}
