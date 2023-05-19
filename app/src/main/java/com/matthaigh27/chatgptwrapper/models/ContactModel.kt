package com.matthaigh27.chatgptwrapper.models

class ContactModel {
    var id:String = ""
    var name:String = ""
    var phoneNumber:String = ""

    constructor(id:String, name:String, phoneNumber:String) {
        this.id = id
        this.name = name
        this.phoneNumber = phoneNumber
    }
}