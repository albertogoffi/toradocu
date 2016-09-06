package org.toradocu.translator;

import edu.stanford.nlp.semgraph.SemanticGraph;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.ThrowsTag;

/**
 * ConditionTranslator translates exception comments in method documentation to Java expressions.
 * The entry point is {@link #translate(List)}.
 */
public class ConditionTranslator {

  private static final Logger log = LoggerFactory.getLogger(ConditionTranslator.class);

  /**
   * Translates the throws tags in the given methods. This method sets the field
   * {@code ThrowsTag.condition} for each throws tags in the given methods.
   *
   * @param methods a list of {@code DocumentedMethod}s whose throws tags to translate
   */
  public static void translate(List<DocumentedMethod> methods) {
    for (DocumentedMethod method : methods) {
      for (ThrowsTag tag : method.throwsTags()) {
        log.trace(
            "Identifying propositions from: \""
                + tag.exceptionComment()
                + "\" in "
                + method.getSignature());

        String comment = tag.exceptionComment().trim();
        String lowerCaseComment = comment.toLowerCase();

        // Sanitize exception comment: remove initial "if"
        if (lowerCaseComment.startsWith("if ") && lowerCaseComment.length() > 3) {
          comment = comment.substring(3);
        }

        /* Identify propositions in the comment. Each sentence in the comment is parsed into a
         * PropositionSeries. */
        List<PropositionSeries> extractedPropositions = getPropositionSeries(comment);

        Set<String> conditions = new LinkedHashSet<>();
        // Identify Java code elements in propositions.
        for (PropositionSeries propositions : extractedPropositions) {
          translatePropositions(propositions, method);
          conditions.add(propositions.getTranslation());
        }
        tag.setCondition(mergeConditions(conditions));
      }
    }
  }

  /**
   * Takes a comment as a String and returns a list of {@code PropositionSeries} objects, one for
   * each sentence in the comment.
   *
   * @param comment the text of a Javadoc comment
   * @return a list of {@code PropositionSeries} objects, one for each sentence in the comment
   */
  private static List<PropositionSeries> getPropositionSeries(String comment) {
    List<PropositionSeries> result = new ArrayList<>();

    for (SemanticGraph semanticGraph : StanfordParser.getSemanticGraphs(comment)) {
      result.add(new SentenceParser(semanticGraph).getPropositionSeries());
    }

    return result;
  }

  /**
   * Translates the {@code Proposition}s in the given {@code propositionSeries} to Java expressions.
   *
   * @param propositionSeries the {@code Proposition}s to translate into Java expressions
   * @param method the method the containing the Javadoc comment from which the
   *        {@code propositionSeries} was extracted
   */
  private static void translatePropositions(
      PropositionSeries propositionSeries, DocumentedMethod method) {
    for (Proposition p : propositionSeries.getPropositions()) {
      Set<CodeElement<?>> subjectMatches;
      subjectMatches = Matcher.subjectMatch(p.getSubject(), method);
      if (subjectMatches.isEmpty()) {
        log.debug("Failed subject translation for: " + p);
        return;
      }

      // A single subject can match multiple elements (e.g., in "either value is null").
      // Therefore, predicate matching should be attempted for each matched subject code element.
      String translation = "";
      for (CodeElement<?> subjectMatch : subjectMatches) {
        String currentTranslation =
            Matcher.predicateMatch(subjectMatch, p.getPredicate(), p.isNegative());
        if (currentTranslation == null) {
          log.trace("Failed predicate translation for: " + p);
          continue;
        }

        if (translation.isEmpty()) {
          translation = currentTranslation;
        } else {
          translation += getConjunction(p.getSubject()) + currentTranslation;
        }
      }

      if (!translation.isEmpty()) {
        log.trace("Translated proposition " + p + " as: " + translation);
        p.setTranslation(translation);
      }
    }
  }

  /**
   * Returns a boolean Java expression that merges the conditions from the given set of conditions.
   * Each condition in the set is combined using an || conjunction.
   *
   * @param conditions the translated conditions for a throws tag (as Java boolean conditions)
   * @return a boolean Java expression that is true only if any of the given conditions is true
   */
  private static String mergeConditions(Set<String> conditions) {
    conditions.removeIf(s -> s.isEmpty());
    if (conditions.size() == 0) {
      return "";
    } else if (conditions.size() == 1) {
      return conditions.iterator().next();
    } else {
      Iterator<String> it = conditions.iterator();
      StringBuilder conditionsBuilder = new StringBuilder("(" + it.next() + ")");
      while (it.hasNext()) {
        conditionsBuilder.append(" || (" + it.next() + ")");
      }
      return conditionsBuilder.toString();
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
      return " || ";
    } else if (subject.startsWith("both ") || subject.startsWith("all ")) {
      return " && ";
    } else {
      return null;
    }
  }
}
