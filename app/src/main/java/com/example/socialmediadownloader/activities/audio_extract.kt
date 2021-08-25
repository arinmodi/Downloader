package com.example.socialmediadownloader.activities


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.socialmediadownloader.R
import com.example.socialmediadownloader.utils.functions
import kotlinx.android.synthetic.main.activity_audio_extract.*
import kotlinx.android.synthetic.main.activity_extrcation__dialog.*
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.jvm.Throws
import android.media.MediaCodec

import android.media.MediaExtractor

import android.media.MediaMetadataRetriever

import android.media.MediaFormat

import android.media.MediaMuxer
import android.view.View
import java.nio.ByteBuffer


class audio_extract : AppCompatActivity() {

    lateinit var videostoragepath: String
    var file_name = ""
    var totalDur = 0
    lateinit var extraction_dialog: Extrcation_Dialog
    var dest = ""
    var videolenghtInSec: Long = 0
    var loop = true

    private val DEFAULT_BUFFER_SIZE = 1 * 1024 * 1024
    private val TAG = "AudioExtractorDecoder"

    private fun getVideoLength(uri: Uri) {
        val mp = MediaPlayer.create(this@audio_extract, uri)
        totalDur = mp.duration
        videolenghtInSec = TimeUnit.MICROSECONDS.toSeconds(mp.duration.toLong())
        mp.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_extract)

        if (Build.VERSION.SDK_INT > 21) {
            window.statusBarColor = resources.getColor(R.color.audio_dark)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Files Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        back_audio_preview.setOnClickListener {
            onBackPressed()
        }

        file_name = intent.getStringExtra("name").toString()
        val path = intent.getStringExtra("path")
        videostoragepath = intent.getStringExtra("uri").toString()


        extraction_dialog = Extrcation_Dialog(this)
        extraction_dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        extraction_dialog.setCancelable(false)

        getVideoLength(Uri.parse(videostoragepath))
        duration.setText(functions().millisecondsToHumanReadble(totalDur))

        Glide.with(this)
            .asBitmap()
            .placeholder(R.drawable.placeholder)
            .load(videostoragepath)
            .into(thumbnail_video_audio)

        filename.setText((file_name + ".mp4"))
        extract_button.setOnClickListener {
            if(totalDur > 1500000){
                Toast.makeText(this,"Video Length too long", Toast.LENGTH_LONG).show()
            }else{
                val isAudio = doesVideoHaveAudioTrack(videostoragepath)
                Log.e("audio", isAudio.toString())
                if(isAudio == true){
                    checkForStoragePermission()
                }else{
                    Toast.makeText(this,"Video does not contain audio", Toast.LENGTH_LONG).show()
                }
            }
        }

        audio_video_play.setOnClickListener {
            val intent = Intent(this, video_play_activity::class.java)
            intent.putExtra("header", R.color.audio_light)
            intent.putExtra("isuri", false)
            intent.putExtra("path", videostoragepath)
            intent.putExtra("toolbar", R.color.audio_dark)
            startActivity(intent)
        }

    }

