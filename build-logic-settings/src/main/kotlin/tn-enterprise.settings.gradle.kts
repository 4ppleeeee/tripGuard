plugins {
    id("com.gradle.enterprise")
}

val isCiPushingServer = System.getenv().containsKey("ENABLE_GRADLE_CACHE_PUSHING")
val gradleEnterpriseServer: String by settings
val useFreeGeServer: String? by settings
val geAccessKey: String? by settings
val disableBuildScan: String? by settings
val disableTaskInputsCapture: String? by settings
val buildCacheUrl: String by settings
val buildCacheUser: String? by settings
val buildCachePassword: String? by settings
val isCI = System.getenv("BK_CI_PIPELINE_ID").isNullOrEmpty().not()
val enableLocalBuildCache: String by settings
val disableRemoteBuildCache: String? by settings

fun com.gradle.scan.plugin.BuildScanExtension.bgCmdValue(name: String, vararg args: Any) {
    background {
        val os = java.io.ByteArrayOutputStream()
        runCatching {
            exec {
                commandLine(*args)
                standardOutput = os
            }
            value(name, os.toString())
        }
    }
}

gradleEnterprise {
    allowUntrustedServer = true
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        // CI 机器上不在后台上传，防止上传失败
        isUploadInBackground = isCI.not()
        isCaptureTaskInputFiles = disableTaskInputsCapture?.toBoolean() != true
        // 默认自动上传，但留一个配置项可供关闭
        publishAlwaysIf(disableBuildScan?.toBoolean() != true)
        tag(if (isCI) "CI" else "Local")
        tag(System.getProperty("os.name"))
        if (isCI) {
            val projectName = System.getenv("BK_CI_PROJECT_NAME")
            val pipelineId = System.getenv("BK_CI_PIPELINE_ID")
            val buildId = System.getenv("BK_CI_BUILD_ID")
            // sync with BlueKing's GradleEnterprise plugin
            tag("project: $projectName")
            tag("pipeline: $pipelineId")
            tag("build: $buildId")
            // custom values for CI builds
            value("Pipeline Project Name", projectName)
            value("Pipeline Name", System.getenv("BK_CI_PIPELINE_NAME"))
            value("Pipeline ID", pipelineId)
            value("Pipeline Starter", System.getenv("BK_CI_START_USER_NAME"))
            value("Pipeline Start Type", System.getenv("BK_CI_START_TYPE"))
            value("Pipeline Build No", System.getenv("BK_CI_BUILD_NUM"))
            value("Pipeline Build ID", buildId)
            value("Pipeline Job ID", System.getenv("BK_CI_BUILD_JOB_ID"))
            value("Pipeline Task ID", System.getenv("BK_CI_BUILD_TASK_ID"))
            link(
                "Pipeline Url",
                "https://devops.woa.com/console/pipeline/$projectName/$pipelineId/detail/$buildId"
            )
        } else {
            tag("project: tencentnews")
            // special tags for local builds
            gradle.startParameter.projectProperties["android.injected.studio.version"]?.let {
                tag("AS $it")
            }
        }
        bgCmdValue("Git Commit ID", "git", "rev-parse", "--verify", "HEAD")
        bgCmdValue("Git Branch Name", "git", "rev-parse", "--abbrev-ref", "HEAD")
    }
}