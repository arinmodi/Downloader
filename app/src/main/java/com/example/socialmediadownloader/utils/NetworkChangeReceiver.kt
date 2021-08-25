package com.example.socialmediadownloader.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

open class NetworkChangeReceiver(private val listener : NetworkChangeListener) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.action)){
                val coonectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork = coonectivityManager.activeNetworkInfo

                val connected = activeNetwork != null && activeNetwork.isConnected
                listener.onNetworkConnectedStateChanged(connected)
            }

        }
    }

    interface NetworkChangeListener {
        fun onNetworkConnectedStateChanged(connected: Boolean)
    }

}