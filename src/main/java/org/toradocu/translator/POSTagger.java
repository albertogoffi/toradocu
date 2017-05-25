package org.toradocu.translator;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import java.util.ArrayList;
import java.util.List;

public class POSTagger {

  /**
   * This is a custom POS tagger to be used by the Stanford Parser. The purpose is tagging words in
   * the comment in case we already know their right POS tag. Up to now, this is useful for
   * inequalities placeholder (tagged ad JJ) and code elements (tagged as NN). This way, the
   * Stanford Parser will take in input a partially tagged sentence, and will adapt its tagging to
   * our previous one.
   *
   * @param sentence the original sentence to tag, i.e. the comment
   * @param codeElements list of the words in the sentence that are code elements
   * @return the partially tagged sentence
   */
  public static List<TaggedWord> tagWords(List<HasWord> sentence, List<String> codeElements) {
    List<TaggedWord> taggedSentence = new ArrayList<>(sentence.size());
    for (HasWord word : sentence) {
      String wordString = word.toString();
      TaggedWord taggedWord = new TaggedWord(wordString);
      if (wordString.contains("INEQUALITY") && taggedWord.tag() == null)
        taggedSentence.add(new TaggedWord(wordString, "JJ"));
      else if (codeElements.contains(wordString) && taggedWord.tag() == null)
        taggedSentence.add(new TaggedWord(wordString, "NN"));
      else taggedSentence.add(taggedWord);
    }

    return taggedSentence;
  }
}
