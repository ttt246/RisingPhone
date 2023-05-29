package com.matthaigh27.chatgptwrapper.models.common

import com.google.gson.Gson
import com.google.gson.GsonBuilder

class HelpPromptModel {
    var name: String = ""
    var description: String = ""
    var prompt: String = ""
    var tags: ArrayList<String>? = null

    override fun toString(): String {
        val gson = Gson()
        val str = gson.toJson(this)
        return str
    }

    companion object {
        fun initModelWithString(strJson: String): HelpPromptModel {
            val gson = Gson()
            val model: HelpPromptModel = gson.fromJson(strJson, HelpPromptModel::class.java)
            return model
        }
    }
}