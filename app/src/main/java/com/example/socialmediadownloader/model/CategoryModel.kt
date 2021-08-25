package com.example.socialmediadownloader.model

import java.io.Serializable

data class CategoryModel (
    val cat_icon : Int,
    val cat_name : String,
    val cat_download_icon : Int,
    val tool_bar: Int,
    val header : Int,
    val header_icon : Int,
    val hint : String,
    val input_title : String
) : Serializable
