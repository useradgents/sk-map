plugins {
    kotlin("multiplatform")
    id("java-library")
    signing
}


kotlin {
    jvm("jvm")

    sourceSets {
        val jvmMain by getting {

            kotlin.srcDir("src/jvmMain/kotlin")

            dependencies {
                implementation(project(":viewmodel"))
                implementation("${Versions.frameworkGroup}:viewmodelTests:${Versions.framework}")
                implementation("${Versions.frameworkGroup}:core-jvm:${Versions.framework}")
            }
        }
    }
}



if (!localPublication) {
    val publication = getPublication(project)

    val javadocJar by tasks.registering(Jar::class) {
        archiveClassifier.set("javadoc")
    }

    publishing {
        publications.withType<MavenPublication> {
            artifact(javadocJar.get())

            if (!localPublication) {
                pom {
                    name.set("SK-Map " + project.name)
                    description.set("${project.name} module for SK-Map skot library")
                    url.set("https://github.com/skot-framework/sk-map")
                    licenses {
                        license {
                            name.set("Apache 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }
                    developers {
                        developer {
                            id.set("sgueniot")
                            name.set("Sylvain Guéniot")
                            email.set("sylvain.gueniot@gmail.com")
                        }
                        developer {
                            id.set("MathieuScotet")
                            name.set("Mathieu Scotet")
                            email.set("mscotet.lmit@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:github.com/skot-framework/sk-map.git")
                        developerConnection.set("scm:git:ssh://github.com/skot-framework/sk-map.git")
                        url.set("https://github.com/skot-framework/sk-map/tree/master")
                    }
                }
            }

        }
    }

    signing {
        useInMemoryPgpKeys(
            publication.signingKeyId,
            publication.signingKey,
            publication.signingPassword
        )
        this.sign(publishing.publications)
    }

}