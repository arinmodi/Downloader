package com.example.socialmediadownloader.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
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
        if(Build.VERSION.SDK_INT >= 30){
            val haspermission = Environment.isExternalStorageManager();
            if(haspermission){
                getStatuses();
            }else{
                requestpermissionforandroid11()
            }
        }else{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Log.e("Go to Statuses", "true")
                getStatuses()
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun requestpermissionforandroid11(){
        try{
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            intent.addCategory("android.intent.category.DEFAULT")
            intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
            startActivityForResult(intent, 2296);
        }catch(e : Exception){
            Log.e("Exception : ", e.message.toString());
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            startActivityForResult(intent, 2296)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 2296){
            if(Build.VERSION.SDK_INT >= 30){
                if(Environment.isExternalStorageManager()){
                    getStatuses();
                }else{
                    Toast.makeText(this, "We need Your Permission!", Toast.LENGTH_LONG).show();
                }
            }
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

            val WHATSAPP_STATUS_FOLDER_PATH =
                if (Build.VERSION.SDK_INT < 30) "/WhatsApp/Media/.Statuses" else "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses"


            val file = File(
                Environment.getExternalStorageDirectory().toString() + WHATSAPP_STATUS_FOLDER_PATH
            )
            Log.e("Path : ", file.toString())
            val listfiles = file.listFiles()

            if (listfiles != null) {
                for (status in listfiles) {

                    Log.e("Status ", status.name)

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
