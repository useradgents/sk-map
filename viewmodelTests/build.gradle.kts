plugins {
    id("com.android.library")
    signing
}

android {
    namespace = "tech.skot.libraries.skmap.viewmodeltests"
    compileSdk = tech.skot.Versions.android_compileSdk
    defaultConfig {
        minSdk = tech.skot.Versions.android_minSdk
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    sourceSets {
        getByName("main") {
            kotlin {
                directories.add("src/jvmMain/kotlin")
            }
        }
    }
    publishing {
        singleVariant("release") { withSourcesJar() }
    }
}

dependencies {
    implementation(project(":viewmodel"))
    implementation("${Versions.frameworkGroup}:viewmodelTests:${Versions.framework}")
    implementation("${Versions.frameworkGroup}:core:${Versions.framework}")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
            }
        }
    }
}
