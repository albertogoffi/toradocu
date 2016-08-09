package org.toradocu;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.toradocu.conf.Configuration;
import org.toradocu.doclet.formats.html.ConfigurationImpl;
import org.toradocu.extractor.JavadocExtractor;
import org.toradocu.extractor.DocumentedMethod;
// import org.toradocu.translator.ConditionTranslator;
// import org.toradocu.translator.TranslatedExceptionComment;
import org.toradocu.util.GsonInstance;
import org.toradocu.util.NullOutputStream;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import com.google.gson.reflect.TypeToken;

import com.sun.javadoc.ClassDoc;
import com.sun.tools.javadoc.Main;

public class Toradocu {
	
	private static Logger LOG;
	private static final String DOCLET = "org.toradocu.doclet.standard.Standard";
	private static final String PROGRAM_NAME = "java -jar toradocu.jar";
	private static final Configuration CONF = Configuration.INSTANCE;
	private static final List<DocumentedMethod> methods = new ArrayList<>();
	
	/**
	 * Entry point for Toradocu. Takes several command-line arguments that
	 * configure its behavior.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		JCommander options;
		try {
			options = new JCommander(CONF, args);
		} catch (ParameterException e) {
			System.out.println(e.getMessage());
			return;
		}
		options.setProgramName(PROGRAM_NAME);
		
		if (CONF.help()) {
			options.usage();
			System.out.println("Options preceded by an asterisk are required.");
			return;
		}
		
		if (CONF.debug()) {
			System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
		}
		LOG = LoggerFactory.getLogger(Toradocu.class);
		
		// === Javadoc Extractor ===
		
		if (CONF.getConditionTranslatorInput() == null) {
			// List of methods to analyze must be retrieved with the Javadoc tool.
			// Suppress all the output of the Javadoc tool.
			PrintWriter nullPrintWriter = new PrintWriter(new NullOutputStream());
			// The execute method executes Javadoc with our doclet that in org.toradocu.doclet.formats.html.HtmlDoclet
			// invokes the method Toradocu.process defined below.  
			Main.execute(PROGRAM_NAME + " - Javadoc Extractor",
						 nullPrintWriter, nullPrintWriter,
						 nullPrintWriter, DOCLET, CONF.getJavadocOptions());
		} else {
			// List of methods to analyze are read from a file specified with a command line option.
			try (BufferedReader reader = Files.newBufferedReader(CONF.getConditionTranslatorInput().toPath())) {
				methods.addAll(GsonInstance.gson().fromJson(reader,
															new TypeToken<List<DocumentedMethod>>(){}.getType()));
			} catch (IOException e) {
				LOG.error("Unable to read the file: " + CONF.getConditionTranslatorInput(), e);
			}
		}
		
		// === Condition Translator ===
		
		// if (CONF.isConditionTranslationEnabled()) {
		//	  List<TranslatedExceptionComment> translatedComments = ConditionTranslator.translate(methods);
		//}
		// printOutput(translatedComments);
		
		// === Oracle Generator ===
		// OracleGenerator.generate(translatedComments);
		
		deleteTemporaryFiles();
	}
	
	/**
	 * This method populates the static field {@code methods} using {@code JavadocExtractor} when the given
	 * {@code classDoc} is the target class specified in {@code CONF}. This method is intended to be
	 * invoked by the Javadoc doclet.
	 * 
	 * @param classDoc the class from which methods are extracted, but only if it is the target class specified
	 *        in {@code CONF}
	 * @param configuration configuration options for the Javadoc doclet
	 */
	public static void process(ClassDoc classDoc, ConfigurationImpl configuration) throws IOException {
		if (!classDoc.qualifiedName().equals(CONF.getTargetClass())) return;
		JavadocExtractor extractor = new JavadocExtractor(configuration);
		methods.addAll(extractor.extract(classDoc)); 
	}
	
	/**
	 * Deletes any temporary files created by Toradocu to store Javadoc output. 
	 */
	private static void deleteTemporaryFiles() {
		if (CONF.getTempJavadocOutputDir() != null) {
			try {
				FileUtils.deleteDirectory(new File(CONF.getTempJavadocOutputDir()));
			} catch (IOException e) {
				LOG.warn("Unable to delete temporary Javadoc output", e);
			}
		}
	}
	
//	private static void printOutput(Collection<?> c) {
//		List<?> sortedC = new ArrayList<>(c);
//		Collections.sort(sortedC, (c1, c2) -> c1.toString().compareTo(c2.toString()));
//		File outputFile = CONF.getConditionTranslatorOutput();
//		if (outputFile != null) { // If the command line option to print the condition translator's output is present
//			try (BufferedWriter writer = Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8)) {
//				for (Object element : sortedC) {
//					writer.write(element.toString());
//					writer.newLine();
//				}
//			} catch (Exception e) {
//				LOG.warn("Unable to write the output of the condition translator", e);
//			}
//		} else { // Else, print the condition translator's output on the standard output
//			StringBuilder output = new StringBuilder();
//			for (Object element : sortedC) {
//				output.append(element).append("\n");
//			}
//			LOG.info(output.toString());
//		}
//	}
}
