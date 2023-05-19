package com.matthaigh27.chatgptwrapper.models

import com.matthaigh27.chatgptwrapper.MyApplication
import org.json.JSONException
import org.json.JSONObject

class RequestBodyModel {

    /** this identify request type
     * example: it will be  'message' when users send message, 'image' when users upload image
     */
    var type: String = ""
    var token: String = ""
    var message: String = ""
    var imageName: String = ""
    var uuid: String = ""

    constructor(builder : Builder) {
        this.token = MyApplication.appContext.getFCMToken()
        this.uuid = MyApplication.appContext.getUUID()
        this.message = builder.message
        this.imageName = builder.imageName
        this.uuid = builder.uuid
    }


    @Throws(JSONException::class)
    fun buidJsonObject(): JSONObject {

        val jsonObject = JSONObject()
        jsonObject.accumulate("type", type)
        jsonObject.accumulate("token", token)
        jsonObject.accumulate("message", message)
        jsonObject.accumulate("image_name", imageName)
        jsonObject.accumulate("uuid", uuid)

        return jsonObject
    }

    class Builder {
        var type: String = ""
        var token: String = ""
        var message: String = ""
        var imageName: String = ""
        var uuid: String = ""
        constructor() {

        }
        constructor(request : RequestBodyModel) {
            this.type = request.type
            this.token = request.token
            this.message = request.message
            this.imageName = request.imageName
            this.uuid = request.uuid
        }

        fun type(type : String) : Builder {
            this.type = type
            return this
        }

        fun token(token : String) : Builder {
            this.token = token
            return this
        }

        fun message(message : String) : Builder {
            this.message = message
            return this
        }

        fun imageName(imageName : String) : Builder {
            this.imageName = imageName
            return this
        }

        fun uuid(uuid : String) : Builder {
            this.uuid = uuid
            return this
        }

        fun build() : RequestBodyModel {
            return RequestBodyModel(this)
        }
    }
}