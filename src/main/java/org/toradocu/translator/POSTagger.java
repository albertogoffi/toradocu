package org.toradocu.translator;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.toradocu.conf.Configuration;
import org.toradocu.extractor.Comment;

public class POSTagger {

  /**
   * This is a custom POS tagger to be used by the Stanford Parser. The purpose is tagging words in
   * the comment in case we already know their right POS tag. Up to now, this is useful for
   * inequalities placeholder (tagged as JJ) and code elements (tagged as NN). This way, the
   * Stanford Parser will take in input a partially tagged sentence, and will adapt its tagging to
   * our previous one.
   *
   * @param comment original {@code Comment}
   * @param placeholderSentence String holding the sentence to tag, with placeholders
   * @param inequalities List of inequalities corresponding to placeholders
   * @param sentence original sentence to tag, i.e. the comment
   * @param parameters method's parameters names
   * @return the partially tagged sentence
   */
  static List<TaggedWord> tagWords(
      Comment comment,
      String placeholderSentence,
      List<String> inequalities,
      List<HasWord> sentence,
      List<String> parameters) {
    int inequalityIndex = 0;
    Map<String, List<Integer>> codeWordsInComment = comment.getWordsMarkedAsCode();
    Map<String, Integer> codeWordsSeen = new HashMap<>();
    for (String key : codeWordsInComment.keySet()) {
      codeWordsSeen.put(key, 0);
    }

    List<TaggedWord> taggedSentence = new ArrayList<>(sentence.size());
    for (HasWord word : sentence) {
      String wordString = word.toString();
      TaggedWord taggedWord = new TaggedWord(wordString);
      if (wordString.contains("INEQUALITY") && taggedWord.tag() == null) {
        String[] inequalityToken = inequalities.get(inequalityIndex).split(" ");
        for (String inequalityWord : inequalityToken) {
          if (codeWordsInComment.get(inequalityWord) != null) {
            // Behind this placeholder there is a codeword: update counter
            // of occurrences already seen
            inequalityWord = inequalityWord;
            int codeWorCount = codeWordsSeen.get(inequalityWord);
            List<Integer> occurrenceInComment = codeWordsInComment.get(inequalityWord);
            for (int occurrence : occurrenceInComment) {
              if (codeWordsSeen.get(inequalityWord) == occurrence) {
                codeWordsSeen.put(inequalityWord, ++codeWorCount);
                break;
              }
            }
          }
        }
        inequalityIndex++;
        taggedSentence.add(new TaggedWord(wordString, "JJ"));
      } else if (wordString.equalsIgnoreCase(Configuration.RETURN_VALUE)
          || wordString.equalsIgnoreCase(Configuration.RECEIVER)) {
        // Our identifiers must be considered nouns
        taggedSentence.add(new TaggedWord(wordString, "NN"));
      } else if (wordString.equals("null")
          || wordString.equals("nonnull")
          || wordString.equals("non-null") && taggedWord.tag() == null) {
        taggedSentence.add(new TaggedWord(wordString, "JJ"));
      } else if (codeWordsInComment.get(wordString) != null) {
        // this wordString is present in the map of word tagged as code.
        // Is it the right one? Check its occurrence.
        List<Integer> occurrenceInComment = codeWordsInComment.get(wordString);
        int codeWorCount = codeWordsSeen.get(wordString);
        for (int occurrence : occurrenceInComment) {
          if (codeWordsSeen.get(wordString) == occurrence) {
            // This occurrence of the word is a codeword: tag it as NN
            taggedSentence.add(new TaggedWord(wordString, "NN"));
          } else {
            // This is not the right occurrence: update counter but add no tag
            codeWordsSeen.put(wordString, ++codeWorCount);
            taggedSentence.add(taggedWord);
          }
        }
      } else if (parameters != null && parameters.contains(wordString)) {
        // Last attempt: no code tags found. Make an assumption over parameters names.
        taggedSentence.add(new TaggedWord(wordString, "NN"));
      } else {
        taggedSentence.add(taggedWord);
      }
    }
    return taggedSentence;
  }
}
