package com.tencent.kmm.startup.std.trace

object QimeiLog {
    fun fileLog(message: String) {
        println("[Qimei] $message")
    }
}

object TuringLog {
    inline fun debug(message: () -> String) {
        println("[Turing] ${message()}")
    }

    inline fun debug(tag: String, message: () -> String) {
        println("[Turing/$tag] ${message()}")
    }

    fun fileLog(message: String) {
        println("[Turing] $message")
    }

    fun fileLog(tag: String, message: String) {
        println("[Turing/$tag] $message")
    }

    fun error(message: String, throwable: Throwable? = null) {
        println("[Turing] $message")
        throwable?.printStackTrace()
    }

    fun error(tag: String, message: String, throwable: Throwable? = null) {
        println("[Turing/$tag] $message")
        throwable?.printStackTrace()
    }
}
