buildscript {
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
    }

    dependencies {
        classpath("${Versions.frameworkGroup}:plugin:${Versions.framework}")
        classpath(libs.dokka.gradle.plugin)
    }
}

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

allprojects {

    group = Versions.group
    version = "${Versions.library}_${Versions.framework}"

    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.dokka")

    repositories {
        google()
        mavenLocal()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
    }

}
