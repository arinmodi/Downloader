package com.example.socialmediadownloader.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.socialmediadownloader.R
import kotlinx.android.synthetic.main.activity_audio_downloader_input.*
import java.io.File

class audio_downloader_input : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_downloader_input)

        if(Build.VERSION.SDK_INT > 21){
            window.statusBarColor = resources.getColor(R.color.audio_dark)
        }

        back_audio.setOnClickListener {
            onBackPressed()
        }

        upload_button.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                getVideoFromStorage()
            }else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    GALLERY_PERMISSION_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == GALLERY_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getVideoFromStorage()
            }else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private fun getVideoFromStorage(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK){
            if(requestCode == GALLERY_CODE){
                if(data != null){
                    val uri : Uri = data.data as Uri
                    val uriString = uri.toString()
                    val myFile = File(uriString)
                    val path = myFile.absolutePath

                    if(getPath(uri).endsWith(".mkv")){
                        Toast.makeText(this,"Invalid File Format",Toast.LENGTH_LONG).show()
                    }else{
                        if(getPath(uri).length > 0){
                            val filename = path.substring(path.lastIndexOf("/") + 1)
                            val intent = Intent(this, audio_extract::class.java)
                            intent.putExtra("name", filename + "")
                            intent.putExtra("path", path)
                            intent.putExtra("uri", getPath(uri))
                            startActivity(intent)
                        }else{
                            Toast.makeText(this,"Video Not Found, may be deleted", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    fun getPath(uri: Uri) : String {
        val projevtion = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = contentResolver.query(uri, projevtion, null, null, null)
        if(cursor != null){
            try{
                val column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                cursor.moveToFirst()
                val name = cursor.getString(column_index)
                cursor.close()
                return name
            }catch (e : Exception){
                return ""
            }
        }else{
            return ""
        }
    }

    companion object {
        private val GALLERY_PERMISSION_CODE = 11
        private val GALLERY_CODE = 2
    }

}