    private fun checkForStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            extractAudio()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 8
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 8) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                extractAudio()
            } else {
                Toast.makeText(this, "We need Your Permission!", Toast.LENGTH_LONG).show();
            }
        }
    }

    fun extractAudio() {
        var myFolderPath = Environment.getExternalStoragePublicDirectory("DL- All In One Downloader/Audio").absolutePath.toString()
        if(Build.VERSION.SDK_INT > 29){
            myFolderPath = "/storage/emulated/0/" + Environment.DIRECTORY_DOWNLOADS.toString() + "/DL- All In One Downloader/Audio"
        }
        dest = (myFolderPath + "/" + file_name + ".mp3")
        Log.e("Folder path = ", myFolderPath)
        val folder = File(myFolderPath)
        extraction_dialog.show()
        if (!folder.exists()) {
            try {
                val status = folder.mkdirs()
                Log.e("Status : ", "Folder Created $status")
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            Log.e("Status : ", "Folder Exist")
        }

        ExtractionTask().execute(arrayOf(""))

    }

    inner class ExtractionTask : AsyncTask<Array<String>, Void, String>() {


        @Throws(IOException::class)
        override fun doInBackground(vararg params: Array<String>?): String {

            val srcPath = videostoragepath
            val dstPath = dest
            val startMs = -1
            val endMs = -1
            val useAudio = true
            val useVideo = false

            Log.e("Total Duration", totalDur.toString())

            val extractor = MediaExtractor()
            extractor.setDataSource(srcPath)
            val trackCount: Int = extractor.getTrackCount()
            Log.e("track count : ", trackCount.toString())
            val muxer = MediaMuxer(dstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val indexMap = HashMap<Int, Int>(trackCount)
            var bufferSize = -1

            for (i in 0 until trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                var selectCurrentTrack = false
                if (mime != null) {
                    if (mime.startsWith("audio/") && useAudio) {
                        selectCurrentTrack = true
                    } else if (mime.startsWith("video/") && useVideo) {
                        selectCurrentTrack = true
                    }
                }
                if (selectCurrentTrack) {
                    extractor.selectTrack(i)
                    val dstIndex = muxer.addTrack(format)
                    indexMap[i] = dstIndex
                    if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                        val newSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                        bufferSize = if (newSize > bufferSize) newSize else bufferSize
                    }
                }
            }

            if (bufferSize < 0) {
                bufferSize = DEFAULT_BUFFER_SIZE
            }

            val retrieverSrc = MediaMetadataRetriever()
            val degreesString = retrieverSrc.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            if (degreesString != null) {
                val degrees = degreesString.toInt()
                if (degrees >= 0) {
                    muxer.setOrientationHint(degrees)
                }
            }
            if (startMs > 0) {
                extractor.seekTo((startMs * 1000).toLong(), MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            }

            val offset = 0
            var trackIndex = -1
            val dstBuf: ByteBuffer = ByteBuffer.allocate(bufferSize)
            val bufferInfo = MediaCodec.BufferInfo()
            Log.e("Buffer Size : ", bufferSize.toString())
            muxer.start()
            var extractedbuffer = 0
            while (true) {
                bufferInfo.offset = offset
                bufferInfo.size = extractor.readSampleData(dstBuf, offset)
                extractedbuffer += 1
                if (bufferInfo.size < 0) {
                    Log.e(TAG, "Saw input EOS.")
                    bufferInfo.size = 0
                    break
                } else {
                    bufferInfo.presentationTimeUs = extractor.sampleTime
                    if (endMs > 0 && bufferInfo.presentationTimeUs > endMs * 1000) {
                        Log.e(TAG, "The current sample is over the trim end time.")
                        break
                    } else {
                        bufferInfo.flags = extractor.sampleFlags
                        trackIndex = extractor.sampleTrackIndex
                        muxer.writeSampleData(indexMap[trackIndex]!!, dstBuf, bufferInfo)
                        extractor.advance()
                    }
                }
            }

            Log.e("Final Buffers: ", extractedbuffer.toString())

            muxer.stop()
            muxer.release()

            runOnUiThread {
                extraction_dialog.dismiss()
                Toast.makeText(this@audio_extract, "Extraction Complted", Toast.LENGTH_LONG).show()
                val selectedUri = Uri.parse(dest)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(selectedUri, "audio/*")
                val contentintent = PendingIntent.getActivity(
                    this@audio_extract,
                    0,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
                val notification = NotificationCompat.Builder(this@audio_extract, CHANNEL_ID)
                notification.setContentTitle("DL-All In One Downloader")
                notification.setContentText("Audio is sucessfully Extracted from your file")
                notification.setSmallIcon(R.drawable.download)
                if (Build.VERSION.SDK_INT > 23) {
                    notification.setColor(getColor(R.color.colorPrimary))
                }
                notification.setPriority(NotificationCompat.PRIORITY_DEFAULT)
                notification.setAutoCancel(true)
                notification.setContentIntent(contentintent)

                val managerCompat = NotificationManagerCompat.from(this@audio_extract)
                managerCompat.notify(1, notification.build())
                finish()
            }

            return ""

        }

    }


    override fun onDestroy() {
        super.onDestroy()
    }


    companion object {
        val CHANNEL_ID = "MYCHANNEL"
    }

    private fun doesVideoHaveAudioTrack(path: String): Boolean {
        val audioTrack: Boolean
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val hasAudioStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)
        audioTrack = hasAudioStr != null
        return audioTrack
    }

}


    /*

                        Log.e("S : ", message + command)
                    Toast.makeText(this@audio_extract, "Extraction Complted", Toast.LENGTH_LONG)
                        .show()
                    val selectedUri = Uri.parse(dest)
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(selectedUri, "audio/*")
                    val contentintent = PendingIntent.getActivity(
                        this@audio_extract,
                        0,
                        intent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                    )
                    val notification = NotificationCompat.Builder(this@audio_extract, CHANNEL_ID)
                    notification.setContentTitle("DL-All In One Downloader")
                    notification.setContentText("Audio is sucessfully Extracted from your file")
                    notification.setSmallIcon(R.drawable.download)
                    if (Build.VERSION.SDK_INT > 23) {
                        notification.setColor(getColor(R.color.colorPrimary))
                    }
                    notification.setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    notification.setAutoCancel(true)
                    notification.setContentIntent(contentintent)

                    val managerCompat = NotificationManagerCompat.from(this@audio_extract)
                    managerCompat.notify(1, notification.build())
                    extraction_dialog.dismiss()
                    finish()


     */


     */


