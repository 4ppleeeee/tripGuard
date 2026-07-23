package com.tencent.news.publish.tasks

import com.tencent.news.extension.getMavenArtifactVersion
import com.tencent.news.extension.libs
import com.tencent.news.extension.publishingExtension
import com.tencent.news.extension.qnPublishingExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.get

/**
 * 配置发布aar的maven任务
 * 1. 产物的版本号会自动添加当前编译类型，比如@release
 * 2. 产物的groupId为`[Project.getGroup]`
 */
internal fun Project.configAndroidKMMPublish(buildType: String): List<TaskProvider<Task>> {
    val androidArtifactId = qnPublishingExtension.androidArtifactName
    val version = getMavenArtifactVersion(buildType)
    val publicationName = "qnkmmAndroid${buildType}"

    publishingExtension.publications.create(publicationName, MavenPublication::class.java) {
        this.version = version
        this.groupId = project.group.toString()
        this.artifactId = androidArtifactId
        // 从components中取对应的产物，会自动添加依赖
        from(components[buildType.toLowerCase()])
        // 配置自定义POM熟悉
        configMavenPomProps(this)

        if (qnPublishingExtension.withSource) {
            val task = tasks["android${buildType}SourcesJar"]
            artifact(task) {
                classifier = "sources"
            }
        }
    }

    return configPublishDependency(publicationName, null)
}

/**
 * 配置发布XCFramework的maven任务
 * 1. 产物的版本号会自动添加当前编译类型，比如@release
 * 2. 产物的groupId为`[Project.getGroup].ios`
 */
internal fun configXcFrameworkPublish(
    project: Project,
    buildType: String,
    version: String,
    zipTask: TaskProvider<Zip>,
    uploadAction: (url: String) -> Unit
): List<TaskProvider<Task>> {

    val xcFrameworkArtifactId = project.qnPublishingExtension.iosArtifactName
    val publicationName = "qnkmmXcFramework${buildType}"

    project.publishingExtension.publications.create(publicationName, MavenPublication::class.java) {
        this.version = version
        this.groupId = "${project.project.group}.ios"
        this.artifactId = xcFrameworkArtifactId

        val archiveProvider = artifact(zipTask.flatMap { it.archiveFile })
        artifact(archiveProvider) {
            extension = "zip"
        }
        project.configMavenPomProps(this)
    }
    return project.configPublishDependency(publicationName, uploadAction)
}

/**
 * 配置[publicationName]对应的发布任务，对于iOS xcFramework会生成产物的[url]并在[uploadAction]中回调，方便配置podSpec
 */
internal fun Project.configPublishDependency(
    publicationName: String,
    uploadAction: ((url: String) -> Unit)?
): List<TaskProvider<Task>> {
    val publication = publishingExtension.publications.getByName(publicationName) as MavenPublication
    val publicationNameCap = publication.name.capitalized()
    val repos = publishingExtension.repositories.filterIsInstance<MavenArtifactRepository>()
    val publishTasks = repos.map { repo ->
        val repoName = repo.name.capitalized()
        val publishTaskName = "publish${publicationNameCap}PublicationTo${repoName}Repository"
        // Verify that the "publish" task exists before collecting
        val task = project.tasks.named(publishTaskName) {
            doLast {
                val url = publication.getMavenRepoArtifactUrl(repo)
                uploadAction?.invoke(url)

                val version = publication.version
                val artifactId = publication.artifactId
                println("${artifactId}(v${version}) has been publish to ${url}.")
            }
        }
        task
    }
    return publishTasks
}

/**
 * 拼装maven产物发布之后的url
 */
private fun MavenPublication.getMavenRepoArtifactUrl(repo: MavenArtifactRepository): String {
    val url = repo.url.toString()
    val group = groupId.replace(".", "/")
    return "$url/$group/$artifactId/$version/$artifactId-$version.zip"
}

/**
 * 将当前的git信息拼接到maven的pom文件中
 */
private fun Project.configMavenPomProps(mavenPublication: MavenPublication) {
    val tag = project.qnPublishingExtension.tag
    mavenPublication.pom {
        packaging = "aar"
        properties.put("commitId", tag.commitId)
        properties.put("commitMessage", tag.commitMessage)
        properties.put("lastAuthor", tag.commitAuthor)
        properties.put("agp", libs.versions.agp.get())
    }
}
