package org.toradocu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.toradocu.aspect.AspectGenerator;
import org.toradocu.conf.Configuration;

import com.sun.javadoc.ExecutableMemberDoc;

public class OracleGenerator {
	
	private static final Logger LOG = Logger.getLogger(OracleGenerator.class.getName());
	private static final Configuration CONF = Configuration.getInstance();
	
	public static void generate(List<TranslatedExceptionComment> translatedComments) {
		if (!CONF.isOracleGenerationEnabled()) {
			LOG.log(Level.INFO, "Oracle generator disabled: skipped aspect generation.");
			return;
		}
		
		if (!translatedComments.isEmpty()) {	
			createIfNotExists(CONF.getAspectsOutputDir());
			
			AspectGenerator aspectGenerator = new AspectGenerator(CONF.getAspectTemplate());
			String testClassName = CONF.getTestClass();
			Map<ExecutableMemberDoc, Set<TranslatedExceptionComment>> translatedCommentMap = getTranslatedCommentMap(translatedComments);
			
			List<String> createdAspectNames = new ArrayList<>();
			int aspectNumber = 0;
			for (ExecutableMemberDoc classMember : translatedCommentMap.keySet()) {
				aspectNumber++;
				String aspectName = "Aspect_" + aspectNumber;
				String aspectPath = Configuration.getInstance().getAspectsOutputDir() + File.separator + aspectName;
				aspectGenerator.create(testClassName, classMember, isParsingComplete(translatedCommentMap.get(classMember)), translatedCommentMap.get(classMember), aspectPath);
				createdAspectNames.add(aspectName);
			}
			createAOPXml(Configuration.getInstance().getAspectsOutputDir(), createdAspectNames);
		}
	}
	
	/*
	 * Toradocu oracles signal a failure if the method under test throws an unexpected exception.
	 * We deem as unexpected the exceptions that are not documented. If Toradocu fails to parse a comment,
	 * Toradocu will avoid to signal any unexpected exception.
	 * TODO: Refine this. We can do much better, for example we can distinguish between no comment and
	 * parsing failed. The first case is much more severe than the second.
	 */
	private static boolean isParsingComplete(Set<TranslatedExceptionComment> translatedComments) {
		for (TranslatedExceptionComment translatedComment : translatedComments) {
			if (translatedComment.getConditions().isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private static void createAOPXml(String folder, List<String> createdAspects) {
		final String HEADER = "<aspectj>\n\t<weaver options=\"-verbose -showWeaveInfo\"/>\n\t<aspects>\n";
		final String FOOTER = "\t</aspects>\n</aspectj>";
		StringBuilder content = new StringBuilder(HEADER);
		for (String aspect : createdAspects) {
			content.append("\t\t<aspect name=\"" + aspect + "\"/>\n");
		}
		content.append(FOOTER);
		try (FileOutputStream output = new FileOutputStream(new File(folder + File.separator + "aop.xml"))) {
			output.write(content.toString().getBytes());
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Error in creating aop.xml file.", e);
		}
	}
	
	private static void createIfNotExists(String aspectsOutputDir) {
		File outputDir = new File(aspectsOutputDir);
		if (!outputDir.exists()) {
			outputDir.mkdir();
		}
	}

	// We generate one aspect per class member (ExecutableMemberDoc). Thus, we aggregate all the translated comments of a member in a map.
	private static Map<ExecutableMemberDoc, Set<TranslatedExceptionComment>> getTranslatedCommentMap(List<TranslatedExceptionComment> translatedComments) {
		Map<ExecutableMemberDoc, Set<TranslatedExceptionComment>> conditionsMap = new HashMap<>();
		for (TranslatedExceptionComment translatedComment : translatedComments) {
			if (translatedComment.getConditions().isEmpty()) {
				continue; // Fix to avoid to generate "empty" aspects (aspects that do not check anything)
			}
			
			Set<TranslatedExceptionComment> tc = conditionsMap.get(translatedComment.getMember());
			if (tc == null) {
				Set<TranslatedExceptionComment> newSet = new HashSet<>();
				newSet.add(translatedComment);
				conditionsMap.put(translatedComment.getMember(), newSet);
			} else {
				tc.add(translatedComment);
			}
		}
		return conditionsMap;
	}
}
