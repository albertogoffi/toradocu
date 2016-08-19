package org.toradocu.extractor;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.toradocu.Toradocu;
import org.toradocu.extractor.DocumentedMethod.Builder;
import org.toradocu.util.GsonInstance;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JavadocExtractorTest {

	private final String toradocuOutputDir = "tmp";
	private final String testResources = "src/test/resources";
	private final Type doubleType = new Type("double");
	private final Type objectType = new Type("java.lang.Object");
	private final Type objectArrayType = new Type("java.lang.Object[]");

	/**
	 * Tests {@code JavadocExtractor} on the example class example.AClass in src/test/resources/example
	 */
	@Test
	public void exampleAClassTest() {
		List<DocumentedMethod> expected = new ArrayList<>();
		
		Builder constructor1 = new Builder("example.AClass", null);
		constructor1.tag(new ThrowsTag("java.lang.NullPointerException", "always"));
		expected.add(constructor1.build());
		
		Builder constructor2 = new Builder("example.AClass", null, new Parameter(new Type("java.lang.String"), "x", 0));
		constructor2.tag(new ThrowsTag("java.lang.NullPointerException", "if x is null"));
		expected.add(constructor2.build());
		
		Builder foo = new Builder("example.AClass.foo", doubleType, new Parameter(new Type("int[]"), "array", 0));
		foo.tag(new ThrowsTag("java.lang.NullPointerException", "if array is null"));
		expected.add(foo.build());
		
		Builder bar = new Builder("example.AClass.bar", doubleType, new Parameter(objectType, "x", 0), new Parameter(objectType, "y", 1));
		bar.tag(new ThrowsTag("java.lang.IllegalArgumentException", "if x is null"));
		expected.add(bar.build());
		
		Builder baz = new Builder("example.AClass.baz", doubleType, new Parameter(objectType, "x", 0));
		baz.tag(new ThrowsTag("java.lang.IllegalArgumentException", "if x is null"));
		expected.add(baz.build());
		
		test("example.AClass", expected, testResources + "/example.AClass_extractor_output.txt", testResources);
	}
	
	/**
	 * Tests {@code JavadocExtractor} on the example class example.AChild in src/test/resources/example
	 */ 
	@Test
	public void exampleAChildTest() {
		List<DocumentedMethod> expected = new ArrayList<>();
		
		Builder baz = new Builder("example.AChild.baz", doubleType, new Parameter(objectType, "z", 0));
		baz.tag(new ThrowsTag("java.lang.IllegalArgumentException", "if z is null"));
		expected.add(baz.build());
		
		Builder vararg = new Builder("example.AChild.vararg", doubleType, true, new Parameter(objectArrayType, "x", 0));
		vararg.tag(new ThrowsTag("java.lang.IllegalArgumentException", "if x is null"));
		expected.add(vararg.build());
		
		Builder foo = new Builder("example.AClass.foo", doubleType, new Parameter(new Type("int[]"), "array", 0));
		foo.tag(new ThrowsTag("java.lang.NullPointerException", "if array is null"));
		expected.add(foo.build());
		
		Builder bar = new Builder("example.AClass.bar", doubleType, new Parameter(objectType, "x", 0), new Parameter(objectType, "y", 1));
		bar.tag(new ThrowsTag("java.lang.IllegalArgumentException", "if x is null"));
		expected.add(bar.build());
		
		test("example.AChild", expected, testResources + "/example.AChild_extractor_output.txt", testResources);
	}
	
	private void test(String targetClass, List<DocumentedMethod> expected, String actualOutput, String sourcePath) {
		Toradocu.main(new String[] {"--target-class", targetClass,
				"--javadoc-extractor-output", actualOutput,
				"--condition-translation", "false",
				"--oracle-generation", "false",
				"--test-class", "foo",
				"--source-dir", sourcePath,
				"-J-docletpath=build/classes/main",
				"-J-d=" + toradocuOutputDir});
		
		java.lang.reflect.Type listType = new TypeToken<List<DocumentedMethod>>(){}.getType();
		Gson gson = GsonInstance.gson();
		Path ouputFilePath = Paths.get(actualOutput);
		try (BufferedReader reader = Files.newBufferedReader(ouputFilePath)) {
			List<DocumentedMethod> actual = gson.fromJson(reader, listType);
			assertThat(actual, is(equalTo(expected)));
			Files.delete(ouputFilePath);
		} catch(IOException e) {
			fail(e.getMessage());
		}
	}
}
