package com.matthaigh27.chatgptwrapper.services.api

import android.util.Log
import com.google.api.Http
import com.matthaigh27.chatgptwrapper.models.RequestBodyModel
import com.matthaigh27.chatgptwrapper.utils.Consts
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

class HttpClient{
    /* Server URL and Api Endpoints */
    final val SERVER_URL = "https://chatgptphone.herokuapp.com/"
    final val SEND_NOTIFICATION_URL = SERVER_URL + "sendNotification"
    final val IMAGE_RELATEDNESS = SERVER_URL + "image_relatedness"
    final val UPLOAD_IMAGE = SERVER_URL + "uploadImage"

    lateinit var mCallback : HttpRisingInterface
    constructor(callback : HttpRisingInterface) {
        mCallback = callback
    }

    private fun sendOkHttpRequest(req: RequestBodyModel, postUrl: String) {
        val postBody = req.buidJsonObject().toString()
        val body: RequestBody = RequestBody.create(Consts.JSON, postBody)

        /**
         * set okhttpclient timeout to 120s
         */
        val request = Request.Builder().url(postUrl).post(body).build()
        val client = OkHttpClient.Builder().connectTimeout(Consts.CUSTOM_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Consts.CUSTOM_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Consts.CUSTOM_TIMEOUT, TimeUnit.SECONDS).build()

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
                Log.d(Consts.TAG, myResponse)

                try {
                    val json = JSONObject(myResponse)["result"].toString()
                    mCallback.onSuccessResult(json)
                } catch (e: JSONException) {
                    mCallback.onFailureResult("incorrect response format")
                    e.printStackTrace()
                }
            }
        })
    }

    /* call sendNotification */
    fun callSendNotification(message : String) {
        sendOkHttpRequest(RequestBodyModel.Builder().message(message).type(ReqType.instance.MESSAGE).build(), SEND_NOTIFICATION_URL)
    }

    /* call image_relatedness */
    fun callImageRelatedness(imageName : String) {
        sendOkHttpRequest(RequestBodyModel.Builder().imageName(imageName).type(ReqType.instance.MESSAGE).build(), IMAGE_RELATEDNESS)
    }

    /* call image_upload */
    fun callImageUpload(imageName : String) {
        sendOkHttpRequest(RequestBodyModel.Builder().imageName(imageName).type(ReqType.instance.IMAGE_UPLOAD).build(), UPLOAD_IMAGE)
    }
}