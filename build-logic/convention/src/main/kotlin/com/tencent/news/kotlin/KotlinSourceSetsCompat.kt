package com.tencent.news.kotlin

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

// 兼容kotlin 1.9.20新增api
val NamedDomainObjectContainer<KotlinSourceSet>.commonMain by KotlinSourceSetConvention
val NamedDomainObjectContainer<KotlinSourceSet>.commonTest by KotlinSourceSetConvention
val NamedDomainObjectContainer<KotlinSourceSet>.androidMain by KotlinSourceSetConvention
val NamedDomainObjectContainer<KotlinSourceSet>.iosMain by KotlinSourceSetConvention
val NamedDomainObjectContainer<KotlinSourceSet>.iosX64Main by KotlinSourceSetConvention
val NamedDomainObjectContainer<KotlinSourceSet>.iosArm64Main by KotlinSourceSetConvention
val NamedDomainObjectContainer<KotlinSourceSet>.iosSimulatorArm64Main by KotlinSourceSetConvention
val NamedDomainObjectContainer<KotlinSourceSet>.iosTest by KotlinSourceSetConvention
val NamedDomainObjectContainer<KotlinSourceSet>.iosX64Test by KotlinSourceSetConvention
val NamedDomainObjectContainer<KotlinSourceSet>.iosArm64Test by KotlinSourceSetConvention
val NamedDomainObjectContainer<KotlinSourceSet>.iosSimulatorArm64Test by KotlinSourceSetConvention
val NamedDomainObjectContainer<KotlinSourceSet>.jsMain by KotlinSourceSetConvention
val NamedDomainObjectContainer<KotlinSourceSet>.jsTest by KotlinSourceSetConvention

object KotlinSourceSetConvention :
    ReadOnlyProperty<NamedDomainObjectContainer<KotlinSourceSet>, NamedDomainObjectProvider<KotlinSourceSet>> {

    internal class Trace : Throwable()

    override fun getValue(
        thisRef: NamedDomainObjectContainer<KotlinSourceSet>,
        property: KProperty<*>,
    ): NamedDomainObjectProvider<KotlinSourceSet> {
        val name = property.name
        if (name in thisRef.names) return thisRef.named(name)
        val trace = Trace()
        return thisRef.register(name) {
//            isRegisteredByKotlinSourceSetConventionAt = trace
        }
    }

    /**
     * @return the stacktrace when the user was using a [KotlinSourceSetConvention] that indeed created/registered a new SourceSet.
     * This will be null if SourceSet already existed and was referenced using the convention, or of no convention was used at all.
     */
//    @Suppress("UnusedReceiverParameter") // Diagnostic is wrong
//    private var KotlinSourceSet.isRegisteredByKotlinSourceSetConventionAt: Trace?
//            by extrasReadWriteProperty("isRegisteredByKotlinSourceSetConvention")
}