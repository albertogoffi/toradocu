package org.toradocu.translator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import edu.stanford.nlp.semgraph.SemanticGraph;

/**
 * PropositionExtractor takes a Javadoc comment as a {@code String} and uses NLP to convert it into propositions.
 */
public class PropositionExtractor {

	/**
	 * Takes a comment as a String and returns a list of {@code PropositionSeries} objects, one for each
	 * sentence in the comment.
	 * 
	 * @param comment the text of a Javadoc comment
	 * @return a list of {@code PropositionSeries} objects, one for each sentence in the comment
	 */
	public static List<PropositionSeries> getPropositionSeries(String comment) {
		comment = addPlaceholders(comment);
		
		List<PropositionSeries> result = new ArrayList<>();
		
		for (SemanticGraph semanticGraph : StanfordParser.getSemanticGraphs(comment)) {
			result.add(new SentenceParser(semanticGraph).getPropositionSeries());
		}
		
		return removePlaceholders(result);
	}
	
	/**
	 * Replaces inequalities (e.g. "< 3", ">= 42") with placeholder text that can be more easily
	 * parsed.
	 * 
	 * @param text the text containing inequalities
	 * @return text with inequalities replaced by placeholders
	 */
	private static String addPlaceholders(String text) {
		java.util.regex.Matcher matcher = Pattern.compile(INEQUALITY_NUMBER_REGEX).matcher(text);
		String placeholderText = text;
		int i = 0;
		while (matcher.find()) {
			inequalities.add(text.substring(matcher.start(), matcher.end()));
			placeholderText = placeholderText.replaceFirst(INEQUALITY_NUMBER_REGEX, PLACEHOLDER_PREFIX + i++);
		}
		return placeholderText;
	}
	
	private static final String INEQUALITY_NUMBER_REGEX = "(<|>)=? ?-?[0-9]+";
	private static final String PLACEHOLDER_PREFIX = "INEQUALITY_";
	private static List<String> inequalities = new ArrayList<>();
	
	/**
	 * Returns a new list of {@code PropositionSeries} in which any placeholder text has been replaced
	 * by the original inequalities.
	 * 
	 * @param seriesList the list of {@code PropositionSeries} containing placeholder text
	 * @return a new list of {@code PropositionSeries} with placeholders replaced by inequalities
	 */
	private static List<PropositionSeries> removePlaceholders(List<PropositionSeries> seriesList) {
		List<PropositionSeries> result = new ArrayList<>();
		
		for (PropositionSeries series : seriesList) {
			List<Proposition> inequalityPropositions = new ArrayList<>();
			for (Proposition placeholderProposition : series.getPropositions()) {
				String subject = placeholderProposition.getSubject();
				String predicate = placeholderProposition.getPredicate();
				for (int i = 0; i < inequalities.size(); i++) {
					subject = subject.replaceAll(PLACEHOLDER_PREFIX + i, inequalities.get(i));
					predicate = predicate.replaceAll(PLACEHOLDER_PREFIX + i, inequalities.get(i));
				}
				inequalityPropositions.add(new Proposition(subject, predicate, placeholderProposition.isNegative()));
			}
			result.add(new PropositionSeries(inequalityPropositions, series.getConjunctions()));
		}
		
		inequalities.clear();
		return result;
	}
	
}
