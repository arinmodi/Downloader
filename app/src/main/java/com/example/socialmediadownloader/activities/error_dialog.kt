package com.example.socialmediadownloader.activities

import android.app.Activity
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.socialmediadownloader.R
import kotlinx.android.synthetic.main.error_dialog.*

class error_dialog(a : Activity, val message : String) : Dialog(a) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.error_dialog)

        error_message.setText(message)
    }
}
