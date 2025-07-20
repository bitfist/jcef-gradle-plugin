![Gradle Plugin](https://img.shields.io/static/v1?label=Gradle&message=Plugin&color=blue&logo=gradle)
![GitHub Release Plugin](https://img.shields.io/static/v1?label=GitHub&message=Release&color=blue&logo=github)
![License](https://img.shields.io/badge/License-Apache%20License%20Version%202.0-blue)
[![Gradle build](https://github.com/bitfist/jcef-gradle-plugin/actions/workflows/test.yml/badge.svg)](https://github.com/bitfist/jcef-gradle-plugin/actions/workflows/test.yml)
![Coverage](.github/badges/jacoco.svg)
![Branches](.github/badges/branches.svg)

# JCEF Gradle Plugin

The JCEF Gradle Plugin simplifies integrating the Java Chromium Embedded Framework (JCEF) with Spring Boot applications.

> [!IMPORTANT]
> This project uses dependencies provided from GitHub. You therefore need to set your GitHub user `GPR_USER` and
> personal access token `GPR_TOKEN` in your `~/.gradle/gradle.properties`

---

Table of contents
=================
* [Features](#-features)
* [Installation](#-installation)
  * [in settings.gradle.kts](#-in-settingsgradlekts)
  * [in build.gradle.kts](#-in-buildgradlekts)
* [Configuration](#-configuration)
* [Usage](#-usage)

---

## ⚙️ **Features**

- Applies `java` and `org.springframework.boot` plugins automatically
- Manages dependencies via Spring Boot BOM
- Adds JCEF and Spring Boot Starter dependencies
- Configures Java compilation with TypeScript generation support
- Adds Maven repository for `io.github.bitfist:jcef-spring-boot-starter`
- In development mode: Configures the Gradle `bootRun` task

---

## 🔧 **Installation**

### 📋 **in settings.gradle.kts**

```kotlin
pluginManagement {
	repositories {
		gradlePluginPortal()
		maven {
			name = "gradle-github-support"
			url = uri("https://maven.pkg.github.com/bitfist/jcef-gradle-plugin")
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
		id("io.github.bitfist.jcef") version "<version>"
	}
}
```

### 📋 **in build.gradle.kts**
```kotlin
plugins {
	id("io.github.bitfist.jcef-gradle-plugin")
}
```

---

## 🛠️ **Configuration**

Configure the extension:

```kotlin
springJcef {
	typescriptOutputPath.set(file("$buildDir/generated/typescript"))
	// enables communication through REST
	enableWebCommunication {
		backendUri = "http://localhost:8080" // default value; used by the generated TypeScript services
		frontendUri = "http://localhost:3000" // default value; used to initialize the JCEF browser
	}
}
```

---

## 📦 **Usage**

The plugin automatically:

1. Applies and configures `java` and `org.springframework.boot` plugins
2. Imports Spring Boot BOM for dependency management
3. Adds Maven repository for `io.github.bitfist:jcef-spring-boot-starter`
4. Adds dependencies:
	- `io.github.bitfist:jcef-spring-boot-starter:<jcefVersion>`
	 - In development mode: `org.springframework.boot:spring-boot-starter-web`
	- Annotation processors for JCEF and Spring Boot
5. Configures Java compilation with:
	- `-Ajcef.output.path` pointing to your configured `typescriptOutputPath`
	- `-Ajcef.web.communication.enabled` indicating processing of method calls through `WEB` or `QUERY`
	- `-Ajcef.output.web.uri` indicating the host to call in the case of `jcef.web.communication.enabled` being `true`
6. In development mode: configures Gradle task `bootRun` with properties:
	- `jcef.development.enable-web-communication=true`
	- `jcef.development.frontend-uri` with whatever you configured in the extension; default http://localhost:3000
