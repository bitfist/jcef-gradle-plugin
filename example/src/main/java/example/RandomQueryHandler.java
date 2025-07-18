package example;

import io.github.bitfist.jcef.spring.tsobject.TypeScriptConfiguration;
import io.github.bitfist.jcef.spring.tsobject.TypeScriptObject;

@TypeScriptObject
@TypeScriptConfiguration(path = "test/example")
class RandomQueryHandler {

	int simpleQuery(String query) {
		return (int)(Math.random() * 100);
	}
}
