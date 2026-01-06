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

    androidTarget("android") {
    }
}

dependencies {
    api("com.mapbox.maps:android:11.0.0")
}

