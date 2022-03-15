package org.toradocu.generator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class TestGeneratorSummaryData {
	
	private static TestGeneratorSummaryData _I = null;
	
	private TestGeneratorSummaryData() { }

	public static TestGeneratorSummaryData _I() {
		if (_I == null) {
			_I = new TestGeneratorSummaryData();
		}
		return _I;		
	}
	
	private ArrayList<String> tableRows = new ArrayList<>();
	private int numEvaluators = 0;
	private int numTests = 0;
	private int numTestFailures = 0;
	private int numTestUnmodeled = 0;
	private int numExceptions = 0;
	private int numCompileProblems = 0;
	private boolean error = false;
	private boolean timeout = false;
	private boolean typePointProblem = false;
	private int totalClasses = 0;
	private int totalNumEvaluators = 0;
	private int totalNumTests = 0;
	private int totalNumTestFailures = 0;
	private int totalNumTestUnmodeled = 0;
	private int totalNumExceptions = 0;
	private int totalNumCompileProblems = 0;
	private int totalWithError = 0;
	private int totalWithTimeout = 0;
	private int totalWithTypePointProblem = 0;
	private int numTestCasesWithoutTargetMethod = 0;
	
	private int numPositiveEvaluators = 0;
	private int numNegativeEvaluators = 0;
	private int numUnmodeledGuards = 0;
	private int numEmptyPostConditions = 0;
	private int numUnmodeledPostConditions = 0;
	private int numTestGenerationErrors = 0;
	private int totPositiveEvaluators = 0;
	private int totNegativeEvaluators = 0;
	private int totUnmodeledGuards = 0; 
	private int totEmptyPostConditions = 0;
	private int totUnmodeledPostConditions = 0;
	private int totTestGenerationErrors = 0;
	private int totTestCasesWithoutTargetMethod = 0;
	
	private String[] header = {
			"Target Class",
			"PosEvaluators",
			"NegEvaluators",
			"EvoEvaluators",
			"Tests",
			"TestUnmodeled",
			"TestFailures",
			"Exceptions",
			"Errors",
			"Timeout",
			"CompileProblems",
			"PointItselfError",
			"UnmodeledGuards",
			"EmptyPosts",
			"UnmodeledPosts",
			"TestGenErrors",
			"TestMissMethod",
	};
	
	private String formatHeaders() {
		String format = "";
		for (String h : header) {
			format += "%-" + (h.length() + 2) + "s| ";
		}
		return format;
	}
	private String formatHline() {
		int len = 2 * header.length; //separators
		for (int i = 0; i < header.length; ++i) {
			len += header[i].length() + 2;
		}
		String hline = "-";
		for (int i = 0; i < len; ++i) {
			hline += "-";
		}
		return hline;
	}

	public void printTable() {
		System.out.println(String.format(formatHeaders(), header[0], header[1], header[2], header[3], header[4], header[5], header[6], header[7], header[8], header[9], header[10], header[11], header[12], header[13], header[14], header[15], header[16]));
		System.out.println(formatHline());
		for (String row: tableRows) {
			System.out.println(row);
		}
		System.out.println(String.format(formatHeaders(), "Total (" + totalClasses + " classes)", totPositiveEvaluators, totNegativeEvaluators, totalNumEvaluators, totalNumTests, totalNumTestUnmodeled, totalNumTestFailures, totalNumExceptions, totalWithError, totalWithTimeout, totalNumCompileProblems, totalWithTypePointProblem, totUnmodeledGuards, totEmptyPostConditions, totUnmodeledPostConditions, totTestGenerationErrors, totTestCasesWithoutTargetMethod));						
	}

	public void hline() {
		tableRows.add(formatHline());
	}

	public void addCurrentSummaryAsTableRow(String targetClass) {
		tableRows.add(String.format(formatHeaders(), targetClass, numPositiveEvaluators, numNegativeEvaluators, numEvaluators, numTests, numTestUnmodeled, numTestFailures, numExceptions, error, timeout, numCompileProblems, typePointProblem, numUnmodeledGuards, numEmptyPostConditions, numUnmodeledPostConditions, numTestGenerationErrors, numTestCasesWithoutTargetMethod));				
		totalClasses += 1;
		totalNumEvaluators += numEvaluators;
		totalNumTests += numTests;
		totalNumTestFailures += numTestFailures;
		totalNumTestUnmodeled += numTestUnmodeled;
		totalNumExceptions += numExceptions;
		totalNumCompileProblems += numCompileProblems;
		totalWithError += (error ? 1 : 0);
		totalWithTimeout += (timeout ? 1 : 0);
		totalWithTypePointProblem += (typePointProblem ? 1 : 0);
		totPositiveEvaluators += numPositiveEvaluators;
		totNegativeEvaluators += numNegativeEvaluators;
		totUnmodeledGuards += numUnmodeledGuards; 
		totEmptyPostConditions += numEmptyPostConditions;
		totUnmodeledPostConditions += numUnmodeledPostConditions;
		totTestGenerationErrors += numTestGenerationErrors;
		totTestCasesWithoutTargetMethod += numTestCasesWithoutTargetMethod;

		targetClass = "";
		numEvaluators = 0;
		numTests = 0;
		numTestFailures = 0;
		numTestUnmodeled = 0;
		numExceptions = 0;
		numCompileProblems = 0;
		error = false;
		timeout = false;
		typePointProblem = false;
		numPositiveEvaluators = 0;
		numNegativeEvaluators = 0;
		numUnmodeledGuards = 0;
		numEmptyPostConditions = 0;
		numUnmodeledPostConditions = 0;
		numTestGenerationErrors = 0;
		numTestCasesWithoutTargetMethod = 0;
	}
	
	public void addEvosuiteData(File evosuiteLogFile) {		  
		try (Scanner scanner = new Scanner(evosuiteLogFile)){
			boolean countEnabled = false;
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();		
				if(line.contains("Finished analyzing classpath") || line.contains("Computation finished")) {
					countEnabled = true;
					continue;
				} else if (!countEnabled) {
					continue;
				} else if (line.contains("* EMITTED TEST CASE:")) {
					if (line.contains("failure")) {
						numTestFailures++;
					} else if (line.contains("unmodeled")) {
						numTestUnmodeled++;
					} else {
						numTests++;
					}
				} else if (line.contains("Exception")) {
					if (line.startsWith("\tat ") || line.startsWith("Caused by:")) {
						continue;
					}
					numExceptions++;
				} else if (line.contains("ERROR")) {
					error = true;
				} else if(line.contains("a timeout occurred")) { 
					timeout = true;
				} else if(line.contains("Cannot instantiate path condition evaluator")) {
					numCompileProblems++;
				} else if(line.contains("Type points to itself")) {
					typePointProblem = true;
				} else if (line.contains("* Total number of test goals for DYNAMOSA: ")) {
					numEvaluators += Integer.parseInt(line.substring(line.indexOf("* Total number of test goals for DYNAMOSA: ") +
							"* Total number of test goals for DYNAMOSA: ".length()));
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void incUnmodeledGuards() {
		++numUnmodeledGuards;		
	}

	public void incEmptyPostConditions() {
		++numEmptyPostConditions;				
	}

	public void incUnmodeledPostConditions() {
		++numUnmodeledPostConditions;				
	}

	public void incTestGenerationErrors() {
		++numTestGenerationErrors;				
	}

	public void incGeneratedPositiveEvaluators() {
		++numPositiveEvaluators;
	}

	public void incGeneratedNegativeEvaluators() {
		++numNegativeEvaluators;
	}

	public void incTestCasesWithoutTargetMehtod() {
		++numTestCasesWithoutTargetMethod;		
	}
}