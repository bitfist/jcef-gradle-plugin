import git.semver.plugin.gradle.GitSemverPluginExtension
import git.semver.plugin.gradle.ReleaseTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
	kotlin("jvm")
	`java-gradle-plugin`
	id("io.github.bitfist.gradle-github-support.release")
}

dependencies {
	implementation(kotlin("stdlib"))
	implementation(gradleApi())
	implementation(libs.plugin.springBoot)
	implementation(libs.plugin.dependencyManagement)

	testImplementation(platform(libs.test.junit5Bom))
	testImplementation("org.junit.jupiter:junit-jupiter-api")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// region Build

group = "io.github.bitfist"

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
	property("springJcef", libs.versions.springJcef.get())
}

tasks.named("processResources") {
	dependsOn(generateVersionProperties)
}

tasks.withType<KotlinCompile> {
	compilerOptions {
		jvmTarget.set(JvmTarget.fromTarget(javaVersion.majorVersion))
	}
}

gradlePlugin {
	plugins {
		create("io.github.bitfist.jcef-gradle-plugin") {
			id = "io.github.bitfist.jcef-gradle-plugin"
			implementationClass = "io.github.bitfist.jcef.gradle.JcefPlugin"
			version = project.version
			description = "JCEF Gradle plugin for jcef-spring-boot-starter"
			displayName = "JCEF Gradle plugin"
			tags.set(listOf("CEF", "JCEF", "Chromium Embedded", "Browser"))
		}
	}
	website.set("https://github.com/bitfist/jcef-gradle-plugin")
	vcsUrl.set("https://github.com/bitfist/jcef-gradle-plugin")
}

// endregion

tasks.test {
	useJUnitPlatform()
}

project.afterEvaluate {
	tasks.register("releaseVersion", ReleaseTask::class.java, extensions.getByType<GitSemverPluginExtension>())
}

gitHubRelease {
	projectName.set("JCEF Gradle plugin")
	projectDescription.set("Gradle plugin for jcef-spring-boot-starter")
	developer.set("bitfist")
	licenseFile.set(rootProject.projectDir.resolve("../LICENSE.txt"))
	license.set("The Apache License, Version 2.0")
	licenseUri.set(URI("https://www.apache.org/licenses/LICENSE-2.0"))
}
