package org.toradocu;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.toradocu.conf.Configuration;
import org.toradocu.doclet.formats.html.ConfigurationImpl;
import org.toradocu.util.NullOutputStream;

import com.beust.jcommander.JCommander;
import com.sun.javadoc.ClassDoc;
import com.sun.tools.javadoc.Main;

public class Toradocu {
	private static final Logger LOG = Logger.getLogger(Toradocu.class.getName());
	private static final String DOCLET = "org.toradocu.doclet.standard.Standard";
	private static final String PROGRAM_NAME = "java -jar toradocu.jar";
	private static final Configuration CONF = Configuration.getInstance();
	
	public static void main(String[] args) {
		JCommander options = new JCommander(CONF, args);
		options.setProgramName(PROGRAM_NAME);
		
		if (CONF.help()) {
			options.usage();
			System.out.println("Options preceded by an asterisk are required.");
			return;
		}
		
		if (CONF.debug()) {
			Logger.getLogger("").setLevel(Level.ALL);
			for (Handler handler: Logger.getLogger("").getHandlers()) {
				if (handler instanceof ConsoleHandler) {
					handler.setLevel(Level.ALL);
				}
			}
		}
		
		/* The execute method executes Javadoc with our doclet that in
		 * org.toradocu.doclet.formats.html.HtmlDoclet invokes the method
		 * Toradocu.process defined below.
		 */  
		PrintWriter nullPrintWriter = new PrintWriter(new NullOutputStream());
		Main.execute(PROGRAM_NAME + " - Javadoc Extractor", nullPrintWriter, nullPrintWriter, nullPrintWriter, DOCLET, CONF.getJavadocOptions());
	}
	
	public static void process(ClassDoc classDoc, ConfigurationImpl configuration) throws IOException {
		if (classDoc.qualifiedName().equals(CONF.getTargetClass())) {
			Set<JavadocExceptionComment> extractedComments = JavadocExtractor.extract(classDoc, configuration);
			List<TranslatedExceptionComment> translatedComments = ConditionTranslator.translate(extractedComments);
			printOutput(translatedComments);
			OracleGenerator.generate(translatedComments);
		}
	}
	
	private static void printOutput(Collection<?> c) {
		List<?> sortedC = new ArrayList<>(c);
		Collections.sort(sortedC, (c1, c2) -> c1.toString().compareTo(c2.toString()));
		File outputFile = CONF.getConditionTranslatorOutput();
		if (outputFile != null) { // If the command line option to print the condition translator's output is present
			try (BufferedWriter writer = Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8)) {
				for (Object element : sortedC) {
					writer.write(element.toString());
					writer.newLine();
				}
			} catch (Exception e) {
				LOG.log(Level.WARNING, "Unable to write the output of the condition translator", e);
			}
		} else { // Else, print the condition translator's output on the standard output
			StringBuilder output = new StringBuilder();
			for (Object o : sortedC) {
				output.append(o).append("\n");
			}
			LOG.info(output.toString());
		}
	}
	
}
