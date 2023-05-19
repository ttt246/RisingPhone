package com.matthaigh27.chatgptwrapper.services.api;

interface HttpRisingInterface {
    fun onSuccessResult(msg: String)

    fun onFailureResult(msg: String)
}
