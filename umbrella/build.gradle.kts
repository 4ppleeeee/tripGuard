import org.gradle.api.publish.maven.MavenPublication
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

plugins {
    id("qqnews.kmm.main")
    id("qqnews.kmm.harmony")
    id("qqnews.kmm.compose")
    id("qqnews.kmm.publish")
}

val seedCoreVersion = System.getenv("kuiklyBizVersion") ?: "0.1.0-SNAPSHOT"

group = "com.tencent.news.core"
version = seedCoreVersion

dependencies {
}

qqnewsKmm {

    withSource = true
    variants = listOf("release")
    iosArtifactName = "seed-core-ios"
    androidArtifactName = "seed-core-android"
    version = seedCoreVersion
    cocoapods = rootProject.properties["cocoapodsUrl"] as? String ?: ""

    tag {
        repoId = System.getenv("gitCodeRepoId") ?: ""
        commitId = System.getenv("commitId") ?: ""
        commitMessage = System.getenv("commitMessage") ?: ""
        commitAuthor = System.getenv("commitAuthor") ?: ""
    }
}

tasks.withType<Test> {
    this.testLogging {
        showStandardStreams = true
    }
}

tasks.register("publishSeedAndroidArtifact") {
    group = "publish"
    description = "Publish Seed Android release artifact"
    dependsOn("publishAndroidReleasePublicationToMavenRepository")
}

val iosArm64Only = providers.gradleProperty("qn.ios.arm64Only").orNull?.toBoolean() ?: false

tasks.register("publishSeedIosArtifact") {
    group = "publish"
    description = "Publish Seed iOS artifacts"
    dependsOn("publishIosArm64PublicationToMavenRepository")
    if (!iosArm64Only) {
        dependsOn(
            "publishIosSimulatorArm64PublicationToMavenRepository",
            "publishIosX64PublicationToMavenRepository",
        )
    }
}

tasks.register("publishSeedOhosArtifact") {
    group = "publish"
    description = "Publish Seed OHOS KLIB"
    dependsOn("publishOhosArm64PublicationToMavenRepository")
}

val patchSeedRootMetadata = tasks.register("patchSeedRootMetadata") {
    group = "publish"
    description = "Add the OHOS leaf variants to Seed root Gradle metadata"
    dependsOn("generateMetadataFileForKotlinMultiplatformPublication")

    doLast {
        val metadataFile = layout.buildDirectory.file(
            "publications/kotlinMultiplatform/module.json"
        ).get().asFile
        check(metadataFile.isFile) { "Seed root metadata was not generated: ${metadataFile.absolutePath}" }

        @Suppress("UNCHECKED_CAST")
        val rootMetadata = JsonSlurper().parse(metadataFile) as MutableMap<String, Any>
        @Suppress("UNCHECKED_CAST")
        val variants = rootMetadata.getValue("variants") as MutableList<MutableMap<String, Any>>
        variants.removeAll { it["name"]?.toString()?.startsWith("ohosArm64") == true }

        val ohosArtifactId = "seed-core-ohosarm64"
        val availableAt = mapOf(
            "url" to "../../$ohosArtifactId/$seedCoreVersion/$ohosArtifactId-$seedCoreVersion.module",
            "group" to "com.tencent.news.core",
            "module" to ohosArtifactId,
            "version" to seedCoreVersion,
        )
        val commonAttributes = mapOf(
            "org.gradle.jvm.environment" to "non-jvm",
            "org.jetbrains.kotlin.native.target" to "ohos_arm64",
            "org.jetbrains.kotlin.platform.type" to "native",
        )
        val ohosVariants = listOf(
            "ohosArm64ApiElements-published" to mapOf(
                "artifactType" to "org.jetbrains.kotlin.klib",
                "org.gradle.category" to "library",
                "org.gradle.usage" to "kotlin-api",
            ),
            "ohosArm64SourcesElements-published" to mapOf(
                "org.gradle.category" to "documentation",
                "org.gradle.dependency.bundling" to "external",
                "org.gradle.docstype" to "sources",
                "org.gradle.usage" to "kotlin-runtime",
            ),
            "ohosArm64ResourcesElements-published" to mapOf(
                "org.gradle.category" to "library",
                "org.gradle.dependency.bundling" to "external",
                "org.gradle.libraryelements" to "kotlin-multiplatformresources",
                "org.gradle.usage" to "kotlin-multiplatformresources",
            ),
        )
        ohosVariants.forEach { (name, attributes) ->
            variants += mutableMapOf(
                "name" to name,
                "attributes" to (commonAttributes + attributes),
                "available-at" to availableAt,
            )
        }

        metadataFile.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(rootMetadata)))
    }
}

tasks.register("publishSeedMetadataArtifact") {
    group = "publish"
    description = "Publish the Seed root metadata after all platform leaf artifacts exist"
    dependsOn("publishKotlinMultiplatformPublicationToMavenRepository")
}

tasks.configureEach {
    if (name == "publishKotlinMultiplatformPublicationToMavenRepository") {
        dependsOn(patchSeedRootMetadata)
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
                "kotlinMultiplatform" -> "seed-core"
                "androidRelease" -> "seed-core-android"
                "androidDebug" -> "seed-core-android-debug"
                "iosArm64" -> "seed-core-iosarm64"
                "iosSimulatorArm64" -> "seed-core-iossimulatorarm64"
                "iosX64" -> "seed-core-iosx64"
                "ohosArm64" -> "seed-core-ohosarm64"
                else -> artifactId.ifBlank { "kmm-seed-core-$name" }
            }
        }
    }
}
