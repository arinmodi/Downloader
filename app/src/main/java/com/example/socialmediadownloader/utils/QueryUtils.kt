package com.example.android.quakereport

import android.text.TextUtils
import android.util.Log
import com.example.socialmediadownloader.activities.instagram_preview
import com.example.socialmediadownloader.model.InstaModel
import com.example.socialmediadownloader.utils.functions
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import java.util.*


class QueryUtils {
    private val LOG_TAG: String = instagram_preview::class.java.getName()


    fun fetchInstaPostData(requestUrl: String): List<InstaModel>? {

        val url = createUrl(requestUrl)

        var jsonResponse: String? = null
        try {
            jsonResponse = makeHTTpRequest(url)
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e)
        }

        return instapost(jsonResponse)
    }

    @Throws(IOException::class)
    private fun makeHTTpRequest(url: URL?): String {
        var jsonresponce = ""
        if (url == null) {
            return jsonresponce
        }
        var urlConnection: HttpURLConnection? = null
        var `is`: InputStream? = null
        try {
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.readTimeout = 10000
            urlConnection.connectTimeout = 15000
            urlConnection.requestMethod = "GET"
            urlConnection.connect()
            if (urlConnection.responseCode == 200) {
                `is` = urlConnection.inputStream
                jsonresponce = readFromStream(`is`)
            } else {
                Log.e(LOG_TAG, "Error Responce Code: " + urlConnection.responseCode)
            }
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e)
        } finally {
            urlConnection?.disconnect()
            `is`?.close()
        }
        return jsonresponce
    }

    @Throws(IOException::class)
    private fun readFromStream(`is`: InputStream?): String {
        val output = StringBuilder()
        if (`is` != null) {
            val isr = InputStreamReader(`is`)
            val reader = BufferedReader(isr)
            var line = reader.readLine()
            while (line != null) {
                output.append(line)
                line = reader.readLine()
            }
        }
        return output.toString()
    }


    private fun createUrl(stringUrl: String): URL? {
        var url: URL? = null
        try {
            url = URL(stringUrl)
        } catch (e: MalformedURLException) {
            Log.e(LOG_TAG, "Problem building the URL ", e)
        }
        return url
    }


    fun instapost(postjson: String?): ArrayList<InstaModel>? {
        if (TextUtils.isEmpty(postjson)) {
            return null
        }

        val postdata : ArrayList<InstaModel> = ArrayList<InstaModel>()


        try {

            val data = JSONObject(postjson)
            val graphql = data.getJSONObject("graphql")
            Log.e(LOG_TAG, graphql.toString())
            val shortcode_media = graphql.getJSONObject("shortcode_media")
            Log.e(LOG_TAG, shortcode_media.toString())
            val owner = shortcode_media.getJSONObject("owner")
            Log.e(LOG_TAG, owner.toString())
            val user_pic = owner.getString("profile_pic_url")
            val user_name = owner.getString("username")
            if(shortcode_media.has("edge_sidecar_to_children")){
                val posts_ref = shortcode_media.getJSONObject("edge_sidecar_to_children")
                val posts = posts_ref.getJSONArray("edges")
                for (i in 0..posts.length() - 1) {
                    val f = posts.getJSONObject(i)
                    val c = f.getJSONObject("node")
                    val downloadurl = c.getString("display_url")
                    val is_video = c.getBoolean("is_video")
                    var url = URL(downloadurl)
                    var video_url = ""
                    if(is_video){
                        video_url = c.getString("video_url")
                        url = URL(video_url)
                    }
                    val urlConnection: URLConnection = url.openConnection()
                    urlConnection.connect()
                    val file_size : Long = ((urlConnection.contentLength) as Number).toLong()
                    val downloadsize = functions().bytesIntoHumanReadable(file_size) + ""
                    var product_type = "";
                    if(shortcode_media.has("product_type")){
                        product_type = shortcode_media.getString("product_type")
                    }else{
                        product_type = "not-reel"
                    }

                    val id = c.getString("id")
                    var post = InstaModel(downloadurl,id, is_video, user_pic, user_name,downloadsize,downloadurl,product_type)
                    if(is_video){
                        post = InstaModel(video_url,id, is_video, user_pic, user_name,downloadsize,downloadurl,product_type)
                    }
                    postdata.add(post)
                }
            }else{
                val downloadurl = shortcode_media.getString("display_url")
                val is_video = shortcode_media.getBoolean("is_video")
                var url = URL(downloadurl)
                var video_url = ""
                if(is_video){
                    video_url = shortcode_media.getString("video_url")
                    url = URL(video_url)
                }
                val urlConnection: URLConnection = url.openConnection()
                urlConnection.connect()
                val file_size : Long = ((urlConnection.contentLength) as Number).toLong()
                val downloadsize = functions().bytesIntoHumanReadable(file_size) + ""
                var product_type = "";
                if(shortcode_media.has("product_type")){
                    product_type = shortcode_media.getString("product_type")
                }else{
                    product_type = "not-reel"
                }
                val id = shortcode_media.getString("id")
                var post = InstaModel(downloadurl,id, is_video, user_pic, user_name,downloadsize,downloadurl,product_type)
                if(is_video){
                    post = InstaModel(video_url,id, is_video, user_pic, user_name,downloadsize,downloadurl,product_type)
                }
                postdata.add(post)
            }
        } catch (e: JSONException) {
            Log.e("QueryUtils", "Problem parsing the Post JSON results", e)
        }

        return postdata
    }
}