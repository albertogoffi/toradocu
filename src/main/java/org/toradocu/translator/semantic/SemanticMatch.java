package org.toradocu.translator.semantic;

import java.util.LinkedHashMap;
import java.util.Map;
import org.toradocu.translator.CodeElement;

/**
 * Created by arianna on 29/05/17.
 *
 * <p>A SemanticMatch bounds a Tag with to its valid semantic matches, i.e. method code elements
 * that have a semantic distance below the predefined threshold. We store further information in
 * order to have a clearer and more complete output.
 */
public class SemanticMatch implements Comparable<SemanticMatch> {

  /** The method the Tag belongs to. */
  String method;

  /** The tag for which we want to produce a condition translation. */
  //    Tag<?> tag;
  String comment;
  /** The distance threshold used for this match. */
  float threshold;

  /** The result of comment parsing: stopwords removal etc. */
  String parsedComment;

  /**
   * Method code element that have a semantic distance from the comment which is below the
   * threshold,i.e. candidates for the correct translation.
   */
  LinkedHashMap<CodeElement<?>, Double> candidates;

  public SemanticMatch(String comment, String method, String parsedComment, float threshold) {
    this.comment = comment;
    this.method = method;
    this.parsedComment = parsedComment;
    this.threshold = threshold;
  }

  public String getMethod() {
    return method;
  }

  public void setCandidates(LinkedHashMap<CodeElement<?>, Double> orderedDistances) {
    int i = 0;
    this.candidates = new LinkedHashMap<CodeElement<?>, Double>();
    for (Map.Entry<CodeElement<?>, Double> entry : orderedDistances.entrySet()) {
      this.candidates.put(entry.getKey(), entry.getValue());
      i++;
      if (i == 5) return;
    }
  }

  public LinkedHashMap<CodeElement<?>, Double> getCandidates() {
    return candidates;
  }

  @Override
  public int compareTo(SemanticMatch semanticMatch) {
    return this.method.compareTo(semanticMatch.method);
  }
}
