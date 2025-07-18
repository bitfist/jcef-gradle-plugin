package io.github.bitfist.jcef.gradle

import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.gradle.dsl.SpringBootExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin

class JcefPluginTest {

	/**
	 * Verifies that applying the plugin by ID:
	 *  - Registers the 'jcef' extension
	 *  - Applies the Java, Spring Boot, and Dependency Management plugins
	 *  - Configures Spring Boot buildInfo() task
	 */
	@Test
	fun `apply-by-id registers extension and core plugins`() {
		val project = ProjectBuilder.builder().build()

		// Apply plugin via its ID
		project.pluginManager.apply("io.github.bitfist.jcef")

		// Pre-configure the extension so configureJava won't fail
		project.extensions.configure(JcefExtension::class.java) {
			it.typescriptOutputPath.set(project.layout.buildDirectory.dir("ts-output").get().asFile)
		}

		// 1) Extension exists
		val ext = project.extensions.findByName("jcef")
		assertNotNull(ext, "The 'jcef' extension should be registered")
		assertTrue(ext is JcefExtension, "Extension must be a JcefExtension")

		// 2) Java plugin applied
		assertTrue(project.plugins.hasPlugin(JavaPlugin::class.java), "JavaPlugin should be applied")

		// 3) Spring Boot plugin and buildInfo task
		assertTrue(project.plugins.hasPlugin(SpringBootPlugin::class.java), "SpringBootPlugin should be applied")
		assertNotNull(
			project.extensions.findByType(SpringBootExtension::class.java),
			"SpringBootExtension should be present"
		)
		assertNotNull(
			project.tasks.findByName("bootBuildInfo"),
			"buildInfo() should create 'bootBuildInfo' task"
		)

		// 4) Dependency-management plugin applied
		assertTrue(
			project.plugins.hasPlugin("io.spring.dependency-management"),
			"Dependency Management plugin should be applied"
		)
		assertNotNull(
			project.extensions.findByType(DependencyManagementExtension::class.java),
			"DependencyManagementExtension should be present"
		)
	}

	/**
	 * Verifies that the plugin adds the correct dependencies to
	 * 'implementation' and 'annotationProcessor' configurations.
	 */
	@Test
	fun `apply-by-id adds expected dependencies`() {
		val project = ProjectBuilder.builder().build()
		// Apply plugin via its ID
		project.pluginManager.apply("io.github.bitfist.jcef")

		// Ensure extension is set so plugin.apply doesn’t fail
		project.extensions.configure(JcefExtension::class.java) {
			it.typescriptOutputPath.set(project.layout.buildDirectory.dir("ts-output").get().asFile)
		}

		// 'implementation' should contain the JCEF Spring Boot starter
		val implDeps = project.configurations.getByName("implementation").dependencies
		assertTrue(
			implDeps.any { it.group == "io.github.bitfist" && it.name == "jcef-spring-boot-starter" },
			"'implementation' must include io.github.bitfist:jcef-spring-boot-starter"
		)

		// 'annotationProcessor' should contain both the JCEF starter and Spring Boot processor
		val apDeps = project.configurations.getByName("annotationProcessor").dependencies
		assertTrue(
			apDeps.any { it.group == "io.github.bitfist" && it.name == "jcef-spring-boot-starter" },
			"'annotationProcessor' must include the JCEF starter"
		)
		assertTrue(
			apDeps.any { it.group == "org.springframework.boot" && it.name == "spring-boot-autoconfigure-processor" },
			"'annotationProcessor' must include spring-boot-autoconfigure-processor"
		)
	}

	/**
	 * Verifies that the plugin configures all JavaCompile tasks to use UTF-8
	 * and passes the '-parameters' and correct jcef.output.* flags.
	 */
	@Test
	fun `apply-by-id in development mode configures JavaCompile options`() {
		val project = ProjectBuilder.builder().build() as ProjectInternal
		// Apply plugin via its ID
		project.pluginManager.apply("io.github.bitfist.jcef")

		// Pre-set extension so configureJava() runs safely
		project.extensions.configure(JcefExtension::class.java) {
			it.typescriptOutputPath.set(project.layout.buildDirectory.dir("ts-output").get().asFile)
			it.developmentMode.set(true)
			it.developmentHost.set("http://localhost2")
			it.developmentPort.set(3001)
		}

		project.pluginManager.apply("io.github.bitfist.jcef")

		project.evaluate()

		// Grab the compileJava task
		val compileTask = project.tasks.findByName("compileJava") as JavaCompile?
		assertNotNull(compileTask, "compileJava task should exist")

		// Check encoding
		assertEquals("UTF-8", compileTask!!.options.encoding, "Compiler encoding must be UTF-8")

		// Check compiler arguments
		val args = compileTask.options.compilerArgs
		assertTrue(args.contains("-parameters"), "Compiler args must contain '-parameters'")
		assertTrue(args.any { it.startsWith("-Ajcef.output.path=") }, "Compiler args must contain the jcef.output.path flag")
		assertTrue(args.any { it.equals("-Ajcef.output.service.type=web") }, "Compiler args must contain the jcef.output.service.type flag")
		assertTrue(args.any { it.equals("-Ajcef.output.web.host=http://localhost2") }, "Compiler args must contain the jcef.output.web.host flag")
		assertTrue(args.any { it.equals("-Ajcef.output.web.port=3001") }, "Compiler args must contain the jcef.output.web.port flag")
	}

	/**
	 * Verifies that the plugin configures all JavaCompile tasks to use UTF-8
	 * and passes the '-parameters' and correct jcef.output.* flags.
	 */
	@Test
	fun `apply-by-id mode configures JavaCompile options`() {
		val project = ProjectBuilder.builder().build() as ProjectInternal
		// Apply plugin via its ID
		project.pluginManager.apply("io.github.bitfist.jcef")

		// Pre-set extension so configureJava() runs safely
		project.extensions.configure(JcefExtension::class.java) {
			it.typescriptOutputPath.set(project.layout.buildDirectory.dir("ts-output").get().asFile)
		}

		project.pluginManager.apply("io.github.bitfist.jcef")

		project.evaluate()

		// Grab the compileJava task
		val compileTask = project.tasks.findByName("compileJava") as JavaCompile?
		assertNotNull(compileTask, "compileJava task should exist")

		// Check encoding
		assertEquals("UTF-8", compileTask!!.options.encoding, "Compiler encoding must be UTF-8")

		// Check compiler arguments
		val args = compileTask.options.compilerArgs
		assertTrue(args.contains("-parameters"), "Compiler args must contain '-parameters'")
		assertTrue(args.any { it.startsWith("-Ajcef.output.path=") }, "Compiler args must contain the jcef.output.path flag")
		assertTrue(args.any { it.equals("-Ajcef.output.service.type=query") }, "Compiler args must contain the jcef.output.service.type flag")
	}
}
