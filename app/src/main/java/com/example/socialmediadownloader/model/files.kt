package com.example.socialmediadownloader.model

import java.io.File
import java.io.Serializable

data class files (
    val file_path : String,
    val file : File
) : Serializable