package org.toradocu.translator.semantic;

/**
 * Created by arianna on 29/05/17.
 *
 * <p>Utils for the selection of specific POS tags (for comment parsing).
 */
public class POSUtils {

  //    public static String findSubjectPredicate(String comment, DocumentedMethod method) {
  //        String result = "";
  //        for (SemanticGraph sg : StanfordParser.getSemanticGraphs(comment, method)) {
  //            //Search nouns, their adjectives and adverbs.
  //
  //            List<IndexedWord> nouns = sg.getAllNodesByPartOfSpeechPattern("NN(.*)");
  //            List<IndexedWord> adjs = sg.getAllNodesByPartOfSpeechPattern("JJ(.*)");
  //            List<IndexedWord> advs = sg.getAllNodesByPartOfSpeechPattern("RB(.*)");
  //            for (IndexedWord noun : nouns) result += noun.word() + " ";
  //            for (IndexedWord adj : adjs) result += adj.word() + " ";
  //            for (IndexedWord adv : advs) result += adv.word() + " ";
  //
  //            //Search for a verb.
  //            List<IndexedWord> verbs = sg.getAllNodesByPartOfSpeechPattern("VB(.*)");
  //            for (IndexedWord v : verbs) result += v.word() + " ";
  //        }
  //        return result.replaceAll("\\s+", " ").trim();
  //    }
}
