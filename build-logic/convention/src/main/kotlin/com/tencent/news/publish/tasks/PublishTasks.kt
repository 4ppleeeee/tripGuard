package com.tencent.news.publish.tasks

import com.tencent.news.extension.QNKMM_TASK_GROUP
import com.tencent.news.extension.cocoapods
import com.tencent.news.extension.getMavenArtifactVersion
import com.tencent.news.extension.kotlinExtension
import com.tencent.news.extension.qnPublishingExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip

/**
 * 注册publishAndroidArtifact任务，会给[variants]对应的每个产物都注册对应的发布任务，任务执行顺序如下：
 * bundleReleaseAar -> copyAndroidReleaseAar -> publishAndroidArtifact
 */
internal fun Project.registerPublishAndroidArtifactTask(variants: List<String>): TaskProvider<Task> {
    val publishTask = tasks.register("publishQnAndroidArtifact") {
        group = QNKMM_TASK_GROUP
        description = "Publish the Android AAR Output of the Kotlin Multiplatform To Maven Repository"
    }

    variants.forEach { variant ->
        registerCopyAndroidAarTask(variant)
        val publishMavenTasks = configAndroidKMMPublish(variant)
        publishMavenTasks.forEach { pt ->
            publishTask.configure {
                dependsOn(pt)
            }
        }
    }
    return publishTask
}

/**
 * 注册publishIOSArtifact任务，会给[variants]对应的每个产物都注册对应的发布任务，任务执行顺序如下：
 * podPublishReleaseXCFramework -> zipReleaseXCFramework -> copyReleaseXCFramework ->
 * uploadReleaseXcFramework -> publishReleaseXcFramework -> publishXCFramework
 */
internal fun Project.registerIosPublishArtifactTask(variants: List<String>): TaskProvider<Task> {
    val publishXcFrameworkTask = registerPublishXcFrameworkArtifactTask(variants)
    val publishIOSTask = tasks.register("publishQnIOSArtifact") {
        group = QNKMM_TASK_GROUP
        description = "Publish the iOS XCFramework Output of the Kotlin Multiplatform To Maven Repository"
        dependsOn(publishXcFrameworkTask)
    }
    return publishIOSTask
}

/**
 * 注册publishXCFramework任务，会自动将xcFramework打包zip并发布到maven
 */
private fun Project.registerPublishXcFrameworkArtifactTask(variants: List<String>): TaskProvider<Task> {
    val publishXCFrameworkTask = variants.map { variant ->
        val zipTask = registerZipXcFrameworkTask(variant)
        registerCopyXCFrameworkToRepo(variant, zipTask)
        val uploadTask = registerUploadXcFrameworkTask(variant, zipTask)
        tasks.register("publish${variant}XCFramework") {
            group = QNKMM_TASK_GROUP
            description = "Publish the $variant XCFramework Output of the Kotlin Multiplatform To Maven Repository"
            dependsOn(uploadTask)
        }
    }

    val publishTask = tasks.register("publishXCFramework") {
        group = QNKMM_TASK_GROUP
        description = "Publish the ($variants) XCFramework Output of the Kotlin Multiplatform To Maven Repository"
        dependsOn(publishXCFrameworkTask)
    }
    return publishTask
}

/**
 * 注册uploadReleaseXcFramework任务，会自动生成podsepc并发布到对应的源
 */
private fun Project.registerUploadXcFrameworkTask(buildType: String, zipTask: TaskProvider<Zip>): TaskProvider<Task> {
    val version = getMavenArtifactVersion(buildType)

    val uploadTask = tasks.register("upload${buildType}XcFramework") {
        group = QNKMM_TASK_GROUP
        description = "Publish the $buildType xcFramework Output of the Kotlin Multiplatform To Maven Repository"
        dependsOn(zipTask)
    }

    val publishMavenTasks = configXcFrameworkPublish(this, buildType, version, zipTask) { url ->
        val cocoapods = kotlinExtension.cocoapods
        val cocoaPodsSpec = CocoaPodsSpec(
            source = url,
            version = qnPublishingExtension.version,
            name = cocoapods.name.ifEmpty { name },
            author = qnPublishingExtension.tag.commitAuthor,
            license = cocoapods.license ?: "",
            homepage = cocoapods.homepage ?: ""
        )
        uploadCocoaPodSpec(cocoaPodsSpec, buildType)
    }

    publishMavenTasks.forEach {
        uploadTask.configure { dependsOn(it) }
    }

    return uploadTask
}




