package org.toradocu.extractor;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.Toradocu;
import org.toradocu.util.GsonInstance;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JavadocExtractorTest {

	private final String testResources = "src/test/resources";
	private final Type doubleType = new Type("double");
	private final Type objectType = new Type("java.lang.Object");
	private final Type objectArrayType = new Type("java.lang.Object[]");
	private final Type npe = new Type("java.lang.NullPointerException");
	private final Type iae = new Type("java.lang.IllegalArgumentException");
	private final Logger LOG = LoggerFactory.getLogger(JavadocExtractorTest.class);

	/**
	 * Tests {@code JavadocExtractor} on the example class example.AClass in src/test/resources/example
	 */
	@Test
	public void exampleAClassTest() {
	    List<Parameter> params = new ArrayList<>();
	    List<ThrowsTag> tags = new ArrayList<>();
		List<DocumentedMethod> expected = new ArrayList<>();
		Type aClass = new Type("example.AClass");
		
		tags.add(new ThrowsTag(npe, "always"));
		expected.add(new DocumentedMethod(aClass, "AClass", null, null, false, tags));
		
		params.add(new Parameter(new Type("java.lang.String"), "x"));
		tags.clear();
		tags.add(new ThrowsTag(npe, "if x is null"));
		tags.add(new ThrowsTag(new Type("example.exception.AnException"), "if x is empty"));
		expected.add(new DocumentedMethod(aClass, "AClass", null, params, false, tags));
		
		params.clear();
		params.add(new Parameter(new Type("int[]"), "array", true));
		tags.clear();
		tags.add(new ThrowsTag(npe, "if array is null"));
		expected.add(new DocumentedMethod(aClass, "foo", doubleType, params, false, tags));
		
		params.clear();
        params.add(new Parameter(objectType, "x", false));
        params.add(new Parameter(objectType, "y", false));
		tags.clear();
        tags.add(new ThrowsTag(iae, "if x is null"));
		expected.add(new DocumentedMethod(aClass, "bar", doubleType, params, false, tags));
		
		params.clear();
        params.add(new Parameter(objectType, "x"));
        tags.clear();
        tags.add(new ThrowsTag(iae, "if x is null"));
		expected.add(new DocumentedMethod(aClass, "baz", doubleType, params, false, tags));
		
		test("example.AClass", expected, testResources + "/example.AClass_extractor_output.txt", testResources);
	}
	
	/**
	 * Tests {@code JavadocExtractor} on the example class example.AChild in src/test/resources/example
	 */ 
	@Test
	public void exampleAChildTest() {
	    List<Parameter> params = new ArrayList<>();
        List<ThrowsTag> tags = new ArrayList<>();
		List<DocumentedMethod> expected = new ArrayList<>();
		Type aClass = new Type("example.AClass");
		Type aChild = new Type("example.AChild");
		
		params.add(new Parameter(objectType, "z"));
		tags.add(new ThrowsTag(iae, "if z is null"));
		expected.add(new DocumentedMethod(aChild, "baz", doubleType, params, false, tags));
		
		params.clear();
        params.add(new Parameter(objectArrayType, "x"));
        tags.clear();
        tags.add(new ThrowsTag(iae, "if x is null"));
		expected.add(new DocumentedMethod(aChild, "vararg", doubleType, params, true, tags));
		
		params.clear();
		params.add(new Parameter(new Type("int[]"), "array", true));
		tags.clear();
		tags.add(new ThrowsTag(npe, "if array is null"));
		expected.add(new DocumentedMethod(aClass, "foo", doubleType, params, false, tags));
		
		params.clear();
        params.add(new Parameter(objectType, "x", false));
        params.add(new Parameter(objectType, "y", false));
        tags.clear();
        tags.add(new ThrowsTag(iae, "if x is null"));
		expected.add(new DocumentedMethod(aClass, "bar", doubleType, params, false, tags));
		        
		test("example.AChild", expected, testResources + "/example.AChild_extractor_output.txt", testResources);
	}
	
	private void test(String targetClass, List<DocumentedMethod> expected, String actualOutput, String sourcePath) {
		Toradocu.main(new String[] {"--target-class", targetClass,
				"--javadoc-extractor-output", actualOutput,
				"--condition-translation", "false",
				"--oracle-generation", "false",
				"--source-dir", sourcePath});
		
		java.lang.reflect.Type listType = new TypeToken<List<DocumentedMethod>>(){}.getType();
		Gson gson = GsonInstance.gson();
		Path ouputFilePath = Paths.get(actualOutput);
		try (BufferedReader reader = Files.newBufferedReader(ouputFilePath)) {
			List<DocumentedMethod> actual = gson.fromJson(reader, listType);
			assertThat(actual, is(equalTo(expected)));
		} catch(IOException e) {
			fail(e.getMessage());
		}
		try {
            Files.delete(ouputFilePath);
        } catch (IOException e) {
            LOG.error("Error deleting the file: " + ouputFilePath);
        }
	}
}
