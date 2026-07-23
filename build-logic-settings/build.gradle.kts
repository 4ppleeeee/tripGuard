plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(gradleKotlinDsl())
    implementation(libs.gradle.enterprisePlugin)
}

repositories {
    maven("https://mirrors.tencent.com/nexus/repository/gradle-plugins")
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    google()
}
