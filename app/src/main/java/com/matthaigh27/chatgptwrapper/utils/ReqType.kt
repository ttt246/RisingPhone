package com.matthaigh27.chatgptwrapper.utils

class ReqType {
    val MESSAGE = "message"
    val IMAGE_UPLOAD = "image_upload"

    companion object {
        var instance: ReqType = ReqType()
    }
}