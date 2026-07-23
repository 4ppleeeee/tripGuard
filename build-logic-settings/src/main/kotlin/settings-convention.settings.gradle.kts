import com.tencent.news.kmm.gradle.localProps

settings.extra["readPropAnywhere"] = KotlinClosure2({ key: String, defVal: String ->
    localProps().getProperty(key, defVal)
})