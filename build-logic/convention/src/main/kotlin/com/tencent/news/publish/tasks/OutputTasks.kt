package com.tencent.news.publish.tasks

import com.tencent.news.extension.QNKMM_TASK_GROUP
import com.tencent.news.extension.androidArtifactOutputDir
import com.tencent.news.extension.baseName
import com.tencent.news.extension.getXCFrameworkPath
import com.tencent.news.extension.iOSArtifactOutputDir
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.register

/**
 * 将Android AAR产物复制到/build/qnkmm/android/
 */
internal fun Project.registerCopyAndroidAarTask(buildType: String): TaskProvider<Copy> {
    return tasks.register("copyAndroid${buildType}Aar", Copy::class.java) {
        group = QNKMM_TASK_GROUP
        description = "Copy the Android AAR Output of the Kotlin Multiplatform to Root Directory"

        val fileName = "${baseName}-${buildType.toLowerCase()}.aar"
        val dest = "${androidArtifactOutputDir}/aar/${fileName}"
        from(layout.buildDirectory.file("outputs/aar/${fileName}"))
        into(dest)

        doLast {
            logger.quiet("Android AAR successfully copied to $dest")
        }

        dependsOn(tasks.named("bundle${buildType}Aar"))
    }
}

/**
 * 将XCFramework打包之后的zip文复制到/build/qnkmm/ios/
 */
internal fun Project.registerCopyXCFrameworkToRepo(buildType: String, zipTask: TaskProvider<Zip>) {
    tasks.register<Copy>("copy${buildType}XCFramework") {
        group = QNKMM_TASK_GROUP
        description =
            "Copy the $buildType version of iOS XCFramework Output of the Kotlin Multiplatform to PodSpec git submodule"

        val dest = "${iOSArtifactOutputDir}/$buildType/xcframework"
        into(dest)
        from(getXCFrameworkPath(buildType))

        doLast {
            logger.quiet("iOS XCFramework successfully copied to $dest")
        }
        dependsOn(zipTask)
    }
}