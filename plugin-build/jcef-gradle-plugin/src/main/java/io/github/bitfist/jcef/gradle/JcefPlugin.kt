package io.github.bitfist.jcef.gradle

import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.springframework.boot.gradle.dsl.SpringBootExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin
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
	override fun apply(project: Project) {
		val extension = project.extensions.create(EXTENSION_NAME, JcefExtension::class.java, project)
		applyPlugins(project)
		applyDependencyManagement(project)
		addDependencies(project)
		configureJava(project, extension)
	}

	private fun applyPlugins(project: Project) {
		project.pluginManager.apply(JavaPlugin::class.java)
		project.pluginManager.apply(SpringBootPlugin::class.java)
		project.extensions.configure(SpringBootExtension::class.java) {
			it.buildInfo()
		}
	}

	private fun applyDependencyManagement(project: Project) {
		val versions = loadVersions()
		val springBootVersion = versions.getProperty("springBoot") ?: throw GradleException("Property 'springBoot' not found in versions.properties")

		project.pluginManager.apply("io.spring.dependency-management")
		project.extensions.configure(DependencyManagementExtension::class.java) { management ->
			management.imports {
				it.mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
			}
		}
	}

	private fun addDependencies(project: Project) {
		val versions = loadVersions()
		val springJcefVersion = versions.getProperty("springJcef") ?: throw GradleException("Property 'springJcef' not found in versions.properties")
		val springBootVersion = versions.getProperty("springBoot") ?: throw GradleException("Property 'springBoot' not found in versions.properties")

		project.dependencies.apply {
			add("implementation", "io.github.bitfist:jcef-spring-boot-starter:$springJcefVersion")
			add("annotationProcessor", "io.github.bitfist:jcef-spring-boot-starter:$springJcefVersion")
			add("annotationProcessor", "org.springframework.boot:spring-boot-autoconfigure-processor:$springBootVersion")
		}
	}

	private fun loadVersions(): Properties {
		val props = Properties()
		val resourceStream = javaClass.classLoader.getResourceAsStream("versions.properties") ?: throw GradleException("versions.properties not found in classpath")
		resourceStream.use { props.load(it) }
		return props
	}

	private fun configureJava(project: Project, extension: JcefExtension) {
		project.afterEvaluate {
			project.tasks.withType(JavaCompile::class.java).configureEach { compile ->
				compile.options.encoding = "UTF-8"
				if (extension.developmentMode.get()) {
					compile.options.compilerArgs.addAll(
						listOf(
							"-Ajcef.output.service.type=web",
							"-Ajcef.output.web.host=${extension.developmentHost.get()}",
							"-Ajcef.output.web.port=${extension.developmentPort.get()}",
						)
					)
				} else {
					compile.options.compilerArgs.add("-Ajcef.output.service.type=query")
				}
				compile.options.compilerArgs.addAll(
					listOf(
						"-parameters",
						"-Ajcef.output.path=${extension.typescriptOutputPath.get().asFile.absolutePath}"
					)
				)
			}
		}
	}
}
