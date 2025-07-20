import io.github.bitfist.github.repository.gitHub

plugins {
	id("io.github.bitfist.gradle-github-support.repository")
	id("io.github.bitfist.jcef-gradle-plugin")
}

repositories {
	gitHub("bitfist/jcef-spring-boot-starter")
	mavenLocal()
	mavenCentral()
}

springJcef {
	typescriptOutputPath.set(projectDir.resolve("src/main/webapp"))
	enableWebCommunication()
}

