import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("qqnews.kmm.main")
    id("qqnews.kmm.harmony")
    id("qqnews.kmm.publish")
}

val seedCoreVersion = System.getenv("kuiklyBizVersion") ?: "0.1.0-SNAPSHOT"

group = "com.tencent.news.core"
version = seedCoreVersion

kotlin {
    sourceSets {
        commonMain.dependencies {
            api("com.tencent.news.core:seed-core:$seedCoreVersion")
        }
    }
}

qqnewsKmm {

    withSource = true
    variants = listOf("release")
    iosArtifactName = "kmm-seed-startup"
    androidArtifactName = "kmm-seed-startup"
    version = seedCoreVersion
    cocoapods = rootProject.properties["cocoapodsUrl"] as? String ?: ""

    tag {
        repoId = System.getenv("gitCodeRepoId") ?: ""
        commitId = System.getenv("commitId") ?: ""
        commitMessage = System.getenv("commitMessage") ?: ""
        commitAuthor = System.getenv("commitAuthor") ?: ""
    }
}

android {
    publishing {
        singleVariant("release")
    }
}

publishing {
    repositories {
        maven {
            credentials {
                username = System.getenv("mavenUserName") ?: ""
                password = System.getenv("mavenPassword") ?: ""
            }
            url = uri(rootProject.properties["mavenUrl"] as? String ?: "")
        }
    }
}

gradle.projectsEvaluated {
    publishing {
        publications.withType<MavenPublication>().configureEach {
            if (name.startsWith("qnkmm") || name.startsWith("XCFramework")) {
                return@configureEach
            }
            groupId = "com.tencent.news.core"
            version = seedCoreVersion
            artifactId = when (name) {
                "kotlinMultiplatform" -> "kmm-seed-startup"
                "androidRelease" -> "kmm-seed-startup-android"
                "androidDebug" -> "kmm-seed-startup-android-debug"
                "iosArm64" -> "kmm-seed-startup-iosarm64"
                "iosSimulatorArm64" -> "kmm-seed-startup-iossimulatorarm64"
                "iosX64" -> "kmm-seed-startup-iosx64"
                else -> artifactId.ifBlank { "kmm-seed-startup-$name" }
            }
        }
    }
}
