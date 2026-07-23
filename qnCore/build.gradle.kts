plugins {
    id("qqnews.kmm.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":qnPlatform"))
        }
    }
}
