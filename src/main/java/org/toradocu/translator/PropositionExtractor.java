package org.toradocu.translator;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;

/**
 * PropositionExtractor takes a Javadoc comment as a {@code String} and uses NLP to convert it into propositions.
 */
public class PropositionExtractor {

	/**
	 * Takes a comment as a String and returns a list of {@code PropositionSeries} objects, one for each
	 * sentence in the comment.
	 * 
	 * @param comment the text of a Javadoc comment
	 * @return a list of {@code PropositionSeries} objects, one for each
	 * sentence in the comment
	 */
	public static List<PropositionSeries> getPropositions(String comment) {		
		List<PropositionSeries> result = new ArrayList<>();
		
		for (List<HasWord> sentence : getSentences(comment)) {
			PropositionSeries propositionsInSentence = new SentenceParser(sentence).getPropositionSeries();
			result.add(propositionsInSentence);
		}
		return result;
	}
	
	/**
	 * Takes a {@code String} of text and returns an {@code Iterable} over lists of {@code HasWord}
	 * elements where each list is a sentence in the given text.
	 * 
	 * @param text a {@code String} of text to split into sentences
	 * @return an {@code Iterable} over a list of {@code HasWord} elements where each list is a sentence
	 *         from the given text
	 */
	private static Iterable<List<HasWord>> getSentences(String text) {
		return new DocumentPreprocessor(new StringReader(text));
	}
}
