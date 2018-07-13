package org.toradocu.translator;

import static java.util.stream.Collectors.toList;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.toradocu.extractor.Comment;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.DocumentedParameter;

/**
 * Created by arianna on 18/05/17.
 *
 * <p>The Parser must extract the {@code PropositionSerie}s from a {@code BlockTag} comment. Thus,
 * it interacts with the Stanford Parser in order to produce the {@code SemanticGraph}. It holds a
 * {@code SemanticGraph}s cache which maps each comment to its {@code SemanticGraph}.
 */
public class Parser {

  /** The regular expressions used to identify patterns in the comment */
  private static final String INEQUALITY_NUMBER_REGEX =
      " *(?!-)((([<>=]=?)|(!=)) ?)-?([0-9]+(?!/)(.[0-9]+)?|zero|one|two|three|four|five|six|seven|eight|nine)";

  private static final String INEQUALITY_NULL_REGEX = "([=]=?|!=) ?null";

  private static final String GENERIC_TYPE_REGEX = " *(<T>)";

  private static final String RANGE_VAR_REGEX =
      " * ?([a-zA-Z0-9]+) ?([<>=]=?) ?([a-zA-Z]+) ?([<>=]=?) ?([a-zA-Z0-9]+)";

  private static final String INEQUALITY_VAR_REGEX =
      " *(?<!-)(([<>=]=?|!=) ?)(?!this)((?![a-zA-Z]+\\()([a-zA-Z][a-zA-Z0-9_]*)|([_][a-zA-Z0-9_]+))(\\.[a-zA-Z0-9_]+(\\(*\\))?)?";
  private static final String PLACEHOLDER_PREFIX = " INEQUALITY_";
  private static final String INEQ_INSOF =
      "(?<!has )(?<!have )an (instance of)"; // e.g "an instance of"
  private static final String INEQ_INSOFPROCESSED =
      " instanceof +[^ \\.]*"; // e.g. "instanceof BinaryMutation"
  private static final String INEQ_THIS = "(?<!of) this\\."; // e.g "<object> is this."

  /** Stores the inequalities that are replaced by placeholders when addPlaceholders is called. */
  private static List<String> inequalities = new ArrayList<>();

  /** Stores the cache of semantic graphs for each pair method-comment. */
  private static Map<MethodComment, List<SemanticGraph>> graphsCache = new HashMap<>();

  private Parser() {}

  /**
   * Store in cache the semantic graphs for a pair comment, method.
   *
   * @param comment the comment object
   * @param method the DocumentedExecutable
   */
  private static List<SemanticGraph> parse_(Comment comment, DocumentedExecutable method) {
    // Check if cache contains a valid answer.
    MethodComment key = new MethodComment(comment, method);
    if (graphsCache.containsKey(key)) {
      return graphsCache.get(key);
    }

    List<SemanticGraph> graphs = new ArrayList<>();
    Comment commentWithPlaceholders = addPlaceholders(comment);
    List<String> arguments = new ArrayList<>();
    if (method != null) {
      // Collect method arguments
      arguments =
          method.getParameters().stream().map(DocumentedParameter::getName).collect(toList());
    }
    // Extract sentences in comment with placeholders
    final List<List<HasWord>> sentences =
        StanfordParser.tokenize(commentWithPlaceholders.getText());
    for (List<HasWord> sentence : sentences) {
      final List<TaggedWord> taggedWords =
          POSTagger.tagWords(
              comment, commentWithPlaceholders.getText(), inequalities, sentence, arguments);
      final SemanticGraph semanticGraph = StanfordParser.parse(taggedWords);
      graphs.add(semanticGraph);
    }
    graphsCache.put(new MethodComment(comment, method), graphs);
    return graphs;
  }

  /**
   * Takes a Comment object and returns a list of {@code PropositionSeries} objects, one for each
   * sentence in the comment.
   *
   * @param comment object representing a Javadoc comment
   * @param method the DocumentedExecutable under analysis
   * @return a list of {@code PropositionSeries} objects, one for each sentence in the comment
   */
  // TODO Move this to a new class PropositionIdentifier that handles Proposition.
  public static List<PropositionSeries> parse(Comment comment, DocumentedExecutable method) {
    List<PropositionSeries> result = new ArrayList<>();
    List<SemanticGraph> semanticGraphs = parse_(comment, method);
    for (SemanticGraph semanticGraph : semanticGraphs) {
      result.add(new SentenceParser(semanticGraph).getPropositionSeries());
    }
    return removePlaceholders(result);
  }

  /**
   * Verifies if the comment contains a verb among the {@code possibleVerbs}. If it doesn't, the
   * verb is assumed to be "is" and is added to the comment.
   *
   * @param placeholderText the comment text containing placeholders
   * @param i counter of the placeholders in the text
   * @return the placeholderText, updated with the added verb or as it was if it already had one
   */
  private static String findVerb(String placeholderText, int i) {
    // Verbs that could appear before (the inequality, or the keyword this, etc.).
    // One of these most be present and will be added otherwise.
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
    return placeholderText;
  }

