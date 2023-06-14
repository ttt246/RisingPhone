package com.matthaigh27.chatgptwrapper.data.models.common

import android.net.Uri

class ImagePromptModel {
    var uri: Uri? = null
    var dateModified: Long = -1L

    constructor(uri: Uri, date: Long) {
        this.uri = uri
        this.dateModified = date
    }
}