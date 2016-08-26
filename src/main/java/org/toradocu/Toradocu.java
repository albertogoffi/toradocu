package org.toradocu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.conf.Configuration;
import org.toradocu.doclet.formats.html.ConfigurationImpl;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.JavadocExtractor;
import org.toradocu.translator.ConditionTranslator;
import org.toradocu.util.ExportedData;
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
	public static final Configuration CONFIGURATION = Configuration.INSTANCE;
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
			options = new JCommander(CONFIGURATION, args);
		} catch (ParameterException e) {
			System.out.println(e.getMessage());
			return;
		}
		options.setProgramName(PROGRAM_NAME);
		CONFIGURATION.initialize();
		
		if (CONFIGURATION.help()) {
			options.usage();
			System.out.println("Options preceded by an asterisk are required.");
			return;
		}
		
		if (CONFIGURATION.debug()) {
			System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "trace");
		}
		/* Suppress non-error messages from Stanford parser (some of the messages directly printed on standard error are still visible). */
		System.setProperty(org.slf4j.impl.SimpleLogger.LOG_KEY_PREFIX + "edu.stanford", "error");
		LOG = LoggerFactory.getLogger(Toradocu.class);
		
		// === Javadoc Extractor ===
		
		if (CONFIGURATION.getConditionTranslatorInput() == null) {
			// List of methods to analyze must be retrieved with the Javadoc tool.
			// Suppress all the output of the Javadoc tool.
			PrintWriter nullPrintWriter = new PrintWriter(new NullOutputStream());
			// The execute method executes Javadoc with our doclet that in org.toradocu.doclet.formats.html.HtmlDoclet
			// invokes the method Toradocu.process defined below.  
			Main.execute(PROGRAM_NAME + " - Javadoc Extractor",
						 nullPrintWriter, nullPrintWriter,
						 nullPrintWriter, DOCLET, CONFIGURATION.getJavadocOptions());
		} else {
			// List of methods to analyze are read from a file specified with a command line option.
			try (BufferedReader reader = Files.newBufferedReader(CONFIGURATION.getConditionTranslatorInput().toPath())) {
				methods.addAll(GsonInstance.gson().fromJson(reader,
															new TypeToken<List<DocumentedMethod>>(){}.getType()));
			} catch (IOException e) {
				LOG.error("Unable to read the file: " + CONFIGURATION.getConditionTranslatorInput(), e);
			}
		}
	    
	    if (CONFIGURATION.getJavadocExtractorOutput() != null) { // Print collection to the output file.
            try (BufferedWriter writer = Files.newBufferedWriter(CONFIGURATION.getJavadocExtractorOutput().toPath(), StandardCharsets.UTF_8)) {
                writer.write(GsonInstance.gson().toJson(methods));
            } catch (Exception e) {
                LOG.error("Unable to write the output on file " + CONFIGURATION.getJavadocExtractorOutput().getAbsolutePath(), e);
            }
        }
        if (CONFIGURATION.debug()) {
            LOG.debug("Methods with Javadoc documentation found in source code: " + methods.toString());
        }
		
		// === Condition Translator ===
		
		if (CONFIGURATION.isConditionTranslationEnabled()) {
			ConditionTranslator.translate(methods);
			if (CONFIGURATION.getConditionTranslatorOutput() != null) {
				ExportedData data = new ExportedData(methods);
				try (BufferedWriter writer = Files.newBufferedWriter(CONFIGURATION.getConditionTranslatorOutput().toPath(),
																	 StandardCharsets.UTF_8)) {
					writer.write(data.asJson());
				} catch (Exception e) {
					LOG.error("Unable to write the output on file " + CONFIGURATION.getConditionTranslatorOutput().getAbsolutePath(), e);
				}
			}
		}
		
		// === Oracle Generator ===
		// OracleGenerator.generate(methods);
		
		deleteTemporaryFiles();
		/* Needed for testing: multiple executions of Toradocu run in the same JVM. This can be improved making {@code methods}
         * non-static and changing the way Toradocu interacts with the javadoc tool. */
		methods.clear();
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
		if (!classDoc.qualifiedName().equals(CONFIGURATION.getTargetClass())) return;
		JavadocExtractor extractor = new JavadocExtractor(configuration);
		methods.addAll(extractor.extract(classDoc)); 
	}
	
	/**
	 * Deletes any temporary files created by Toradocu to store Javadoc output. 
	 */
	private static void deleteTemporaryFiles() {
		if (CONFIGURATION.getTempJavadocOutputDir() != null) {
			try {
				FileUtils.deleteDirectory(new File(CONFIGURATION.getTempJavadocOutputDir()));
			} catch (IOException e) {
				LOG.warn("Unable to delete temporary Javadoc output", e);
			}
		}
	}
}
