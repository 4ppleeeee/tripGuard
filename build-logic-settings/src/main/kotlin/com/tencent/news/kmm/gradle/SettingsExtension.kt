package com.tencent.news.kmm.gradle

import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.DynamicObjectAware
import org.gradle.api.internal.plugins.DslObject
import java.io.File

fun Settings.includeQnKmmModules(rootDir: File) {
    include(rootDir, "umbrella", "")
    moduleProperties().forEach { name, path ->
        val projectName = name.toString()
        val projectPath = path.toString()
        include(rootDir, projectName, projectPath)
    }
}

private fun Settings.include(rootDir: File, name: String, path: String) {
    include(":$name")
    if (path.isNotEmpty()) {
        project(":$name").projectDir = File(rootDir, path)
    } else {
        project(":$name").projectDir = File(rootDir, name)
    }
}

fun Settings.localProps(): java.util.Properties {
    return loadProperties("local.properties")
}

fun Settings.moduleProperties(): java.util.Properties {
    return loadProperties("modules.properties")
}

fun Settings.loadProperties(fileObj: Any): java.util.Properties {
    val props = java.util.Properties()
    val file: File = when (fileObj) {
        is File -> fileObj
        else -> File(rootDir, fileObj.toString())
    }
    if (file.exists()) {
        props.load(java.io.FileReader(file))
    }
    return props
}

fun Settings.isAndroidBuild(): Boolean {
    println("build.platform: ${findProperty("build.platform")}")
    return findProperty("build.platform") == "android"
}

fun Settings.findProperty(key: String): String? {
    val result =
        (this as? DynamicObjectAware ?: DslObject(this)).asDynamicObject.tryGetProperty(key)
    return if (result.isFound) {
        result.value
    } else {
        localProps().getProperty(key)
    }?.toString()
}
