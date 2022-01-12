buildscript {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        classpath("tech.skot:plugin:${Versions.framework}")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.5.30")
    }
}

allprojects {

    group = Versions.group
    version = "${Versions.library}_${Versions.framework}"

    repositories {
        google()
        mavenLocal()
        mavenCentral()
    }

    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.dokka")

}
