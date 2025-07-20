package io.github.bitfist.jcef.gradle

import io.spring.gradle.dependencymanagement.DependencyManagementPlugin
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.gradle.dsl.SpringBootExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin

@DisplayName("🛠️ JcefPlugin Configuration Tests")
class JcefPluginTest {

	private fun buildProject(): ProjectInternal {
		return ProjectBuilder.builder().build() as ProjectInternal
	}

	@Test
	@DisplayName("🔌 Plugin Application")
	fun `plugin is applied and plugins are registered`() {
		val project = buildProject()
		project.pluginManager.apply(JcefPlugin::class.java)

		project.evaluate()

		// Check that JavaPlugin and SpringBootPlugin applied
		assertTrue(project.plugins.hasPlugin(JavaPlugin::class.java), "JavaPlugin should be applied")
		assertTrue(project.plugins.hasPlugin(SpringBootPlugin::class.java), "SpringBootPlugin should be applied")
		assertTrue(project.plugins.hasPlugin(DependencyManagementPlugin::class.java), "Dependency Management plugin should be applied")

		// Extension exists
		val ext = project.extensions.findByName("springJcef")
		assertNotNull(ext, "JcefExtension should be created")
		assertTrue(ext is JcefExtension, "Extension should be of type JcefExtension")
	}

	@Test
	@DisplayName("📦 Dependency Management Configuration")
	fun `spring boot dependency management is configured`() {
		val project = buildProject()
		project.pluginManager.apply(JcefPlugin::class.java)
		project.extensions.getByType(SpringBootExtension::class.java)

		project.evaluate()

		assertDependencyRegistered(project, "org.springframework.boot", "spring-boot-autoconfigure-processor", "annotationProcessor")
		assertNotNull(project.tasks.named("bootBuildInfo"), "bootBuildInfo task should be registered")
	}

	@Test
	@DisplayName("🔗 Spring JCEF Dependencies")
	fun `spring jcef dependencies are added`() {
		val project = buildProject()
		project.pluginManager.apply(JcefPlugin::class.java)

		project.evaluate()

		assertDependencyRegistered(project, "io.github.bitfist", "jcef-spring-boot-starter")
		assertDependencyRegistered(project, "io.github.bitfist", "jcef-spring-boot-starter", "annotationProcessor")
	}

	@Test
	@DisplayName("⚙️ JavaCompile Options - Production Mode")
	fun `java compile tasks have correct args in production mode`() {
		val project = buildProject()
		project.pluginManager.apply(JcefPlugin::class.java)
		val ext = project.extensions.getByType(JcefExtension::class.java)
		ext.typescriptOutputPath.set(project.layout.buildDirectory.asFile.get().resolve("generated-ts"))
		// Leave developmentMode as default (false)

		project.evaluate()

		val javaCompile = project.tasks.withType(JavaCompile::class.java).first()
		val args = javaCompile.options.compilerArgs

		assertTrue(args.contains("-Ajcef.output.service.type=query"), "Should add query service type in production mode")
		assertTrue(args.contains("-parameters"), "Should include -parameters flag")
		assertTrue(args.any { it.startsWith("-Ajcef.output.path=") }, "Should configure output path")
	}

	@Test
	@DisplayName("🚧 Development Mode")
	fun `java compile tasks have correct args in development mode`() {
		val project = buildProject()
		project.pluginManager.apply(JcefPlugin::class.java)
		val ext = project.extensions.getByType(JcefExtension::class.java)

		ext.typescriptOutputPath.set(project.layout.buildDirectory.asFile.get().resolve("generated-ts"))
		// Enable developmentMode
		ext.developmentMode.set(true)

		project.evaluate()

		val javaCompile = project.tasks.withType(JavaCompile::class.java).first()
		val args = javaCompile.options.compilerArgs

		assertDependencyRegistered(project, "org.springframework.boot", "spring-boot-starter-web")
		assertTrue(args.contains("-Ajcef.output.service.type=web"), "Should add web service type in development mode")
		assertTrue(args.contains("-Ajcef.output.web.host=http://localhost"), "Should include development host")
		assertTrue(args.contains("-Ajcef.output.web.port=8080"), "Should include development port")
		assertTrue(args.contains("-parameters"), "Should include -parameters flag")
		assertTrue(args.any { it.startsWith("-Ajcef.output.path=") }, "Should configure output path")
	}

	private fun assertDependencyRegistered(project: ProjectInternal, group: String, name: String, scope: String = "implementation") {
		val dependencies = project.configurations.getByName(scope).dependencies
		assertTrue(
			dependencies.any { dependency -> dependency.group == group && dependency.name == name },
			"Expected $group:$name to be on the $scope configuration"
		)
	}
}
