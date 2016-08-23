package org.toradocu.translator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.Toradocu;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.ThrowsTag;
import org.toradocu.util.OutputPrinter;

/**
 * ConditionTranslator translates exception comments in method documentation to
 * Java expressions.
 */
public class ConditionTranslator {
	
	private static final Logger LOG = LoggerFactory.getLogger(ConditionTranslator.class);
	
	/**
	 * This method translates the throws tags in the given methods.
	 * 
	 * @param methods a list of {@code DocumentedMethod}s whose throws tags to translate
	 */
	public static void translate(List<DocumentedMethod> methods) {
		for (DocumentedMethod method : methods) {
			for (ThrowsTag tag : method.throwsTags()) {
				StringBuilder logMessage = new StringBuilder("Identifying propositions from: ");
				logMessage.append("\"" + tag.getComment() + "\" in " + method.getSignature());
				LOG.trace(logMessage.toString());
				
				// Identify propositions in the comment. Each sentence in the comment is parsed into
				// a PropositionSeries.
				List<PropositionSeries> extractedPropositions
						= PropositionExtractor.getPropositionSeries(tag.getComment());

				Set<String> conditions = new LinkedHashSet<>();
				// Identify Java code elements in propositions.
				for (PropositionSeries propositions : extractedPropositions) {
					translatePropositions(propositions, method);
					conditions.add(propositions.getTranslation());
				}
				tag.setConditions(conditions);
			}
		}
		// Print translated throws tags.
		List<ThrowsTag> tags = methods.stream()
									  .map(m -> m.throwsTags())
									  .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
		OutputPrinter.Builder builder = new OutputPrinter.Builder("ConditionTranslator", tags);
		if (Toradocu.CONFIGURATION.getConditionTranslatorOutput() != null) {
			builder.file(Toradocu.CONFIGURATION.getConditionTranslatorOutput());
		}
		builder.logger(LOG);
		builder.build().print();
	}

	/**
	 * Translates the {@code Proposition}s in the given {@code propositionSeries} to Java expressions.
	 * 
	 * @param propositionSeries the {@code Proposition}s to translate into Java expressions
	 * @param method the method the containing the Javadoc comment from which the {@code propositionSeries} was
	 *        extracted
	 */
	private static void translatePropositions(PropositionSeries propositionSeries, DocumentedMethod method) {
		for (Proposition p : propositionSeries.getPropositions()) {
			Set<CodeElement<?>> subjectMatches;
			subjectMatches = Matcher.subjectMatch(p.getSubject(), method);
			if (subjectMatches.isEmpty()) {
				LOG.debug("Failed subject translation for: " + p);
				return;
			}
			
			// A single subject can match multiple elements (e.g., in "either value is null").
			// Therefore, predicate matching should be attempted for each matched subject code element.
			String translation = "";
			for (CodeElement<?> subjectMatch : subjectMatches) {
				String currentTranslation = Matcher.predicateMatch(subjectMatch, p.getPredicate(), p.isNegative());
				if (currentTranslation == null) {
					LOG.trace("Failed predicate translation for: " + p);
					continue;
				}
				
				if (translation.isEmpty()) {
					translation = currentTranslation;
				} else {
					translation += getConjunction(p.getSubject()) + currentTranslation;
				}
			}
			
			if (!translation.isEmpty()) {
				LOG.trace("Translated proposition " + p + " as: " + translation);
				p.setTranslation(translation);
			}
		}
	}

	/**
	 * Returns the conjunction that should be used to form the translation for a {@code Proposition}
	 * with the given subject. Returns null if no conjunction should be used.
	 * 
	 * @param subject the subject of the {@code Proposition}
	 * @return the conjunction that should be used to form the translation for the {@code Proposition}
	 *         with the given subject or null if no conjunction should be used
	 */
	private static String getConjunction(String subject) {
		if (subject.startsWith("either ") || subject.startsWith("any ")) {
			return "||";
		} else if (subject.startsWith("both") || subject.startsWith("all ")) {
			return "&&";
		} else {
			return null;
		}
	}

}
