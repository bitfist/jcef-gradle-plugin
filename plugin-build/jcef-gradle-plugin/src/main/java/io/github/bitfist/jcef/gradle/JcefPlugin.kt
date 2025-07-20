package io.github.bitfist.jcef.gradle

import io.spring.gradle.dependencymanagement.DependencyManagementPlugin
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.springframework.boot.gradle.dsl.SpringBootExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import org.springframework.boot.gradle.tasks.run.BootRun
import java.net.URI
import java.util.Properties

/**
 * JcefPlugin is a Gradle plugin designed to simplify the setup and integration of a project
 * that utilizes JCEF (Java Chromium Embedded Framework) combined with Spring Boot.
 * It performs the following key tasks:
 *
 * - Applies necessary plugins such as JavaPlugin and SpringBootPlugin.
 * - Configures Spring Boot dependency management by importing the Spring Boot BOM.
 * - Adds the required dependencies for JCEF and Spring Boot.
 * - Configures Java compilation settings, including compiler arguments.
 *
 * The plugin also interacts with the `JcefExtension` to customize and parameterize the output path
 * for generated TypeScript files through the `typescriptOutputPath` property.
 *
 * This plugin expects a properties file named `versions.properties` on the classpath
 * containing dependency version definitions for keys like `jcef` and `springBoot`.
 */

const val EXTENSION_NAME = "springJcef"

@Suppress("UnnecessaryAbstractClass")
abstract class JcefPlugin : Plugin<Project> {

	companion object {
		const val JCEF_OUTPUT_PATH_OPTION: String = "jcef.output.path"
		const val JCEF_WEB_COMMUNICATION_ENABLED_OPTION: String = "jcef.web.communication.enabled"
		const val WEB_BACKEND_URI_OPTION: String = "jcef.web.backend.uri"

		const val JVM_ARG_ENABLE_WEB_COMMUNICATION = "jcef.development.enable-web-communication"
		const val JVM_ARG_WEB_FRONTEND_URI = "jcef.development.frontend-uri"

		const val VERSION_PROPERTY_SPRING_BOOT = "springBoot"
		const val VERSION_PROPERTY_SPRING_JCEF = "springJcef"
	}

	private val versions = loadVersions()

	override fun apply(project: Project) {
		val extension = project.extensions.create(EXTENSION_NAME, JcefExtension::class.java, project)

		configureJava(project, extension)
		configureSpringBoot(project, extension)
		configureSpringJcef(project)
	}

	private fun configureSpringBoot(project: Project, extension: JcefExtension) {
		val springBootVersion = versions.getProperty(VERSION_PROPERTY_SPRING_BOOT)
			?: throw GradleException("Property '$VERSION_PROPERTY_SPRING_BOOT' not found in versions.properties")

		project.pluginManager.apply(SpringBootPlugin::class.java)
		project.extensions.configure(SpringBootExtension::class.java) {
			it.buildInfo()
		}

		project.pluginManager.apply(DependencyManagementPlugin::class.java)
		project.extensions.configure(DependencyManagementExtension::class.java) { management ->
			management.imports {
				it.mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
			}
		}

		project.repositories.maven { repository ->
			repository.name = "jcef-spring-boot-starter"
			repository.url = URI("https://maven.pkg.github.com/bitfist/jcef-spring-boot-starter")
			repository.credentials { credentials ->
				credentials.username = project.findProperty("GPR_USER") as String?
					?: System.getenv("GITHUB_ACTOR")
						?: throw IllegalArgumentException("GPR_USER and GITHUB_ACTOR not set")
				credentials.password = project.findProperty("GPR_KEY") as String?
					?: System.getenv("GITHUB_TOKEN")
						?: throw IllegalArgumentException("GPR_KEY and GITHUB_TOKEN not set")
			}
		}

		project.dependencies.apply {
			add("annotationProcessor", "org.springframework.boot:spring-boot-autoconfigure-processor:$springBootVersion")
			project.afterEvaluate {
				if (extension.webCommunication.isPresent) {
					add("implementation", "org.springframework.boot:spring-boot-starter-web")
				} else {
					add("implementation", "org.springframework.boot:spring-boot-starter")
				}
			}
		}
		project.afterEvaluate {
			if (extension.webCommunication.isPresent) {
				project.tasks.withType(BootRun::class.java).configureEach { task ->
					task.jvmArgs = listOf(
						"-D$JVM_ARG_ENABLE_WEB_COMMUNICATION=true",
						"-D$JVM_ARG_WEB_FRONTEND_URI=${extension.webCommunication.get().frontendUri}",
					)
				}
			}
		}
	}

	private fun configureSpringJcef(project: Project) {
		val springJcefVersion = versions.getProperty(VERSION_PROPERTY_SPRING_JCEF)
			?: throw GradleException("Property '$VERSION_PROPERTY_SPRING_JCEF' not found in versions.properties")

		project.dependencies.apply {
			add("implementation", "io.github.bitfist:jcef-spring-boot-starter:$springJcefVersion")
			add("annotationProcessor", "io.github.bitfist:jcef-spring-boot-starter:$springJcefVersion")
		}
	}

	private fun loadVersions(): Properties {
		val props = Properties()
		val resourceStream = javaClass.classLoader.getResourceAsStream("versions.properties")
			?: throw GradleException("versions.properties not found in classpath")
		resourceStream.use { props.load(it) }
		return props
	}

	private fun configureJava(project: Project, extension: JcefExtension) {
		project.pluginManager.apply(JavaPlugin::class.java)

		project.afterEvaluate {
			project.tasks.withType(JavaCompile::class.java).configureEach { compile ->
				compile.options.encoding = "UTF-8"
				if (extension.webCommunication.isPresent) {
					compile.options.compilerArgs.add("-A$WEB_BACKEND_URI_OPTION=${extension.webCommunication.get().backendUri}")
				}
				compile.options.compilerArgs.addAll(
					listOf(
						"-parameters",
						"-A$JCEF_OUTPUT_PATH_OPTION=${extension.typescriptOutputPath.get().asFile.absolutePath}",
						"-A$JCEF_WEB_COMMUNICATION_ENABLED_OPTION=${extension.webCommunication.isPresent}"
					)
				)
			}
		}
	}
}
