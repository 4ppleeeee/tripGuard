package com.tencent.kmm.demo.setup

/**
 * Android shell build metadata injected from BuildConfig during app startup.
 */
internal object BuildInfo {
    var buildId: String = "local"
        private set
    var buildNum: String = "local"
        private set
    var pipelineName: String = "local"
        private set
    var branch: String = "local"
        private set
    var commit: String = "local"
        private set
    var buildTime: String = "local"
        private set

    fun init(
        buildId: String,
        buildNum: String,
        pipelineName: String,
        branch: String,
        commit: String,
        buildTime: String,
    ) {
        this.buildId = buildId
        this.buildNum = buildNum
        this.pipelineName = pipelineName
        this.branch = branch
        this.commit = commit
        this.buildTime = buildTime
    }
}
