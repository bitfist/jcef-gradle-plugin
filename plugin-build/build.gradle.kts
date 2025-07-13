plugins {
    alias(libs.plugins.kotlin) apply false
}

tasks.register("clean", Delete::class.java) {
    delete(rootProject.layout.buildDirectory)
}
