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

jcef {
	typescriptOutputPath.set(projectDir.resolve("src/main/webapp"))
	developmentMode.set(true)
	developmentHost.set("http://localhost")
	developmentPort.set(8080)
}

