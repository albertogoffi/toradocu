package org.toradocu.translator;

import edu.stanford.nlp.semgraph.SemanticGraph;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
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

        // Add end-of-sentence period, if missing
        if (!comment.endsWith(".")) {
          comment += ".";
        }

        // Sanitize exception comment: remove initial "if"
        String lowerCaseComment = comment.toLowerCase();
        while (lowerCaseComment.startsWith("if ")) {
          comment = comment.substring(3);
          lowerCaseComment = comment.toLowerCase();
        }

        // Identify propositions in the comment. Each sentence in the comment is parsed into a
        // PropositionSeries.
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
    // Replace written out inequalities with symbols.
    text =
        text.replace("greater than or equal to", ">=")
            .replace("less than or equal to", "<=")
            .replace("greater than", ">")
            .replace("less than", "<")
            .replace("equal to", "==");

    java.util.regex.Matcher matcher = Pattern.compile(INEQUALITY_NUMBER_REGEX).matcher(text);

    java.util.regex.Matcher matcherInstanceOf = Pattern.compile(INEQ_INSOF).matcher(text);

    while (matcherInstanceOf.find()) {
      // Instance of added to the comparator list
      // Replace "[an] instance of" with "instanceof"
      text = text.replaceFirst(INEQ_INSOF, " instanceof");
    }

    java.util.regex.Matcher matcherIOfProcessed =
        Pattern.compile(INEQ_INSOFPROCESSED).matcher(text);
    String placeholderText = text;
    int i = 0;

    while (matcherIOfProcessed.find()) {
      //Specific case for the instance of placeholder. We put into inequalities the instanceof and the name
      // of the class
      inequalities.add(text.substring(matcherIOfProcessed.start(), matcherIOfProcessed.end()));
      placeholderText = placeholderText.replaceFirst(INEQ_INSOFPROCESSED, PLACEHOLDER_PREFIX + i++);
    }

    while (matcher.find()) {
      inequalities.add(text.substring(matcher.start(), matcher.end()));
      placeholderText =
          placeholderText.replaceFirst(INEQUALITY_NUMBER_REGEX, PLACEHOLDER_PREFIX + i);
      // Verbs that could appear before the inequality. One of these most be present
      // and will be added otherwise.
      String[] possibleVerbs = {"is", "is not", "isn't", "are", "are not", "aren't"};
      boolean containsVerb = false;
      for (String possibleVerb : possibleVerbs) {
        if (placeholderText.contains(possibleVerb + PLACEHOLDER_PREFIX + i)) {
          containsVerb = true;
          break;
        }
      }
      if (!containsVerb) {
        // The verb is assumed to be "is" and will be added to the text.
        placeholderText =
            placeholderText.replaceFirst(PLACEHOLDER_PREFIX + i, " is" + PLACEHOLDER_PREFIX + i);
      }
      i++;
    }

    return placeholderText;
  }

  private static final String INEQUALITY_NUMBER_REGEX = " *(([<>=]=?)|(!=)) ?-?[0-9]+";
  private static final String PLACEHOLDER_PREFIX = " INEQUALITY_";
  private static final String INEQ_INSOF = " *[an]* (instance of)"; // e.g "an instance of"
  private static final String INEQ_INSOFPROCESSED =
      " instanceof +[^ \\.]*"; // e.g. "instanceof BinaryMutation"

  /** Stores the inequalities that are replaced by placeholders when addPlaceholders is called. */
  private static List<String> inequalities = new ArrayList<>();

  /**
   * Returns a new list of {@code PropositionSeries} in which any placeholder text has been replaced
   * by the original inequalities. Original inequalities that were written out (e.g. "less than")
   * are replaced by their symbolic equivalent (e.g. "<").
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
        inequalityPropositions.add(
            new Proposition(subject, predicate, placeholderProposition.isNegative()));
      }
      result.add(new PropositionSeries(inequalityPropositions, series.getConjunctions()));
    }

    inequalities.clear();
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

      // Maps each subject code element to the Java expression translation that uses
      // that code element.
      Map<CodeElement<?>, String> translations = new LinkedHashMap<>();
      for (CodeElement<?> subjectMatch : subjectMatches) {
        String currentTranslation =
            Matcher.predicateMatch(subjectMatch, p.getPredicate(), p.isNegative());
        if (currentTranslation == null) {
          log.trace("Failed predicate translation for: " + p);
          continue;
        }
        translations.put(subjectMatch, currentTranslation);
      }

      if (translations.isEmpty()) {
        // Predicate match failed for every subject match.
        return;
      }

      // The final translation.
      String result = null;

      String conjunction = getConjunction(p.getSubject());
      if (conjunction != null) {
        // A single subject can refer to multiple elements (e.g., in "either value is null").
        // Therefore, translations for each subject code element should be merged using the
        // appropriate conjunction.
        for (String translation : translations.values()) {
          if (result == null) {
            result = translation;
          } else {
            result += conjunction + translation;
          }
        }
      } else {
        // Only one of the subject matches should be used.
        // Prefer parameters, followed by classes, methods and fields.
        CodeElement<?> preferredSubjectMatch = null;
        for (CodeElement<?> subjectMatch : translations.keySet()) {
          if (subjectMatch instanceof ParameterCodeElement) {
            preferredSubjectMatch = subjectMatch;
            break;
          } else if (subjectMatch instanceof ClassCodeElement) {
            preferredSubjectMatch = subjectMatch;
          } else if (subjectMatch instanceof MethodCodeElement
              && (preferredSubjectMatch == null
                  || preferredSubjectMatch instanceof FieldCodeElement)) {
            preferredSubjectMatch = subjectMatch;
          } else if (subjectMatch instanceof FieldCodeElement && preferredSubjectMatch == null) {
            preferredSubjectMatch = subjectMatch;
          }
        }
        result = translations.get(preferredSubjectMatch);
      }

      log.trace("Translated proposition " + p + " as: " + result);
      p.setTranslation(result);
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
