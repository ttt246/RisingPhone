package com.matthaigh27.chatgptwrapper.models.requestmodels

import com.matthaigh27.chatgptwrapper.MyApplication
import com.matthaigh27.chatgptwrapper.models.common.ContactModel
import com.matthaigh27.chatgptwrapper.utils.Utils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class RequestTrainContactModel {

    /** this identify request type
     * example: it will be  'message' when users send message, 'image' when users upload image
     */
    var type: String = ""
    var token: String = ""
    var contacts = JSONArray()
    var uuid: String = ""

    constructor(builder: Builder) {
        this.token = MyApplication.appContext.getFCMToken()
        this.uuid = MyApplication.appContext.getUUID()
        this.contacts = builder.contacts
        this.uuid = builder.uuid
    }

    @Throws(JSONException::class)
    fun buildJsonObject(): JSONObject {

        val jsonObject = JSONObject()
        jsonObject.accumulate("type", type)
        jsonObject.accumulate("token", token)
        jsonObject.accumulate("contacts", contacts)
        jsonObject.accumulate("uuid", uuid)

        return jsonObject
    }

    class Builder {
        var type: String = ""
        var token: String = ""
        var contacts = JSONArray()
        var uuid: String = ""

        constructor() {

        }

        constructor(request: RequestTrainContactModel) {
            this.type = request.type
            this.token = request.token
            this.uuid = request.uuid
            this.contacts = request.contacts
        }

        fun type(type: String): Builder {
            this.type = type
            return this
        }

        fun token(token: String): Builder {
            this.token = token
            return this
        }

        fun contacts(contacts: ArrayList<ContactModel>): Builder {
            this.contacts = Utils.instance.convertContactModelToJsonArray(contacts)
            return this
        }

        fun uuid(uuid: String): Builder {
            this.uuid = uuid
            return this
        }

        fun build(): RequestTrainContactModel {
            return RequestTrainContactModel(this)
        }
    }
}