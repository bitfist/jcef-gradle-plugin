![Gradle Plugin](https://img.shields.io/static/v1?label=Gradle&message=Plugin&color=blue&logo=gradle)
![GitHub Release Plugin](https://img.shields.io/static/v1?label=GitHub&message=Release&color=blue&logo=github)
![License](https://img.shields.io/badge/License-Apache%20License%20Version%202.0-blue)
[![Gradle build](https://github.com/bitfist/jcef-gradle-plugin/actions/workflows/test.yml/badge.svg)](https://github.com/bitfist/jcef-gradle-plugin/actions/workflows/test.yml)
![Coverage](.github/badges/jacoco.svg)
![Branches](.github/badges/branches.svg)

# JCEF Gradle Plugin

## 🚀 **Overview**

The JCEF Gradle Plugin simplifies integrating the Java Chromium Embedded Framework (JCEF) with Spring Boot applications.

---

Table of contents
=================
* [Features](#-features)
* [Installation](#-installation)
  * [in settings.gradle.kts](#-in-settingsgradlekts)
  * [in build.gradle.kts](#-in-buildgradlekts)
* [Configuration](#-configuration)
* [Usage](#-usage)
* [Example](#-example)

---

## ⚙️ **Features**

- Applies `java` and `org.springframework.boot` plugins automatically
- Manages dependencies via Spring Boot BOM
- Adds JCEF and Spring Boot Starter dependencies
- Configures Java compilation with TypeScript generation support

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

❌ Remember to set your GitHub user `GPR_USER` and GitHub Access Token `GPR_KEY` in `~/.gradle/gradle.properties`

### 📋 **in build.gradle.kts**
```kotlin
plugins {
	id("io.github.bitfist.jcef-gradle-plugin")
}
```

---

## 🛠️ **Configuration**

Configure the output path for generated TypeScript files:

```kotlin
jcef {
	typescriptOutputPath.set(file("$buildDir/generated/typescript"))
	// turns on CEF query calls through REST
	developmentMode.set(true)
	// application host
	developmentHost.set("localhost")
	developmentPort.set(8080)
}
```

---

## 📦 **Usage**

The plugin automatically:

1. Applies and configures `java` and `org.springframework.boot` plugins
2. Imports Spring Boot BOM for dependency management
3. Adds dependencies:
	- `io.github.bitfist:jcef-spring-boot-starter:<jcefVersion>`
	- Annotation processors for JCEF and Spring Boot
4. Configures Java compilation with:
	- `-Ajcef.output.path` pointing to your configured `typescriptOutputPath`
	- `-Ajcef.output.service.type` indicating processing of method calls through `WEB` or `QUERY`
	- `-Ajcef.output.web.host` indicating the host to call in the case of `jcef.output.service.type` being `WEB`
	- `-Ajcef.output.web.port` indicating the port to call in the case of `jcef.output.service.type` being `WEB`

---

## 💡 **Example**

```kotlin
plugins {
	id("io.github.bitfist.jcef")
}

jcef {
	typescriptOutputPath.set(file("$buildDir/generated/typescript"))
}
```
