package org.toradocu.testlib;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.toradocu.Toradocu;

/**
 * PrecisionRecallTest contains static methods to perform a precision recall
 * test using Toradocu.
 */
public class PrecisionRecallTest {

	/**
	 * Runs Toradocu on the given class and collects data on its precision and
	 * recall.
	 *
	 * @param targetClass the fully qualified name of the class on which to
	 *        run the test
	 * @param srcPath the source path for the given targetClass
	 * @param expectedOutputDir the path of the directory containing the
	 *        expected output for the targetClass.
	 * @return statistics for the test
	 */
	public static TestCaseStats test(String targetClass, String srcPath, String expectedOutputDir) {
		String className = getClassName(targetClass);
		String actualOutputFile = "tmp" + File.separator + className + "_out.txt";
		String expectedOutputFile = Paths.get(expectedOutputDir, className + "_expected.txt").toString();
		String message = "=== Test " + targetClass + " ===";
		
		Toradocu.main(new String[] {"--target-class", targetClass,
				"--condition-translator-output", actualOutputFile,
				"--oracle-generation", "false",
				"--test-class", "foo",
				"--source-dir", srcPath,
				"-J-docletpath=build/classes/main",
				"-J-d=tmp"});
		return compare(actualOutputFile, expectedOutputFile, message);
	}
	
	/**
	 * Returns a simple class name for the class with the given qualified
	 * name.
	 *
	 * @param qualifiedClassName the qualified name of the class
	 * @return the simple name of the class
	 */
	private static String getClassName(String qualifiedClassName) {
		return qualifiedClassName.substring(qualifiedClassName.lastIndexOf(".") + 1);
	}

	/**
	 * Compares the output file and the expected output file. Calculates
	 * statistics on precision and recall and prints the results.
	 *
	 * @param outputFile the file containing the actual test output
	 * @param expectedOutputFile the file containing the expected test output
	 * @param message a message to print before all other output
	 * @return statistics on precision and recall for the test
	 */
	private static TestCaseStats compare(String outputFile, String expectedOutputFile, String message) {
		StringBuilder report = new StringBuilder();
		
		report.append(message + "\n");
		try (BufferedReader outFile = Files.newBufferedReader(Paths.get(outputFile));
			 BufferedReader expFile = Files.newBufferedReader(Paths.get(expectedOutputFile))) {
			List<String> output = outFile.lines().collect(Collectors.toList());
			List<String> expected = expFile.lines().collect(Collectors.toList());
			TestCaseStats result = new TestCaseStats(expected.size());
			for (String line : output) {
				if (!line.endsWith("==> []")) { // If Toradocu output is not empty (important to get correct recall)
					if (expected.contains(line)) {
						result.incrementTP();
					} else {
						result.incrementFP();
						report.append("Wrong condition: " + line + "\n");
					}
				} else {
					report.append("Missing condition:" + line + "\n");
				}
			}
			double precision = result.getPrecision();
			double recall = result.getRecall();
			report.append("Conditions: " + expected.size() + "\n");
			report.append("Precision: " + String.format("%.2f", precision) + "\n");
			report.append("Recall: " + String.format("%.2f", recall) + "\n");
			System.out.println(report);
			return result;
		} catch (IOException e) {
			fail(e.getMessage());
			return null;
		}
	}
}
