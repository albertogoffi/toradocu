package org.toradocu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.toradocu.conf.Configuration;
import org.toradocu.doclet.formats.html.ConfigurationImpl;
import org.toradocu.extractor.JavadocExceptionComment;
import org.toradocu.extractor.JavadocExtractor;
import org.toradocu.extractor.Method;
import org.toradocu.util.GsonInst;
import org.toradocu.util.NullOutputStream;

import com.beust.jcommander.JCommander;
import com.google.gson.reflect.TypeToken;
import com.sun.javadoc.ClassDoc;
import com.sun.tools.javadoc.Main;

public class Toradocu {
	private static final Logger LOG = LoggerFactory.getLogger(Toradocu.class);
	private static final String DOCLET = "org.toradocu.doclet.standard.Standard";
	private static final String PROGRAM_NAME = "java -jar toradocu.jar";
	private static final Configuration CONF = Configuration.INSTANCE;
	private static final List<Method> methods = new ArrayList<>();
	
	public static void main(String[] args) {
		JCommander options = new JCommander(CONF, args);
		options.setProgramName(PROGRAM_NAME);
		
		if (CONF.help()) {
			options.usage();
			System.out.println("Options preceded by an asterisk are required.");
			return;
		}
		
		if (CONF.debug()) {
			System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
		}
		
		if (CONF.getConditionTranslatorInput() == null) { // List of methods to analyze must be retrieved with the Javadoc tool
			/* The execute method executes Javadoc with our doclet that in
			 * org.toradocu.doclet.formats.html.HtmlDoclet invokes the method
			 * Toradocu.process defined below.
			 */  
			PrintWriter nullPrintWriter = new PrintWriter(new NullOutputStream()); // suppress all the output of the Javadoc tool
			Main.execute(PROGRAM_NAME + " - Javadoc Extractor", nullPrintWriter, nullPrintWriter, nullPrintWriter, DOCLET, CONF.getJavadocOptions());
		} else { // List of methods to analyze are read from a file specified with a command line option
			try (BufferedReader reader = Files.newBufferedReader(CONF.getConditionTranslatorInput().toPath())) {
				methods.addAll(GsonInst.gson().fromJson(reader, new TypeToken<List<Method>>(){}.getType()));
			} catch (IOException e) {
				LOG.error("Unable to read the file: " + CONF.getConditionTranslatorInput(), e);
			}
		}
		
//		List<TranslatedExceptionComment> translatedComments = ConditionTranslator.translate(extractedComments);
//		printOutput(translatedComments);
//		OracleGenerator.generate(translatedComments);
	}
	
	/**
	 * This method populate static field <code>methods</code>.
	 * This method is intended to be invoked by the Javadoc tool.
	 */
	public static void process(ClassDoc classDoc, ConfigurationImpl configuration) throws IOException {
		if (!classDoc.qualifiedName().equals(CONF.getTargetClass())) return;
		
		JavadocExtractor extractor = new JavadocExtractor(configuration);
		methods.addAll(extractor.extract(classDoc));
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
//				LOG.log(Level.WARNING, "Unable to write the output of the condition translator", e);
//			}
//		} else { // Else, print the condition translator's output on the standard output
//			StringBuilder output = new StringBuilder();
//			for (Object o : sortedC) {
//				output.append(o).append("\n");
//			}
//			LOG.info(output.toString());
//		}
//	}
//	
}
