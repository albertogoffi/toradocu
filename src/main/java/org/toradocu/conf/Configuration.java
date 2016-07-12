package org.toradocu.conf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;

public enum Configuration {
	
	INSTANCE;
	
// General options

	@Parameter(names = "--targetClass", description = "Qualified name of the class to analyze with Toradocu", required = true)
	private String targetClass;
	
	@Parameter(names = "--debug", description = "Enable debug mode with fine-grained logging")
	private boolean debug;
	
	@Parameter(names = {"-h", "--help"}, description = "Print a list of available options", help = true)
	private boolean help;
	
// Condition translator options
	
	@Parameter(names = "--saveConditionTranslatorOutput", description = "File where to save the condition translator output", converter = FileConverter.class)
	private File conditionTranslatorOutput;
	
// Aspect creation options
	
	@Parameter(names = "--oracleGeneration", description = "Enable/disable the generation of the aspects", arity = 1)
	private boolean oracleGeneration = true;
	
	@Parameter(names = "--testClass", description = "Qualified name of the class that will be instrumented with aspects")
	private String testClass;
	
	@Parameter(names = "--outputDir", description = "Folder where Toradocu saves aspects")
	private String aspectsOutputDir = "aspects";
	
// Javadoc options
	
	@DynamicParameter(names = "-J", description = "Javadoc options")
	private Map<String, String> javadocOptions = new HashMap<>();
	
// Temporary Javadoc output directory
	private String tempJavadocOutputDir;
	
// Constants
	
	private final String aspectTemplate = "AspectTemplate.java";

// Getters
	
	public boolean isOracleGenerationEnabled() {
		return oracleGeneration;
	}
	
	public String getAspectsOutputDir() {
		return aspectsOutputDir;
	}
	
	public String getTestClass() {
		return testClass;
	}
	
	public String getAspectTemplate() {
		return aspectTemplate;
	}
	
	public String getTargetClass() {
		return targetClass;
	}
	
	/**
	 * Returns a temporary directory for Javadoc output or null if a
	 * non-temporary directory is set for Javadoc output.
	 * 
	 * @return a temporary directory for Javadoc output or null if a
	 * non-temporary directory is set for Javadoc output
	 */
	public String getTempJavadocOutputDir() {
		return tempJavadocOutputDir;
	}
	
	public File getConditionTranslatorOutput() {
		return conditionTranslatorOutput;
	}
	
	public boolean help() {
		return help;
	}
	
	public boolean debug() {
		return debug;
	}
	
	public String[] getJavadocOptions() {
		List<String> arguments = new ArrayList<>();
		for (Entry<String, String> javadocOption : javadocOptions.entrySet()) {
			arguments.add(javadocOption.getKey());
			if (!javadocOption.getValue().isEmpty()) {
				arguments.add(javadocOption.getValue());
			}
		}
		if (!arguments.contains("-quiet")) {
			arguments.add("-quiet");
		}
		if (!arguments.contains("-private")) {
			arguments.add("-private");
		}
		
		// Use a temporary Javadoc output directory if one is not specified
		if (!arguments.contains("-d")) {
			try {
				tempJavadocOutputDir = Files.createTempDirectory(null).toString();
				arguments.add("-d");
				arguments.add(tempJavadocOutputDir);
			} catch (IOException e) {
				// Output to working directory instead.
			}
		}
		
		arguments.add(getTargetPackage());
		return arguments.toArray(new String[0]);
	}
	
	private String getTargetPackage() {
		int packageStringEnd = targetClass.lastIndexOf(".");
		if (packageStringEnd == -1) {
			return "";
		}
		return targetClass.substring(0, packageStringEnd);
	}
}
