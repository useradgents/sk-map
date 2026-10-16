plugins {
    id("tech.skot.library-viewlegacy")
    signing

}


android {
    namespace = "tech.skot.libraries.skmap.viewlegacy"
}

dependencies {
    api("com.mapbox.maps:android:11.0.0")
}

