package com.matthaigh27.chatgptwrapper.models

/**
 * ChatModel for Chat RecyclerView
 */
class ChatModel {
    var message: String = ""
    var isMe: Boolean = true
    var image: ByteArray? = null
    var imageName: String = ""
    var visibleFeedback = false
    var feedback = 0
    var isWidget: Boolean = false
    var widgetType = ""
}
