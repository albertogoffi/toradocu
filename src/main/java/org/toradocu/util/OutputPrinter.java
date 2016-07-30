package org.toradocu.util;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to print a collection of objects from some Toradocu component.
 */
public final class OutputPrinter {
	
	/** {@code Logger} for this class. */
	private final Logger LOG;
	/** Component that wants to print. */
	private final String component;
	/** Collection to print. */
	private final List<?> collection;
	/** {@code Logger} of the component where to print the output (if present). */
	private final Logger componentLogger;
	/** {@code File} where to print the output (if present). */ 
	private final File outputFile;
	
	/**
	 * Prints the collection to the logger and output file specified for this printer.
	 */
	public void print() {
		if (outputFile != null) { // Print collection to the output file.
			try (BufferedWriter writer = Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8)) {
				writer.write(GsonInstance.gson().toJson(collection));
			} catch (Exception e) {
				LOG.error("Unable to write the output on file " + outputFile.getAbsolutePath(), e);
			}
		}
		if (componentLogger != null) { // Print collection on the logger.			
			StringBuilder output = new StringBuilder("[" + component + "]");
			for (Object o : collection) {
				output.append("\n").append(o);
			}
			componentLogger.info(output.toString());
		}
	}
	
	/**
	 * Builds an {@code OutputPrinter} using the provided information.
	 */
	public static class Builder implements org.apache.commons.lang3.builder.Builder<OutputPrinter> {

		// Required parameters.
		/** Component that wants to print. */
		private final String component;
		/** Collection to print. */
		private final List<?> collection;
		
		// Optional parameters (one out of two must be present).
		/** {@code Logger} of the component where to print the output (if present). */
		private Logger componentLogger;
		/** {@code File} where to print the output (if present). */
		private File outputFile;
		
		/**
		 * Constructs a builder for an {@code OutputPrinter} with the given component name and collection
		 * of objects to print.
		 * 
		 * @param component the name of the component whose output to print
		 * @param collection the collection of objects to print
		 */
		public Builder(String component, List<?> collection) {
			Objects.requireNonNull(component, "component must not be null");
			Objects.requireNonNull(collection, "collection must not be null");
			this.component = component;
			this.collection = collection;
		}
		
		/**
		 * Adds the specified logger as an output location for the {@code OutputPrinter}.
		 * 
		 * @param logger the logger to send output to
		 * @return this {@code Builder}
		 */
		public Builder logger(Logger logger) {
			this.componentLogger = logger;
			return this;
		}
		
		/**
		 * Adds the specified file as an output location for the {@code OutputPrinter}.
		 * 
		 * @param outputFile the file to send output to
		 * @return this {@code Builder}
		 */
		public Builder file(File outputFile) {
			this.outputFile = outputFile;
			return this;
		}
		
		/**
		 * Builds and returns an {@code OutputPrinter} with the information given to this {@code Builder}.
		 * 
		 * @return a {@code OutputPrinter} containing the information passed to this builder
		 */
		@Override
		public OutputPrinter build() {
			return new OutputPrinter(this);
		}
	}
	
	/**
	 * Constructs an {@code OutputPrinter} using the information in the provided {@code Builder}.
	 * 
	 * @param builder the {@code Builder} containing information about this {@code OutputPrinter}
	 */
	private OutputPrinter(Builder builder) {
		LOG = LoggerFactory.getLogger(OutputPrinter.class);
		component = builder.component;
		collection = builder.collection;
		componentLogger = builder.componentLogger;
		outputFile = builder.outputFile;
	}
}
