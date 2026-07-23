package com.tencent.news.extension

import org.gradle.api.Action

/**
 * KMM产物发布配置
 */
interface QNKmmPublishingExtension {

    /**
     * 产物版本号
     */
    var version: String

    /**
     * 发布产物时是否携带源码
     */
    var withSource: Boolean

    /**
     * 产物变种(debug, release);
     * 如果未配置，则从Android取publishLibraryVariants，iOS取Framework的buildType；
     * 产物的版本号会添加后缀**@${variant}**以防止同时发布多个产物；
     */
    var variants: List<String>?

    /**
     * 发布CocoaPods spec的仓库源
     */
    var cocoapods: String

    /**
     * iOS XCFramework发布到Maven的名称
     */
    var iosArtifactName: String

    /**
     * Android AAR发布到Maven的名称
     */
    var androidArtifactName: String

    /**
     * Git相关信息，会写入到pom文件里
     */
    var tag: QNKmmGitTag

    fun tag(action: Action<QNKmmGitTag>) {
        this.tag = QNKmmGitTag()
        action.execute(this.tag)
    }
}

class QNKmmGitTag {
    /**
     * 仓库id
     */
    var repoId: String = ""

    /**
     * 最后一次提交的commit id
     */
    var commitId: String = ""

    /**
     * 最后一次提交的评论
     */
    var commitMessage: String = ""

    /**
     * 最后一次提交的用户名
     */
    var commitAuthor: String = ""
}
