package com.example.socialmediadownloader.activities

import android.app.Activity
import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.socialmediadownloader.R
import kotlinx.android.synthetic.main.activity_extrcation__dialog.*

class Extrcation_Dialog(val a : Context) : Dialog(a) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extrcation__dialog)

        extraction_Progress.visibility = View.VISIBLE
    }
}
