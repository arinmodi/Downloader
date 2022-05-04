package com.example.socialmediadownloader.activities

import android.app.Activity
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.socialmediadownloader.R
import com.example.socialmediadownloader.model.DownloadModel
import io.realm.Realm
import kotlinx.android.synthetic.main.confimtaio__dialog.*

class ConfirmationDialog(val a : Activity) : Dialog(a) {

    private var onClickListener : OnClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.confimtaio__dialog)

        cancel_button.setOnClickListener {
            dismiss()
        }

        delet_button.setOnClickListener {
            if(onClickListener != null){
                onClickListener!!.onClick()
            }
        }
    }

    interface OnClickListener {
        fun onClick()
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

}
