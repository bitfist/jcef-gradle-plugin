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
import org.springframework.boot.gradle.tasks.run.BootRun

@DisplayName("🛠️ JcefPlugin Configuration Tests")
class JcefPluginTest {

	private fun buildProject(): ProjectInternal {
		val project = ProjectBuilder.builder().build() as ProjectInternal
		project.extensions.extraProperties.set("GPR_USER", "test")
		project.extensions.extraProperties.set("GPR_KEY", "test")
		return project
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

		assertTrue(args.contains("-A${JcefPlugin.JCEF_WEB_COMMUNICATION_ENABLED_OPTION}=false"), "Should add query service type in production mode")
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
		ext.enableWebCommunication()

		project.evaluate()

		val javaCompile = project.tasks.withType(JavaCompile::class.java).first()
		val compilerArgs = javaCompile.options.compilerArgs

		assertDependencyRegistered(project, "org.springframework.boot", "spring-boot-starter-web")
		assertTrue(compilerArgs.contains("-A${JcefPlugin.JCEF_WEB_COMMUNICATION_ENABLED_OPTION}=true"), "Should add web service type in development mode")
		assertTrue(compilerArgs.contains("-A${JcefPlugin.WEB_BACKEND_URI_OPTION}=http://localhost:8080"), "Should include development host")
		assertTrue(compilerArgs.contains("-parameters"), "Should include -parameters flag")
		assertTrue(compilerArgs.any { it.startsWith("-A${JcefPlugin.JCEF_OUTPUT_PATH_OPTION}=") }, "Should configure output path")

		val bootRun = project.tasks.withType(BootRun::class.java).first()
		assertTrue(bootRun.jvmArgs?.contains("-D${JcefPlugin.JVM_ARG_ENABLE_WEB_COMMUNICATION}=true") ?: false, "Should add jvm arg for web communication")
		assertTrue(bootRun.jvmArgs?.contains("-D${JcefPlugin.JVM_ARG_WEB_FRONTEND_URI}=${ext.webCommunication.get().frontendUri}") ?: false, "Should add jvm arg for web frontend uri")
	}

	private fun assertDependencyRegistered(project: ProjectInternal, group: String, name: String, scope: String = "implementation") {
		val dependencies = project.configurations.getByName(scope).dependencies
		assertTrue(
			dependencies.any { dependency -> dependency.group == group && dependency.name == name },
			"Expected $group:$name to be on the $scope configuration"
		)
	}
}
