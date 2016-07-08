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

public class PrecisionRecallTest {

	public static TestCaseStats test(String targetClass, String srcPath, String expectedOutputDir) {
		String className = getClassName(targetClass);
		String actualOutputFile = "tmp" + File.separator + className + "_out.txt";
		String expectedOutputFile = expectedOutputDir + className + "_expected.txt";
		String message = "=== Test " + targetClass + " ===";
		
		Toradocu.main(new String[] {"--targetClass", targetClass,
				"--saveConditionTranslatorOutput", actualOutputFile,
				"--oracleGeneration", "false",
				"--testClass", "foo",
//				"--debug",
				"-J-sourcepath=" + srcPath,
				"-J-docletpath=build/classes/main",
				"-J-d=tmp", "-J-quiet="});
		return compare(actualOutputFile, expectedOutputFile, message);
	}
	
	private static String getClassName(String qualifiedClassName) {
		return qualifiedClassName.substring(qualifiedClassName.lastIndexOf(".") + 1);
	}

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
