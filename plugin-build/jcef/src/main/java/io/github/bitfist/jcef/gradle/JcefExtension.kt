package io.github.bitfist.jcef.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property
import java.io.File
import javax.inject.Inject

@Suppress("UnnecessaryAbstractClass")
abstract class JcefExtension
@Inject
constructor(project: Project) {
	private val objects = project.objects

	val typescriptOutputPath: Property<File> = objects.property(File::class.java)
}