  private static Comment addPlaceholders(Comment comment) {

    ArrayList<String> contentToIgnore = new ArrayList<>();

    String text =
        comment
            .getText()
            .replace("greater than or equal to", ">=")
            .replace("greater or equal to", ">=")
            .replace("≥", ">=")
            .replace("less than or equal to", "<=")
            .replace("lesser than or equal to", "<=")
            .replace("lesser or equal to", "<=")
            .replace("smaller than or equal to", "<=")
            .replace("lower than or equal to", "<=")
            .replace("≤", "<=")
            .replace("greater than", ">")
            .replace("smaller than", "<")
            .replace("less than", "<")
            .replace("lesser than", "<")
            .replace("lower than", "<")
            .replace("equal to", "==");

    java.util.regex.Matcher matcherInstanceOf = Pattern.compile(INEQ_INSOF).matcher(text);

    java.util.regex.Matcher matcherThis = Pattern.compile(INEQ_THIS).matcher(text);

    java.util.regex.Matcher matcherGeneric = Pattern.compile(GENERIC_TYPE_REGEX).matcher(text);

    java.util.regex.Matcher matcherRangeVar = Pattern.compile(RANGE_VAR_REGEX).matcher(text);

    java.util.regex.Matcher matcherVarComp = Pattern.compile(INEQUALITY_VAR_REGEX).matcher(text);

    java.util.regex.Matcher matcherIneqNumber =
        Pattern.compile(INEQUALITY_NUMBER_REGEX).matcher(text);

    java.util.regex.Matcher matcherIneqNull = Pattern.compile(INEQUALITY_NULL_REGEX).matcher(text);

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
      // Specific case for the instance of placeholder. We put into inequalities the instanceof and
      // the name of the class.
      inequalities.add(text.substring(matcherIOfProcessed.start(), matcherIOfProcessed.end()));
      placeholderText = placeholderText.replaceFirst(INEQ_INSOFPROCESSED, PLACEHOLDER_PREFIX + i++);
    }

    while (matcherThis.find()) {
      inequalities.add(text.substring(matcherThis.start(), matcherThis.end()));
      placeholderText = placeholderText.replaceFirst(INEQ_THIS, PLACEHOLDER_PREFIX + i);
      placeholderText = findVerb(placeholderText, i);
      i++;
    }

    while (matcherGeneric.find()) {
      placeholderText = placeholderText.replaceFirst(GENERIC_TYPE_REGEX, "IGNORE_ME");
      contentToIgnore.add(matcherGeneric.group(0));
    }

    while (matcherRangeVar.find()) {
      placeholderText = placeholderText.replaceFirst(RANGE_VAR_REGEX, "IGNORE_ME");
      contentToIgnore.add(matcherRangeVar.group(0));
    }

    while (matcherIneqNumber.find()) {
      inequalities.add(text.substring(matcherIneqNumber.start(), matcherIneqNumber.end()));
      placeholderText =
          placeholderText.replaceFirst(INEQUALITY_NUMBER_REGEX, PLACEHOLDER_PREFIX + i);
      placeholderText = findVerb(placeholderText, i);
      i++;
    }

    while (matcherIneqNull.find()) {
      inequalities.add(text.substring(matcherIneqNull.start(), matcherIneqNull.end()));
      placeholderText = placeholderText.replaceFirst(INEQUALITY_NULL_REGEX, PLACEHOLDER_PREFIX + i);
      placeholderText = findVerb(placeholderText, i);
      i++;
    }

    while (matcherVarComp.find()) {
      inequalities.add(text.substring(matcherVarComp.start(), matcherVarComp.end()));
      placeholderText = placeholderText.replaceFirst(INEQUALITY_VAR_REGEX, PLACEHOLDER_PREFIX + i);
      placeholderText = findVerb(placeholderText, i);
      i++;
    }

    for (String ignoredString : contentToIgnore) {
      placeholderText = placeholderText.replaceFirst("IGNORE_ME", ignoredString);
    }

    return new Comment(placeholderText, comment.getWordsMarkedAsCode());
  }

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
      final SemanticGraph semanticGraph = series.getSemanticGraph();
      List<Proposition> inequalityPropositions = new ArrayList<>();
      for (Proposition placeholderProposition : series.getPropositions()) {
        Subject subject = placeholderProposition.getSubject();
        String subjectAsString = subject.getSubject();
        String predicate = placeholderProposition.getPredicate();

        for (int i = 0; i < inequalities.size(); i++) {
          subjectAsString = subjectAsString.replaceAll(PLACEHOLDER_PREFIX + i, inequalities.get(i));
          predicate = predicate.replaceAll(PLACEHOLDER_PREFIX + i, inequalities.get(i));
        }
        subject.setSubject(subjectAsString); // Replace subject string representation.

        inequalityPropositions.add(
            new Proposition(subject, predicate, placeholderProposition.isNegative()));
      }
      final PropositionSeries newSeries =
          new PropositionSeries(semanticGraph, inequalityPropositions, series.getConjunctions());
      result.add(newSeries);
    }

    inequalities.clear();
    return result;
  }
}

/** This class ties a String comment to its DocumentedMethod. */
class MethodComment {
  private Comment comment;
  private DocumentedExecutable method;

  public MethodComment(Comment comment, DocumentedExecutable method) {
    this.comment = comment;
    this.method = method;
  }

  public Comment getComment() {
    return comment;
  }

  public DocumentedExecutable getMethod() {
    return method;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MethodComment that = (MethodComment) o;

    if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
    return method != null ? method.equals(that.method) : that.method == null;
  }

  @Override
  public int hashCode() {
    int result = comment != null ? comment.hashCode() : 0;
    result = 31 * result + (method != null ? method.hashCode() : 0);
    return result;
  }
}
