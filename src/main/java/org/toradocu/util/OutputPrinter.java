package org.toradocu.util;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OutputPrinter {
	
	private final Logger LOG;
	private final String component; // Component that wants to print
	private final List<?> collection; // Collection to print
	private final Logger componentLogger; // Logger of the component where to print the output (if present)
	private final File outputFile; // File where to print the output (if present)
	
	public void print() {
		System.out.println("[" + component + "]");
		collection.stream().forEach(System.out::println);
		
		if (outputFile != null) { // Print collection on the output file
			try (BufferedWriter writer = Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8)) {
				writer.write(GsonInst.gson().toJson(collection));
			} catch (Exception e) {
				LOG.error("Unable to write the output on file " + outputFile.getAbsolutePath(), e);
			}
		}
		if (componentLogger != null) { // Print collection on the logger			
			StringBuilder output = new StringBuilder("[" + component + "]");
			for (Object o : collection) {
				output.append("\n").append(o);
			}
			componentLogger.info(output.toString());
		}
	}
	
	public static class Builder implements org.toradocu.util.Builder<OutputPrinter> {

		// Required parameters
		private final String component; // Component that wants to print
		private final List<?> collection; // Collection to print
		
		// Optional parameters (one out of two must be present)
		private Logger componentLogger; // Logger of the component where to print the output (if present)
		private File outputFile; // File where to print the output (if present)
		
		public Builder(String component, List<?> collection) {
			Objects.requireNonNull(component, "component must not be null");
			Objects.requireNonNull(collection, "collection must not be null");
			this.component = component;
			this.collection = collection;
		}
		
		public Builder logger(Logger logger) {
			this.componentLogger = logger;
			return this;
		}
		
		public Builder file(File outputFile) {
			this.outputFile = outputFile;
			return this;
		}
		
		@Override
		public OutputPrinter build() {
			return new OutputPrinter(this);
		}
	}
	
	private OutputPrinter(Builder builder) {
		LOG = LoggerFactory.getLogger(OutputPrinter.class);
		component = builder.component;
		collection = builder.collection;
		outputFile = builder.outputFile;
		componentLogger = builder.componentLogger;
	}
}
