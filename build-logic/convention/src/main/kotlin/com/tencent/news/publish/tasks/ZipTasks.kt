package com.tencent.news.publish.tasks

import com.tencent.news.extension.QNKMM_TASK_GROUP
import com.tencent.news.extension.baseName
import com.tencent.news.extension.capitalizeFirstLetter
import com.tencent.news.extension.getXCFrameworkPath
import com.tencent.news.extension.getXCFrameworkZipFile
import com.tencent.news.extension.isCocoaPodsApplied
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip

/**
 * 注册对应zipXCFramework任务，等podPublishXCFramework生成xcFramework之后将其打包zip
 */
internal fun Project.registerZipXcFrameworkTask(buildType: String): TaskProvider<Zip> {
    val zipFile = getXCFrameworkZipFile(buildType)
    val sourceDir = getXCFrameworkPath(buildType)

    return tasks.register("zip${buildType}XCFramework", Zip::class.java) {
        group = QNKMM_TASK_GROUP
        description = "Zip the $buildType version of iOS XCFramework output of the Kmm to root directory"

        archiveFileName.set(zipFile.name)
        destinationDirectory.set(zipFile.parentFile)
        exclude("*.podspec")
        from(sourceDir)

//        dependsOn(getBuildXcFrameworkTask(buildType))

        doLast {
            logger.quiet("$buildType version of iOS XCFramework zipped to ${zipFile.path} successfully")
        }
    }
}

private fun Project.getBuildXcFrameworkTask(buildType: String): TaskProvider<Task> {
    return if (!isCocoaPodsApplied) {
        val baseName = baseName.capitalizeFirstLetter()
        tasks.named("assemble${baseName}${buildType}XCFramework")
    } else {
        tasks.named("podPublish${buildType}XCFramework")
    }
}

