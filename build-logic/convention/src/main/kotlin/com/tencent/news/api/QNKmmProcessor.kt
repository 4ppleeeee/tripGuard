package com.tencent.news.api

import org.gradle.api.Project

interface QNKmmProcessor {
    val project: Project

    fun doAfterProjectEvaluated()
}