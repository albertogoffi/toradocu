package org.toradocu.extractor;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.toradocu.Toradocu;
import org.toradocu.extractor.Method.Builder;
import org.toradocu.util.GsonInst;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JavadocExtractorTest {

	private final String toradocuOutputDir = "tmp";
//	private final String guavaSrc = "src/test/resources/guava-19.0-sources";
//	private final String guavaExpectedOutputPath = "src/test/resources/Guava-19/";
//	private final String commonsCollectionsExpectedOutputPath = "src/test/resources/CommonsCollections-4.1/";
//	private final String commonsCollectionsSrc = "src/test/resources/commons-collections4-4.1-src/src/main/java";
	private final String testResources = "src/test/resources";

	// Test with 'example' application 
	@Test
	public void exampleTest() throws Throwable {
		List<Method> expected = new ArrayList<>();
		
		Builder foo = new Builder("example.AClass.foo", new Parameter("int[]", "array"));
		foo.tag(new ThrowsTag("java.lang.NullPointerException", "if array is null"));
		expected.add(foo.build());
		
		Builder bar = new Builder("example.AClass.bar", new Parameter("java.lang.Object", "x"), new Parameter("java.lang.Object", "y"));
		bar.tag(new ThrowsTag("java.lang.IllegalArgumentException", "if x is null"));
		expected.add(bar.build());
		
		Builder baz = new Builder("example.AClass.baz", new Parameter("java.lang.Object", "x"));
		baz.tag(new ThrowsTag("java.lang.IllegalArgumentException", "if x is null"));
		expected.add(baz.build());
		
		test("example.AClass", expected, testResources + "/exampleTest_extractor_output.txt", testResources);
	}
	
	// Tests with Guava subjects
	
//	@Test
//	public void moreObjectsTest() {
//		String output = toradocuOutputDir + File.separator + "MoreObjects" + "_extractor_out.txt";
//		String expectedOutput = guavaExpectedOutputPath + "MoreObjects" + "_extractor_expected.txt";
//		test("com.google.common.base.MoreObjects", output, expectedOutput, guavaSrc);
//	}
//	
//	// Tests with Commons Collections subjects
//	
//	@Test
//	public void arrayStackTest() {
//		String output = toradocuOutputDir + File.separator + "ArrayStack" + "_extractor_out.txt";
//		String expectedOutput = commonsCollectionsExpectedOutputPath + "ArrayStack" + "_extractor_expected.txt";
//		test("org.apache.commons.collections4.ArrayStack", output, expectedOutput, commonsCollectionsSrc);
//	}
	
	private void test(String targetClass, List<Method> expected, String actualOutput, String sourcePath) throws IOException {
		Toradocu.main(new String[] {"--targetClass", targetClass,
				"--saveJavadocExtractorOutput", actualOutput,
				"--conditionTranslation", "false",
				"--oracleGeneration", "false",
				"--testClass", "foo",
				"-J-sourcepath=" + sourcePath,
				"-J-docletpath=build/classes/main",
				"-J-d=" + toradocuOutputDir});
		
		Type listType = new TypeToken<List<Method>>(){}.getType();
		Gson gson = GsonInst.gson();
		BufferedReader reader = Files.newBufferedReader(Paths.get(actualOutput));
		List<Method> actual = gson.fromJson(reader, listType);
		
		assertEquals(expected, actual);
	}
	
//	private void compare(String outputFile, String expectedOutputFile) {
//		try (BufferedReader outFile = Files.newBufferedReader(Paths.get(outputFile));
//			 BufferedReader expFile = Files.newBufferedReader(Paths.get(expectedOutputFile))) {
//			List<String> output = outFile.lines().collect(Collectors.toList());
//			output.remove(0); // We remove the header that is present only in the actual output
//			List<String> expected = expFile.lines().collect(Collectors.toList());
//			
//			assertThat(output.size(), is(expected.size()));			
//			for (int i = 0; i < output.size(); i++) {
//				String actualLine = output.get(i);
//				String expectedLine = expected.get(i);
//				assertThat(actualLine, is(expectedLine));
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//	}
}
