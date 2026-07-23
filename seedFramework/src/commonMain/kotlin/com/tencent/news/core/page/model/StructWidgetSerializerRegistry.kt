package com.tencent.news.core.page.model

import kotlinx.serialization.modules.PolymorphicModuleBuilder

object StructWidgetSerializerRegistry {

    private val registrars = mutableListOf<PolymorphicModuleBuilder<StructWidget>.() -> Unit>()

    fun register(registrar: PolymorphicModuleBuilder<StructWidget>.() -> Unit) {
        registrars += registrar
    }

    internal fun applyTo(builder: PolymorphicModuleBuilder<StructWidget>) {
        registrars.forEach { registrar ->
            builder.registrar()
        }
    }
}
