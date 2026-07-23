/**
 * Precompiled [tn-enterprise.settings.gradle.kts][Tn_enterprise_settings_gradle] script plugin.
 *
 * @see Tn_enterprise_settings_gradle
 */
public
class TnEnterprisePlugin : org.gradle.api.Plugin<org.gradle.api.initialization.Settings> {
    override fun apply(target: org.gradle.api.initialization.Settings) {
        try {
            Class
                .forName("Tn_enterprise_settings_gradle")
                .getDeclaredConstructor(org.gradle.api.initialization.Settings::class.java, org.gradle.api.initialization.Settings::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
