/**
 * Precompiled [settings-convention.settings.gradle.kts][Settings_convention_settings_gradle] script plugin.
 *
 * @see Settings_convention_settings_gradle
 */
public
class SettingsConventionPlugin : org.gradle.api.Plugin<org.gradle.api.initialization.Settings> {
    override fun apply(target: org.gradle.api.initialization.Settings) {
        try {
            Class
                .forName("Settings_convention_settings_gradle")
                .getDeclaredConstructor(org.gradle.api.initialization.Settings::class.java, org.gradle.api.initialization.Settings::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
