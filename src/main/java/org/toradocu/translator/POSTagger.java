package org.toradocu.translator;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;

public class POSTagger {

	public List<TaggedWord> tagWords(List<HasWord> sentence) {
		List<TaggedWord> taggedSentence = new ArrayList<>(sentence.size());
		for (HasWord word : sentence) {
			String wordString = word.toString();
			TaggedWord taggedWord = new TaggedWord(wordString);
//			if (wordString.equals("null")) {
//				taggedSentence.add(new TaggedWord(wordString, "NN"));
//			}
			// TODO: tag the words we know how to tag!
			taggedSentence.add(taggedWord);
		}
	
		return taggedSentence;
	}
}
