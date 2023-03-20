var mapBoxEnabled = false
include(":viewcontract")
include(":viewmodel")
include(":viewlegacy")
if (mapBoxEnabled) {
    include(":viewlegacy_mapbox")
}
include(":viewmodelTests")


fun getMapBoxKey(): String {
    return if (mapBoxEnabled) {
        val localProperties = java.util.Properties()
        localProperties.load(java.io.FileInputStream(File(rootDir, "local.properties")))
        localProperties.getProperty("mapBoxPrivateKey")
    } else {
        "no-key"
    }
}


if (mapBoxEnabled) {
    dependencyResolutionManagement {
        repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
        repositories {
            google()
            mavenCentral()
            maven {
                url = uri("https://jitpack.io")
            }

            maven {
                setUrl("https://api.mapbox.com/downloads/v2/releases/maven")
                authentication {
                    create<BasicAuthentication>("basic")
                }
                credentials {
                    // Do not change the username below.
                    // This should always be `mapbox` (not your username).
                    username = "mapbox"
                    // Use the secret token you stored in gradle.properties as the password

                    password = getMapBoxKey()

                }
            }
        }
    }
} else {
    dependencyResolutionManagement {
        repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
        repositories {
            google()
            mavenCentral()
            maven {
                url = uri("https://jitpack.io")
            }

        }
    }
}