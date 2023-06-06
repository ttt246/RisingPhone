package com.matthaigh27.chatgptwrapper.services.api

import android.util.Log
import com.matthaigh27.chatgptwrapper.models.common.ContactModel
import com.matthaigh27.chatgptwrapper.models.requestmodels.RequestBodyModel
import com.matthaigh27.chatgptwrapper.models.requestmodels.RequestTrainContactModel
import com.matthaigh27.chatgptwrapper.utils.Constants
import com.matthaigh27.chatgptwrapper.utils.Constants.GET
import com.matthaigh27.chatgptwrapper.utils.Constants.POST
import com.matthaigh27.chatgptwrapper.utils.ReqType
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class HttpClient {
    /* Server URL and Api Endpoints */
    val SERVER_URL = "https://smartphone.herokuapp.com/"
    val SEND_NOTIFICATION_URL = SERVER_URL + "sendNotification"
    val IMAGE_RELATEDNESS = SERVER_URL + "image_relatedness"
    val UPLOAD_IMAGE = SERVER_URL + "uploadImage"
    val GET_ALL_HELP_COMMANDS = SERVER_URL + "commands"
    val TRAIN_CONTACTS = SERVER_URL + "train/contacts"

    var mCallback: HttpRisingInterface

    constructor(callback: HttpRisingInterface) {
        mCallback = callback
    }

    private fun sendOkHttpRequest(postBody: String, postUrl: String, method: String) {
        val body: RequestBody = RequestBody.create(Constants.JSON, postBody)

        /**
         * set okhttpclient timeout to 120s
         */
        var request: Request? = null
        if (method == POST) request = Request.Builder().url(postUrl).post(body).build()
        else request = Request.Builder().url(postUrl).get().build()

        val client =
            OkHttpClient.Builder().connectTimeout(Constants.CUSTOM_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constants.CUSTOM_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constants.CUSTOM_TIMEOUT, TimeUnit.SECONDS).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                /**
                 * Handle failure
                 */
                e.printStackTrace()

                mCallback.onFailureResult("Fail to send request to server. Please ask again.")
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse = response.body()!!.string()
                Log.d(Constants.TAG, myResponse)

                try {
                    val json = JSONObject(myResponse)["result"].toString()
                    mCallback.onSuccessResult(json)
                } catch (e: JSONException) {
                    mCallback.onFailureResult(myResponse)
                    e.printStackTrace()
                }
            }
        })
    }

    /* call sendNotification */
    fun callSendNotification(message: String) {
        sendOkHttpRequest(
            RequestBodyModel.Builder().message(message).type(ReqType.instance.MESSAGE).build()
                .buildJsonObject().toString(), SEND_NOTIFICATION_URL, POST
        )
    }

    /* call image_relatedness */
    fun callImageRelatedness(imageName: String) {
        sendOkHttpRequest(
            RequestBodyModel.Builder().imageName(imageName).type(ReqType.instance.MESSAGE).build()
                .buildJsonObject().toString(), IMAGE_RELATEDNESS, POST
        )
    }

    /* call image_upload */
    fun callImageUpload(imageName: String) {
        sendOkHttpRequest(
            RequestBodyModel.Builder().imageName(imageName).type(ReqType.instance.IMAGE_UPLOAD)
                .build().buildJsonObject().toString(), UPLOAD_IMAGE, POST
        )
    }

    fun getALlHelpPromptCommands() {
        sendOkHttpRequest(
            RequestBodyModel.Builder().build().buildJsonObject().toString(),
            GET_ALL_HELP_COMMANDS,
            GET
        )
    }

    fun trainContacts(contacts: ArrayList<ContactModel>) {
        sendOkHttpRequest(
            RequestTrainContactModel.Builder().contacts(contacts).build().buildJsonObject().toString(),
            TRAIN_CONTACTS,
            POST
        )
    }
}