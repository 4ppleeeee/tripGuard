plugins {
    id("qqnews.kmm.library")
    id("qqnews.kmm.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":qnView"))
            api(project(":qnFramework"))
            api(project(":qnPlatform"))
        }
    }
}
