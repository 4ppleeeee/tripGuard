import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 鸿蒙编译插件
 */
class QNKmmHarmonyPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            registerBuildHarmonyTask()
        }
    }

    private fun Project.registerBuildHarmonyTask() {
    }
}