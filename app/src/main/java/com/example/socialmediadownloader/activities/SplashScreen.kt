package com.example.socialmediadownloader.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmediadownloader.R


@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {

    private val SPLASH_DISPLAY_LENGTH = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spalsh_screen)

        if(Build.VERSION.SDK_INT > 21){
            window.statusBarColor = resources.getColor(R.color.colorPrimaryDark)
        }

        Handler().postDelayed(Runnable {
            val mainIntent = Intent(this,MainActivity::class.java)
            startActivity(mainIntent)
            finish()
        }, SPLASH_DISPLAY_LENGTH.toLong())
    }
}
