import git.semver.plugin.gradle.GitSemverPluginExtension
import git.semver.plugin.gradle.ReleaseTask
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.jvm.java

plugins {
	kotlin("jvm")
	`java-gradle-plugin`
	`maven-publish`
	alias(libs.plugins.pluginPublish)
	id("io.github.bitfist.github.release")
	id("io.github.bitfist.github.repository")
}

dependencies {
	implementation(kotlin("stdlib"))
	implementation(gradleApi())
	implementation(libs.springBootPlugin)
	implementation(libs.dependencyManagement)

	testImplementation(platform(libs.junit5Bom))
	testImplementation("org.junit.jupiter:junit-jupiter-api")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// region Build

val javaVersion = JavaVersion.VERSION_21
java {
	sourceCompatibility = javaVersion
	targetCompatibility = javaVersion
}

val generateVersionProperties by tasks.registering(WriteProperties::class) {
	group = "build"
	description = "Writes versions.properties"

	destinationFile.set(layout.buildDirectory.file("resources/main/versions.properties"))

	property("springBoot", libs.versions.springBoot.get())
	property("jcef", libs.versions.jcef.get())
}

tasks.named<ProcessResources>("processResources") {
	dependsOn(generateVersionProperties)
}

tasks.withType<KotlinCompile> {
	compilerOptions {
		jvmTarget.set(JvmTarget.fromTarget(javaVersion.majorVersion))
	}
}

gradlePlugin {
	plugins {
		create(property("ID").toString()) {
			id = property("ID").toString()
			implementationClass = property("IMPLEMENTATION_CLASS").toString()
			version = property("VERSION").toString()
			description = property("DESCRIPTION").toString()
			displayName = property("DISPLAY_NAME").toString()
			tags.set(listOf("CEF", "JCEF", "Chromium Embedded", "Browser"))
		}
	}
	website.set(property("WEBSITE").toString())
	vcsUrl.set(property("VCS_URL").toString())
}

// endregion

tasks.test {
	useJUnitPlatform()
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			groupId = project.group.toString()
			artifactId = "jcef-gradle-plugin"
			version = project.version.toString()

			from(components["java"])
		}
	}
}

tasks.register<ReleaseTask>("releaseVersion", ReleaseTask::class.java, extensions.getByType<GitSemverPluginExtension>())
