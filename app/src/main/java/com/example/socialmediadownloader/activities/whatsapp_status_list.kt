package com.example.socialmediadownloader.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socialmediadownloader.R
import com.example.socialmediadownloader.adapters.Staus_Adapter
import kotlinx.android.synthetic.main.activity_whatsapp_status_list.*
import java.io.File
import java.util.*

class whatsapp_status_list : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whatsapp_status_list)

        if(Build.VERSION.SDK_INT > 21){
            window.statusBarColor = resources.getColor(R.color.whatsapp_dark)
        }

        val grid = GridLayoutManager(this,2,LinearLayoutManager.VERTICAL,false)
        status_list.layoutManager = grid

        back_whatsapp.setOnClickListener {
            onBackPressed()
        }

        checkForpermission()
    }


    fun checkForpermission(){
        Log.e("Inside Permision", "true")
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Log.e("Go to Statuses", "true")
            getStatuses()
        }else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
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
                getStatuses()
            }else{
                Toast.makeText(this, "We need Your Permission!", Toast.LENGTH_LONG).show();
            }
        }
    }


    fun getStatuses(){
        val list = ArrayList<String>()

        Log.e("Inside Statuses", "true")

        try {

            var WHATSAPP_STATUS_FOLDER_PATH = "/WhatsApp/Media/.Statuses/"

            if(Build.VERSION.SDK_INT > 29){
                WHATSAPP_STATUS_FOLDER_PATH  = "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses/"
            }

            Log.e("external st: ",Environment.getExternalStorageDirectory().toString() )

            val file = File(
                Environment.getExternalStorageDirectory().toString() + WHATSAPP_STATUS_FOLDER_PATH
            )
            val listfiles = file.listFiles()

            if (listfiles != null) {
                for (status in listfiles) {

                    if (status.name.endsWith(".jpg") || status.name.endsWith(".jpeg") || status.name.endsWith(".png") || status.name.endsWith(".mp4")) {
                        val model = status.absolutePath
                        list.add(model)
                    }
                }
            }
        }catch (e : Exception){
            Log.e("Error" , e.printStackTrace().toString())
        }

        val adapter =  Staus_Adapter(this,list)
        status_list.adapter = adapter
    }


    companion object {
        const val STORAGE_PERMISSION_CODE = 10
    }

}
