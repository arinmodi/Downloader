package com.example.socialmediadownloader.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class DownloadModel : RealmObject() {

    @PrimaryKey
    var id : Long = 0
        get() = field
        set(value) {
            field = value
        }

    var downloadId : Long = 0
        get() = field
        set(value) {
            field = value
        }

    var title : String = ""
        get() = field
        set(value) {
            field = value
        }

    var file_path : String =""
        get() = field
        set(value) {
            field = value
        }

    var progress : String = ""
        get() = field
        set(value) {
            field = value
        }

    var status : String =""
        get() = field
        set(value) {
            field = value
        }

    var file_size : String =""
        get() = field
        set(value) {
            field = value
        }

    var is_paused : Boolean = false
        get() = field
        set(value) {
            field = value
        }

    var total_size : String = ""
        get() = field
        set(value) {
            field = value
        }

    var platfrom : Int = 0
        get() = field
        set(value) {
            field = value
        }

    var date : String = ""
        get() = field
        set(value) {
            field = value
        }

}