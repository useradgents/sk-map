plugins {
    kotlin("multiplatform")
    id("tech.skot.library-viewlegacy")
    signing
}

android {
    namespace = "tech.skot.libraries.skmap.viewlegacy"
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
    }
    androidTarget("android") {

    }
}

dependencies {
    api("com.google.android.gms:play-services-maps:19.2.0")
    api("com.google.maps.android:android-maps-utils:3.20.1")
    api("com.google.maps.android:maps-utils-ktx:5.2.2")
    implementation("com.google.android.gms:play-services-location:21.3.0")
}

