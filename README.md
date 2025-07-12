<img src="https://img.shields.io/static/v1?label=Gradle&message=Plugin&color=6DB33F&logo=gradle" alt="Gradle Plugin"/>
<img src="https://img.shields.io/static/v1?label=GitHub&message=Release&color=24292e&logo=github" alt="GitHub Release Plugin"/>
[![Gradle build](https://github.com/bitfist/jcef-gradle-plugin/actions/workflows/test.yml/badge.svg)](https://github.com/bitfist/os-conditions-spring-boot-starter/actions/workflows/test.yml)
![Coverage](.github/badges/jacoco.svg)

# JCEF Gradle Plugin

## 🚀 **Overview**

The JCEF Gradle Plugin simplifies integrating the Java Chromium Embedded Framework (JCEF) with Spring Boot applications.

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
	id("io.github.bitfist.jcef")
}
```

---

## 🛠️ **Configuration**

Configure the output path for generated TypeScript files:

```kotlin
jcef {
	typescriptOutputPath.set(file("$buildDir/generated/typescript"))
}
```

---

## 📦 **Usage**

The plugin automatically:

1. Applies `java` and `org.springframework.boot` plugins
2. Imports Spring Boot BOM for dependency management
3. Adds dependencies:
	- `io.github.bitfist:jcef-spring-boot-starter:<jcefVersion>`
	- Annotation processors for JCEF and Spring Boot
4. Configures Java compilation with `-Ajcef.output.path` pointing to your configured `typescriptOutputPath`

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
