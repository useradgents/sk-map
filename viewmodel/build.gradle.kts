plugins {
    kotlin("multiplatform")
    id("tech.skot.library")
    signing
}
kotlin {
    android {
        compileSdk = 36
        namespace = "tech.skot.libraries.skmap"
    }
}


val dokkaOutputDir = layout.buildDirectory.dir("dokkaHtml")


dokka {
    moduleName.set("sk-map")
    dokkaPublications.html {
        outputDirectory.set(dokkaOutputDir)
    }
}

val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") {
    delete(dokkaOutputDir)
}

val javadocJar = tasks.register<Jar>("javadocJar") {
    dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaOutputDir)
}
