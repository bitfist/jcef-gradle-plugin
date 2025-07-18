package io.github.bitfist.jcef.gradle

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

@Suppress("UnnecessaryAbstractClass")
abstract class JcefExtension
@Inject
constructor(project: Project) {
	private val objects = project.objects

	val typescriptOutputPath: RegularFileProperty = objects.fileProperty()
	val developmentMode: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
	val developmentHost: Property<String> = objects.property(String::class.java).convention("http://localhost")
	val developmentPort: Property<Int> = objects.property(Int::class.java).convention(3000)
}
