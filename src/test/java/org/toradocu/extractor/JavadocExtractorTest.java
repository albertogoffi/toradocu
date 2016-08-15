package org.toradocu.extractor;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
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

	 
	/* This test case tests the JavadocExtractor on the example class example.AClass in src/test/resources/example */
	@Test
	public void exampleAClassTest() {
		List<DocumentedMethod> expected = new ArrayList<>();
		
		Builder constructor1 = new Builder("", "example.AClass");
		constructor1.tag(new ThrowsTag("java.lang.NullPointerException", "always"));
		expected.add(constructor1.build());
		
		Builder constructor2 = new Builder("", "example.AClass", new Parameter("java.lang.String", "x"));
		constructor2.tag(new ThrowsTag("java.lang.NullPointerException", "if x is null"));
		expected.add(constructor2.build());
		
		Builder foo = new Builder("", "example.AClass.foo", new Parameter("int[]", "array"));
		foo.tag(new ThrowsTag("java.lang.NullPointerException", "if array is null"));
		expected.add(foo.build());
		
		Builder bar = new Builder("", "example.AClass.bar", new Parameter("java.lang.Object", "x"), new Parameter("java.lang.Object", "y"));
		bar.tag(new ThrowsTag("java.lang.IllegalArgumentException", "if x is null"));
		expected.add(bar.build());
		
		Builder baz = new Builder("", "example.AClass.baz", new Parameter("java.lang.Object", "x"));
		baz.tag(new ThrowsTag("java.lang.IllegalArgumentException", "if x is null"));
		expected.add(baz.build());
		
		test("example.AClass", expected, testResources + "/exampleTest_extractor_output.txt", testResources);
	}
	
	/* This test case tests the JavadocExtractor on the example class example.AChild in src/test/resources/example */
	@Test
	public void exampleAChildTest() {
		List<DocumentedMethod> expected = new ArrayList<>();
		
		Builder baz = new Builder("", "example.AChild.baz", new Parameter("java.lang.Object", "z"));
		baz.tag(new ThrowsTag("java.lang.IllegalArgumentException", "if z is null"));
		expected.add(baz.build());
		
		Builder foo = new Builder("", "example.AClass.foo", new Parameter("int[]", "array"));
		foo.tag(new ThrowsTag("java.lang.NullPointerException", "if array is null"));
		expected.add(foo.build());
		
		Builder bar = new Builder("", "example.AClass.bar", new Parameter("java.lang.Object", "x"), new Parameter("java.lang.Object", "y"));
		bar.tag(new ThrowsTag("java.lang.IllegalArgumentException", "if x is null"));
		expected.add(bar.build());
		
		Builder vararg = new Builder("", "example.AClass.vararg", new Parameter("java.lang.Object...", "x"));
		vararg.tag(new ThrowsTag("java.lang.IllegalArgumentException", "if x is null"));
		expected.add(vararg.build());
		
		test("example.AChild", expected, testResources + "/exampleTest_extractor_output.txt", testResources);
	}
	
	private void test(String targetClass, List<DocumentedMethod> expected, String actualOutput, String sourcePath) {
		Toradocu.main(new String[] {"--targetClass", targetClass,
				"--saveJavadocExtractorOutput", actualOutput,
				"--conditionTranslation", "false",
				"--oracleGeneration", "false",
				"--testClass", "foo",
				"-J-sourcepath=" + sourcePath,
				"-J-docletpath=build/classes/main",
				"-J-d=" + toradocuOutputDir});
		
		Type listType = new TypeToken<List<DocumentedMethod>>(){}.getType();
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
