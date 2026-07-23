package com.tencent.news.core.setup

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.list.trace.NewsJson
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass


object GlobalModelSerializerFactory {

    private val serializerMap by lazy { linkedMapOf<KClass<*>, KSerializer<*>>() }

    // todo 【架构说明】有2种情况，需要在这里注册model的解析：
    //  1. model分散到各个 qnXXX 模块里，处理模块之间的依赖注入；
    //  2. 宿主老model类的Gson解析，有用到这个接口，也需要在这里注入（宿主会对这里注册的自动适配Gson）
    @KmmInternalApi // 【内部api】仅限kmm各模块setup阶段注册使用，其余情况禁止调用
    fun <T : Any> register(clazz: KClass<T>, creator: KSerializer<out T>) {
        serializerMap[clazz] = creator
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getDefaultSerializer(clazz: KClass<T>): KSerializer<out T> {
        // tips：register方法里有泛型强约束，所以这里get出来只要非空就可以强转，不会有坑
        val creator = serializerMap[clazz] as? KSerializer<out T>

        if (creator == null) {
            val error = RuntimeException(
                "【警告】请检查 ${clazz.simpleName} 是否漏注册了 GlobalModelSerializerFactory.register"
            )
            NewsJson.error("GlobalModelSerializerFactory 解析异常", error)
            throw error
        }

        return creator
    }

    inline fun <reified T : Any> getDefault(): KSerializer<out T> = getDefaultSerializer(T::class)

    fun getRegistryList(): List<Pair<KClass<*>, KSerializer<*>>> =
        serializerMap.map { it.key to it.value }

}

