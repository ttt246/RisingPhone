package com.matthaigh27.chatgptwrapper.data.models.common

class HelpCommandModel {
    var mainCommandName: String = ""
    var assistCommandName: String = ""

    fun isMainCommand(): Boolean {
        return mainCommandName != "help"
    }
}