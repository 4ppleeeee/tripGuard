package com.tencent.news.publish.tasks

import com.tencent.news.extension.baseName
import com.tencent.news.extension.cocoapods
import com.tencent.news.extension.iOSArtifactOutputDir
import com.tencent.news.extension.kotlinExtension
import com.tencent.news.extension.qnPublishingExtension
import com.tencent.news.utils.procRun
import org.gradle.api.Project
import java.io.File

/**
 * podsSpec配置
 */
data class CocoaPodsSpec(
    val source: String,
    val version: String,
    val name: String,
    val homepage: String,
    val author: String,
    val license: String
)

/**
 * 上传podSpec到指定的源
 */
fun Project.uploadCocoaPodSpec(config: CocoaPodsSpec, buildType: String) {
    val podSpecFile = file("${iOSArtifactOutputDir}/${buildType}/${baseName}.podspec")
    if (generateCocoaPodsSpecFile(podSpecFile, config)) {
        publishCocoaPodsSpecFile(podSpecFile.absolutePath)
    }
}

/**
 * 生成podSpec文件到指定目录
 */
private fun Project.generateCocoaPodsSpecFile(podSpecFile: File, config: CocoaPodsSpec) =
    with(kotlinExtension.cocoapods) {
        val deploymentTargets = run {
            listOf(ios, osx, tvos, watchos).filter { it.deploymentTarget != null }
                .joinToString("\n") {
                    if (extraSpecAttributes.containsKey("${it.name}.deployment_target")) {
                        ""
                    } else {
                        "|    spec.${it.name}.deployment_target = '${it.deploymentTarget}'"
                    }
                }
        }

        val dependencies = pods.joinToString(separator = "\n") { pod ->
            val versionSuffix = if (pod.version != null) {
                ", '${pod.version}'"
            } else {
                ""
            }
            "|    spec.dependency '${pod.name}'$versionSuffix"
        }

        val vendoredFramework = "${baseName}.xcframework"
        val vendoredFrameworks = if (extraSpecAttributes.containsKey("vendored_frameworks")) {
            ""
        } else {
            "|    spec.vendored_frameworks      = '$vendoredFramework'"
        }

        val libraries = if (extraSpecAttributes.containsKey("libraries")) {
            ""
        } else {
            "|    spec.libraries                = 'c++'"
        }

        val customSpec = extraSpecAttributes.map { "|    spec.${it.key} = ${it.value}" }.joinToString("\n")

        podSpecFile.writeText(
            """
            |Pod::Spec.new do |spec|
            |    spec.name                     = '${config.name}'
            |    spec.version                  = '${config.version}'
            |    spec.homepage                 = '${config.homepage}'
            |    spec.source                   = { 
            |                                      :http => '${config.source}',
            |                                      :type => 'zip'
            |                                    }
            |    spec.authors                  = '${config.author}'
            |    spec.license                  = '${config.license}'
            |    spec.summary                  = '${summary.orEmpty()}'
            |    spec.dependency 'KuiklyIOSRender', '>= 1.1.87-2.0.21'
            $vendoredFrameworks
            $libraries
            $deploymentTargets
            $dependencies
            $customSpec
            |end
        """.trimMargin()
        )
        true
    }


private fun Project.publishCocoaPodsSpecFile(podSpecFilePath: String) {
    val extras = mutableListOf<String>()
    extras.add("--allow-warnings")
    extras.add("--verbose")
    extras.add("--skip-import-validation")  // 跳过导入验证，避免架构检查
    extras.add("--skip-tests")              // 跳过测试验证
    extras.add("--sources=https://git.woa.com/T-CocoaPods/Specs.git")
    var cocoaPodSpecUrl = qnPublishingExtension.cocoapods
    procRun(
        "pod",
        "repo",
        "push",
        cocoaPodSpecUrl,
        podSpecFilePath,
        *extras.toTypedArray()
    )
}