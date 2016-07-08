package org.toradocu.translator;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;

public class PropositionExtractor {

	public static List<PropositionList> getPropositions(String comment) {		
		List<PropositionList> propositionList = new ArrayList<>();
		
		// Preprocess text: where should we do this? Is it necessary?
		
		// For each sentence in comment
		for (List<HasWord> sentence : getSentences(comment)) {
			// Identify propositions
			PropositionList propositions = new SentenceParser(sentence).getPropositionList();
			propositionList.add(propositions);
		}
		return propositionList;
	}
	
	private static Iterable<List<HasWord>> getSentences(String text) {
		return new DocumentPreprocessor(new StringReader(text));
	}
}
