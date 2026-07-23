package com.tencent.news.detekt

import com.tencent.news.build.logic.enableLocalModel
import com.tencent.news.build.logic.localProps
import com.tencent.news.build.logic.moduleProperties
import com.tencent.news.extension.libs
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/**
 * detekt 静态代码扫描
 *
 * Author: joejhzhou
 * Date: 2024/12/18
 */
fun Project.applyDetekt() {
    applyPlugin()

    detekt {
        debug = true
        disableDefaultRuleSets = true
        ignoreFailures = true
        baseline = file("$rootDir/detekt_baseline.xml")
//        source.from(sourceSets())
    }

    dependencies {
        val localLint = !localProps().getProperty("lint.dir").isNullOrEmpty()
        val detektNotation: Any = if (localLint) project(":Lint") else libs.detekt.rule
        val lintNotation: Any = if (localLint) project(":Lint") else libs.lint.rule
        add("detektPlugins", detektNotation)
        add("compileOnly", detektNotation)
        add("lintChecks", lintNotation)
        add("lintPublish", lintNotation)
        add("compileOnly", lintNotation)
    }

//    includeCommon()
}

private fun Project.applyPlugin() {
    apply(plugin = libs.plugins.detektPlugin.get().pluginId)
}

private fun Project.detekt(action: DetektExtension.() -> Unit) =
    extensions.configure<DetektExtension>(action)

private val sourceType = listOf("androidMain", "commonMain", "iosMain", "jsMain")


private fun Project.sourceSets() = sourceType.flatMap { source ->
    val kotlin = "src/$source/kotlin"
    mutableListOf(kotlin) + if (enableLocalModel()) {
        emptyList()
    } else {
        moduleProperties().map {
            val projectPath = it.value.toString().ifEmpty { "${it.key}" }
            "../${projectPath}/$kotlin"
        }
    }
}.toSet().toTypedArray()
