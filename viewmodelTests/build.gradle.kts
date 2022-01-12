plugins {
    kotlin("multiplatform")
    id("java-library")
}


kotlin {
    jvm("jvm")

    sourceSets {
        val jvmMain by getting {

            kotlin.srcDir("src/jvmMain/kotlin")

            dependencies {
                implementation(project(":viewmodel"))
                implementation("tech.skot:viewmodelTests:${Versions.framework}")
            }
        }
    }
}