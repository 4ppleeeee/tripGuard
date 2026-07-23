package com.tencent.news.publish

import com.tencent.news.api.QNKmmProcessor
import com.tencent.news.extension.QNKMM_TASK_GROUP
import com.tencent.news.extension.capitalizeFirstLetter
import com.tencent.news.extension.iOSTargets
import com.tencent.news.extension.kotlinExtension
import com.tencent.news.extension.qnPublishingExtension
import com.tencent.news.publish.tasks.registerIosPublishArtifactTask
import com.tencent.news.publish.tasks.registerPublishAndroidArtifactTask
import com.tencent.news.utils.isAndroidBuild
import com.tencent.news.utils.isIOSBuild
import com.tencent.news.utils.runIfNotOhosBuild
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * 注册KMM的maven发布任务，目前支持：
 * 1. 发布android aar到maven，任务名称为**publishAndroidArtifact**
 * 2. 发布iOS XCFramework到maven，并上传podSpec到指定源，任务名称为**publishIOSArtifact**
 * 3. publishArtifact可以同时发布1和2
 */
class QNKmmPublishProcessor(override val project: Project) : QNKmmProcessor {

    override fun doAfterProjectEvaluated() {
        with(project) {
            runIfNotOhosBuild {
                registerPublishTask()
            }
        }
    }

    private fun Project.registerPublishTask() {
        val publishTask = if (isAndroidBuild()) {
            registerAndroidPublishTask()
        } else {
            registerIOSPublishTask()
        }
        tasks.register("publishArtifact").configure {
            group = QNKMM_TASK_GROUP
            description = "publish artifacts of all kotlin multiplatform"
            dependsOn(publishTask)
        }
    }

    private fun KotlinMultiplatformExtension.getAndroidTarget(): Any {
        return try {
            // 尝试调用 androidTarget()
            javaClass.getMethod("androidTarget").invoke(this)
        } catch (e: Exception) {
            try {
                // 如果失败，尝试调用 android()
                javaClass.getMethod("android").invoke(this)
            } catch (e: Exception) {
                error("Failed to call either androidTarget() or android() method")
            }
        }
    }

    private fun Project.registerAndroidPublishTask(): TaskProvider<Task> {
        val variantsCandidate = if (qnPublishingExtension.variants.isNullOrEmpty()) {
            kotlinExtension.getAndroidTarget().javaClass.getMethod("getPublishLibraryVariants").invoke(kotlinExtension.getAndroidTarget())
        } else {
            qnPublishingExtension.variants
        }
        val variants = variantsCandidate?.let { if (it is Collection<*>) it.map { it.toString().capitalizeFirstLetter() } else null }?.distinct()
        variants ?: error("Please config publishLibraryVariants in kotlin.android extension")
        logger.info("register android publish tasks: $variants")
        val publishAndroidTask = registerPublishAndroidArtifactTask(variants)
        return publishAndroidTask
    }

    private fun Project.registerIOSPublishTask(): TaskProvider<Task> {
        val frameworks = kotlinExtension.iOSTargets.flatMap { it.binaries.filterIsInstance<Framework>() }
        val variantsCandidate = if (qnPublishingExtension.variants.isNullOrEmpty()) {
            frameworks.map { it.buildType.getName() }
        } else {
            qnPublishingExtension.variants
        }
        val variants = variantsCandidate?.map { it.capitalizeFirstLetter() }?.distinct()
        variants ?: error("Please config iOS target in kotlin extension or variants in qqnewsKmm extension")
        logger.info("register xcFramework publish tasks: $variants")
        val publishXcFrameworkTask = registerIosPublishArtifactTask(variants)
        return publishXcFrameworkTask
    }
}