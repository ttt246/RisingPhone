package com.matthaigh27.chatgptwrapper.models.common

class ContactModel {
    var id: String = ""
    var name: String = ""
    var phoneList: ArrayList<String>? = null
    var status: String = ""

    init {
        phoneList = ArrayList()
    }

    fun setData(id: String, name: String, phoneList: ArrayList<String>, status: String) {
        this.id = id
        this.name = name
        this.phoneList = phoneList
        this.status = status
    }
}