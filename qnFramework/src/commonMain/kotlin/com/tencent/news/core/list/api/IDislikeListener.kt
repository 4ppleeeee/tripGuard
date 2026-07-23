package com.tencent.news.core.list.api

interface IDislikeListener {
    fun onDislike() {}
    fun onComplain() {}
    fun onCancel() {}
}