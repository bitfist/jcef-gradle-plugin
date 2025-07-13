package example;

import io.github.bitfist.jcef.spring.query.CefQueryHandler;
import io.github.bitfist.jcef.spring.query.TypeScriptConfiguration;

@CefQueryHandler
@TypeScriptConfiguration(packageName = "test.example")
class RandomQueryHandler {

	@CefQueryHandler("simple")
	int simpleQuery(String query) {
		return (int)(Math.random() * 100);
	}
}
