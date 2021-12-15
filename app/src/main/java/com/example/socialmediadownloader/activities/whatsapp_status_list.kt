package com.example.socialmediadownloader.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socialmediadownloader.R
import kotlinx.android.synthetic.main.activity_whatsapp_status_list.*
import android.app.Activity
import android.content.Context

import android.content.Intent
import android.net.Uri
import android.os.Environment

import android.os.storage.StorageManager
import android.util.Log
import android.widget.Toast

import androidx.annotation.RequiresApi
import com.example.socialmediadownloader.adapters.Staus_Adapter
import java.io.File
import android.content.UriPermission

import androidx.documentfile.provider.DocumentFile
import com.example.socialmediadownloader.model.files

class whatsapp_status_list : AppCompatActivity() {

    private val TAG = "Status List: "
    private val REQUEST_ACTION_OPEN_DOCUMENT_TREE = 12
    private val STATUS_DIRECTORY_NEW  = "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fcom.whatsapp%2FWhatsApp"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whatsapp_status_list)

        if (Build.VERSION.SDK_INT > 21) {
            window.statusBarColor = ContextCompat.getColor(this,R.color.whatsapp_dark);
        }

        val grid = GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false)
        status_list.layoutManager = grid

        back_whatsapp.setOnClickListener {
            onBackPressed()
        }

        checkForpermission()

    }


    fun checkForpermission() {
        val permissions: List<UriPermission> = contentResolver.persistedUriPermissions
        Log.e("Size of permission : ", permissions.size.toString());
        if(Build.VERSION.SDK_INT >= 30){
            if(permissions.size == 1){
                if(permissions[0].uri.toString().contains("com.whatsapp")){
                    // has Whtasapp Permission
                    getStatus11()
                }
            } else if(permissions.size > 1){
                contentResolver.persistedUriPermissions.clear()
                getPermissionQAbove()
                getStatus11()
            }else{
                getPermissionQAbove()
                getStatus11()
            }
        }else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                getStatuses()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_ACTION_OPEN_DOCUMENT_TREE) {
            println("HEY")
            if (data == null) return
            val uri: Uri = data.data ?: return
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
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
                getStatuses()
            }else{
                Toast.makeText(this, "We need Your Permission!", Toast.LENGTH_LONG).show();
            }
        }
    }

    fun getStatus11(){
        val uri = Uri.parse(STATUS_DIRECTORY_NEW)
        val doc = DocumentFile.fromTreeUri(this, uri)
        val list = ArrayList<files>()

        val doc2 = doc?.findFile("Media")
        val doc3  = doc2?.findFile(".Statuses")

        val statusFiles = doc3?.listFiles()

        if(statusFiles != null) {
            for(status in statusFiles){
                if (status.name!!.endsWith(".jpg") || status.name!!.endsWith(".jpeg") || status.name!!.endsWith(".png") || status.name!!.endsWith(".mp4")) {
                    val model = status
                    list.add(files(model.uri.toString(),File(model.uri.path.toString())))
                }
            }
        }

        val adapter =  Staus_Adapter(this,list)
        status_list.adapter = adapter
    }


    fun getStatuses(){
        val list = ArrayList<files>()

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
                        list.add(files(status.absolutePath,File(status.absolutePath)));
                    }
                }
            }
        }catch (e : Exception){
            Log.e("Error" , e.printStackTrace().toString())
        }

        val adapter =  Staus_Adapter(this,list)
        status_list.adapter = adapter
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun getPermissionQAbove() {
        val sm = applicationContext.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val intent = sm.primaryStorageVolume.createOpenDocumentTreeIntent()
        val uri: Uri = getUri()
        intent.putExtra("android.provider.extra.INITIAL_URI", uri)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        Log.d(TAG, "uri: " + uri.toString())
        (this@whatsapp_status_list as Activity).startActivityForResult(
            intent,
            REQUEST_ACTION_OPEN_DOCUMENT_TREE
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun getUri(): Uri {
        val sm = applicationContext.getSystemService(STORAGE_SERVICE) as StorageManager
        val intent = sm.primaryStorageVolume.createOpenDocumentTreeIntent()
        var startDir  = "Android/media/com.whatsapp/WhatsApp"
        val uri: Uri? = intent.getParcelableExtra("android.provider.extra.INITIAL_URI")
        var scheme: String = uri.toString()
        Log.d(TAG, "INITIAL_URI scheme: $scheme")
        scheme = scheme.replace("/root/", "/document/")
        startDir = startDir.replace("/", "%2F")
        scheme += "%3A$startDir"
        return Uri.parse(scheme)
    }


    companion object {
        const val STORAGE_PERMISSION_CODE = 10
    }

}
