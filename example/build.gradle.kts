import io.github.bitfist.github.repository.gitHub

plugins {
	id("io.github.bitfist.github.repository")
	id("io.github.bitfist.jcef")
}

repositories {
	gitHub("bitfist/jcef-spring-boot-starter")
	mavenLocal()
	mavenCentral()
}

jcef {
	typescriptOutputPath.set(projectDir.resolve("src/main/webapp"))
	developmentMode.set(true)
}

