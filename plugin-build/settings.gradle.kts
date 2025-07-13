pluginManagement {
    repositories {
		mavenLocal()
        gradlePluginPortal()
        mavenCentral()

		maven {
			name = "gradle-github-support"
			url = uri("https://maven.pkg.github.com/bitfist/gradle-github-support")
			credentials {
				try {
					username = settings.extra["GPR_USER"] as String?
				} catch (exception: ExtraPropertiesExtension.UnknownPropertyException) {
					username = System.getenv("GITHUB_ACTOR") ?: throw IllegalArgumentException("GITHUB_ACTOR not set")
				}
				try {
					password = settings.extra["GPR_KEY"] as String?
				} catch (exception: ExtraPropertiesExtension.UnknownPropertyException) {
					password = System.getenv("GITHUB_TOKEN") ?: throw IllegalArgumentException("GITHUB_TOKEN not set")
				}
			}
		}
    }
	plugins {
		id("io.github.bitfist.github.release") version "0.1.4"
	}
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

include(":jcef")
