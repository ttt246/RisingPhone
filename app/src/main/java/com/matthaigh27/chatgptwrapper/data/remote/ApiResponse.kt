package com.matthaigh27.chatgptwrapper.data.remote;

interface ApiResponse {
    fun onSuccessResult(msg: String)

    fun onFailureResult(msg: String)
}
