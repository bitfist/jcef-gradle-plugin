package io.github.bitfist.jcef.gradle

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

@Suppress("UnnecessaryAbstractClass")
abstract class JcefExtension @Inject constructor(project: Project) {
	private val objects = project.objects

	val typescriptOutputPath: RegularFileProperty = objects.fileProperty()
	val webCommunication: Property<WebCommunication> = objects.property(WebCommunication::class.java).convention(null as WebCommunication?)

	fun enableWebCommunication(modifier: WebCommunication.() -> Unit = {}) {
		if (!webCommunication.isPresent) {
			webCommunication.set(WebCommunication())
		}
		webCommunication.get().modifier()
	}
}

data class WebCommunication(
	var backendUri: String = "http://localhost:8080",
	var frontendUri: String = "http://localhost:3000"
)
