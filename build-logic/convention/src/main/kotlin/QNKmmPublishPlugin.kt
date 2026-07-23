import com.tencent.news.api.QNKmmProcessor
import com.tencent.news.extension.QNKmmPublishingExtension
import com.tencent.news.extension.libs
import com.tencent.news.publish.QNKmmPublishProcessor
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * KMM插件，可自定义[QNKmmProcessor]实现不同的逻辑。目前已注册任务有：
 * 1. [QNKmmPublishProcessor]: KMM的maven发布任务
 */
class QNKmmPublishPlugin : Plugin<Project> {

    private val processors: MutableList<QNKmmProcessor> = mutableListOf()

    override fun apply(target: Project) {
        with(target) {
            initProcessors()
            registerExtension()
            applyPlugins()

            afterEvaluate {
                execProcessorAfterEvaluated()
            }
        }
    }

    private fun Project.initProcessors() {
        if (processors.isEmpty()) {
            processors.add(QNKmmPublishProcessor(this))
        }
    }

    private fun Project.registerExtension() {
        extensions.create("qqnewsKmm", QNKmmPublishingExtension::class.java)
    }

    private fun Project.applyPlugins() {
        pluginManager.apply(libs.plugins.maven.get().pluginId)
    }

    private fun execProcessorAfterEvaluated() {
        processors.forEach {
            it.doAfterProjectEvaluated()
        }
    }
}