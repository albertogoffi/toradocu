package org.toradocu.translator;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.semgraph.SemanticGraph;

/**
 * PropositionExtractor takes a Javadoc comment as a {@code String} and uses NLP to convert it into
 * propositions.
 */
public class PropositionExtractor {

  /**
   * Takes a comment as a String and returns a list of {@code PropositionSeries} objects, one for
   * each sentence in the comment.
   *
   * @param comment the text of a Javadoc comment
   * @return a list of {@code PropositionSeries} objects, one for each sentence in the comment
   */
  public static List<PropositionSeries> getPropositionSeries(String comment) {
    List<PropositionSeries> result = new ArrayList<>();

    for (SemanticGraph semanticGraph : StanfordParser.getSemanticGraphs(comment)) {
      result.add(new SentenceParser(semanticGraph).getPropositionSeries());
    }

    return result;
  }
}
