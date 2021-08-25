package com.example.socialmediadownloader.utils

import android.util.Log
import java.sql.Time
import java.util.concurrent.TimeUnit

class functions {

    fun bytesIntoHumanReadable(bytes: Long): String? {
        val kilobyte: Long = 1024
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024
        val terabyte = gigabyte * 1024
        return if (bytes >= 0 && bytes < kilobyte) {
            "$bytes B"
        } else if (bytes >= kilobyte && bytes < megabyte) {
            (bytes / kilobyte).toString() + " KB"
        } else if (bytes >= megabyte && bytes < gigabyte) {
            (bytes / megabyte).toString() + " MB"
        } else if (bytes >= gigabyte && bytes < terabyte) {
            (bytes / gigabyte).toString() + " GB"
        } else if (bytes >= terabyte) {
            (bytes / terabyte).toString() + " TB"
        } else {
            "0KB"
        }
    }

    fun millisecondsToHumanReadble(millis : Int) : String {
        var seconds : Long = 1000
        var min : Long = 60000
        var hours : Long = min * 60

        val milliseconds = millis.toLong()

        Log.e("Milliseconds : ", milliseconds.toString())

        return if (milliseconds >= 0 && milliseconds < seconds) {
            "$milliseconds mil"
        } else if (milliseconds >= seconds && milliseconds < min) {
            TimeUnit.MILLISECONDS.toSeconds(millis.toLong()).toString() + " SEC"
        } else if (milliseconds >= min && milliseconds <= hours) {
            val mins = TimeUnit.MILLISECONDS.toMinutes(millis.toLong())
            val totalseconds = TimeUnit.MILLISECONDS.toSeconds(millis.toLong())
            val remseconds = totalseconds - (mins * 60)
            var finalmins  = ""
            if(remseconds <= 0){
                finalmins = mins.toString() + ":00"
            }else if(remseconds <= 9){
                finalmins = mins.toString() + ":0" + remseconds
            }else{
                finalmins = mins.toString() + ":" + remseconds
            }
            finalmins + " MIN"
        } else if (milliseconds >= hours) {
            val Hours = TimeUnit.MILLISECONDS.toHours(millis.toLong())
            val totalmins = TimeUnit.MILLISECONDS.toMinutes(millis.toLong())
            val totalSecs = TimeUnit.MILLISECONDS.toSeconds(millis.toLong())
            val remmins = totalmins - hours*60
            val remsecs = totalSecs - totalmins*60

            var finalhour = ""
            var finalmins = ""
            var finalsecs = ""

            if(Hours > 0 && Hours < 10){
                finalhour = "0" + finalhour
            }else{
                finalhour = finalhour
            }

            if(remmins > 0 && remmins < 10){
                finalmins = "0" + finalmins
            }else if(remmins <= 0){
                finalmins = "00"
            }else{
                finalmins = finalmins
            }

            if(remsecs > 0 && remsecs < 10){
                finalsecs = "0" + finalsecs
            }else if(remsecs <= 0){
                finalsecs = "00"
            }else{
                finalsecs = finalsecs
            }

             finalhour + ":" + finalmins + ":" + finalsecs + " HOUR"
        } else {
            "$milliseconds mil"
        }

    }

}