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
	private final Type npe = new Type("java.lang.NullPointerException");
	private final Type iae = new Type("java.lang.IllegalArgumentException");

	/**
	 * Tests {@code JavadocExtractor} on the example class example.AClass in src/test/resources/example
	 */
	@Test
	public void exampleAClassTest() {
		List<DocumentedMethod> expected = new ArrayList<>();
		Type aClass = new Type("example.AClass");
		
		Builder constructor1 = new Builder(aClass, "AClass", null);
		constructor1.tag(new ThrowsTag(npe, "always"));
		expected.add(constructor1.build());
		
		Builder constructor2 = new Builder(aClass, "AClass", null, new Parameter(new Type("java.lang.String"), "x", 0));
		constructor2.tag(new ThrowsTag(npe, "if x is null"));
		expected.add(constructor2.build());
		
		Builder foo = new Builder(aClass, "foo", doubleType, new Parameter(new Type("int[]"), "array", 0, true));
		foo.tag(new ThrowsTag(npe, "if array is null"));
		expected.add(foo.build());
		
		Builder bar = new Builder(aClass, "bar", doubleType, new Parameter(objectType, "x", 0, false), new Parameter(objectType, "y", 1, false));
		bar.tag(new ThrowsTag(iae, "if x is null"));
		expected.add(bar.build());
		
		Builder baz = new Builder(aClass, "baz", doubleType, new Parameter(objectType, "x", 0));
		baz.tag(new ThrowsTag(iae, "if x is null"));
		expected.add(baz.build());
		
		test("example.AClass", expected, testResources + "/example.AClass_extractor_output.txt", testResources);
	}
	
	/**
	 * Tests {@code JavadocExtractor} on the example class example.AChild in src/test/resources/example
	 */ 
	@Test
	public void exampleAChildTest() {
		List<DocumentedMethod> expected = new ArrayList<>();
		Type aClass = new Type("example.AClass");
		Type aChild = new Type("example.AChild");
		
		Builder baz = new Builder(aChild, "baz", doubleType, new Parameter(objectType, "z", 0));
		baz.tag(new ThrowsTag(iae, "if z is null"));
		expected.add(baz.build());
		
		Builder vararg = new Builder(aChild, "vararg", doubleType, true, new Parameter(objectArrayType, "x", 0));
		vararg.tag(new ThrowsTag(iae, "if x is null"));
		expected.add(vararg.build());
		
		Builder foo = new Builder(aClass, "foo", doubleType, new Parameter(new Type("int[]"), "array", 0, true));
		foo.tag(new ThrowsTag(npe, "if array is null"));
		expected.add(foo.build());
		
		Builder bar = new Builder(aClass, "bar", doubleType, new Parameter(objectType, "x", 0, false), new Parameter(objectType, "y", 1, false));
		bar.tag(new ThrowsTag(iae, "if x is null"));
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
