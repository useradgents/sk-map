plugins {
    id("tech.skot.library-viewlegacy")
    signing
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3)
    }
}

android {
    compileSdk { version =  release(36) }
    namespace = "tech.skot.libraries.skmap.viewlegacy"
}

dependencies {
    api(libs.play.services.maps)
    api(libs.android.maps.utils)
    api(libs.maps.utils.ktx)
    implementation(libs.play.services.location)
}

