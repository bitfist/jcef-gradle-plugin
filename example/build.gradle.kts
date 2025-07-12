plugins {
    id("io.github.bitfist.jcef")
}

repositories {
    mavenLocal()
    mavenCentral()
}

jcef {
    typescriptOutputPath.set(projectDir.resolve("src/main/webapp"))
}
