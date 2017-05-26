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
import org.toradocu.extractor.ExecutableMember;
import org.toradocu.extractor.Parameter;

/** Created by arianna on 18/05/17. */
public class Parser {

  /** The regular expressions used to identify patterns in the comment */
  private static final String INEQUALITY_NUMBER_REGEX =
      " *((([<>=]=?)|(!=)) ?)-?([0-9]+(?!/)(.[0-9]+)?|zero|one|two|three|four|five|six|seven|eight|nine)";

  private static final String INEQUALITY_VAR_REGEX =
      " *((([<>=]=?)|(!=)) ?)(?!this)((([a-zA-Z]+([0-9]?))+_?(?! ))+(.([a-zA-Z]+([0-9]?))+(\\(*\\))?)?)";
  private static final String ARITHMETIC_OP_REGEX =
      "(([a-zA-Z]+[0-9]?_?)+) ?([-+*/%]) ?(([a-zA-Z]+[0-9]?_?)+)";
  private static final String PLACEHOLDER_PREFIX = " INEQUALITY_";
  private static final String INEQ_INSOF = " *[an]* (instance of)"; // e.g "an instance of"
  private static final String INEQ_INSOFPROCESSED =
      " instanceof +[^ \\.]*"; // e.g. "instanceof BinaryMutation"
  private static final String INEQ_THIS = " this\\."; // e.g "<object> is this."

  /** Stores the inequalities that are replaced by placeholders when addPlaceholders is called. */
  private static List<String> inequalities = new ArrayList<>();

  /** Stores the cache of semantic graphs for each pair method-comment. */
  private static Map<MethodComment, List<SemanticGraph>> graphsCache = new HashMap<>();

  private Parser() {}

  /**
   * Store in cache the semantic graphs for a pair comment, method.
   *
   * @param comment the comment object
   * @param method the ExecutableMember
   */
  private static List<SemanticGraph> parse_(Comment comment, ExecutableMember method) {
    // Check if cache contains a valid answer.
    MethodComment key = new MethodComment(comment, method);
    if (graphsCache.containsKey(key)) {
      return graphsCache.get(key);
    }

    List<SemanticGraph> graphs = new ArrayList<>();
    Comment commentWithPlaceholders = addPlaceholders(comment);
    List<String> arguments = new ArrayList<>();
    List<String> codeElements = new ArrayList<>();
    if (method != null)
      // Collect method arguments
      arguments = method.getParameters().stream().map(Parameter::getName).collect(toList());

    // Extract sentences in comment with placeholders
    final List<List<HasWord>> sentences =
        StanfordParser.tokenize(commentWithPlaceholders.getText());
    for (List<HasWord> sentence : sentences) {
      for (HasWord word : sentence) {
        // For every word in the sentence, check if it is an argument and update the code elements list
        if (arguments.contains(word.toString())) codeElements.add(word.word());
      }
      // Update code elements list also with words marked as @code
      codeElements.addAll(comment.getWordsMarkedAsCode());
      final List<TaggedWord> taggedWords = POSTagger.tagWords(sentence, codeElements);
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
   * @param method the ExecutableMember under analysis
   * @return a list of {@code PropositionSeries} objects, one for each sentence in the comment
   */
  // TODO Move this to a new class PropositionIdentifier that handles Proposition.
  public static List<PropositionSeries> parse(Comment comment, ExecutableMember method) {
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
    //One of these most be present and will be added otherwise.
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
    String text =
        comment
            .getText()
            .replace("greater than or equal to", ">=")
            .replace("≥", ">=")
            .replace("less than or equal to", "<=")
            .replace("lesser than or equal to", "<=")
            .replace("lesser or equal to", "<=")
            .replace("≤", "<=")
            .replace("greater than", ">")
            .replace("smaller than or equal to", "<=")
            .replace("smaller than", "<")
            .replace("less than", "<")
            .replace("lesser than", "<")
            .replace("equal to", "==");

    java.util.regex.Matcher matcher = Pattern.compile(INEQUALITY_NUMBER_REGEX).matcher(text);

    java.util.regex.Matcher matcherInstanceOf = Pattern.compile(INEQ_INSOF).matcher(text);

    java.util.regex.Matcher matcherThis = Pattern.compile(INEQ_THIS).matcher(text);

    java.util.regex.Matcher matcherVarComp = Pattern.compile(INEQUALITY_VAR_REGEX).matcher(text);

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

    while (matcher.find()) {
      inequalities.add(text.substring(matcher.start(), matcher.end()));
      placeholderText =
          placeholderText.replaceFirst(INEQUALITY_NUMBER_REGEX, PLACEHOLDER_PREFIX + i);
      placeholderText = findVerb(placeholderText, i);
      i++;
    }

    while (matcherVarComp.find()) {
      inequalities.add(text.substring(matcherVarComp.start(), matcherVarComp.end()));
      placeholderText = placeholderText.replaceFirst(INEQUALITY_VAR_REGEX, PLACEHOLDER_PREFIX + i);
      placeholderText = findVerb(placeholderText, i);
      i++;
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
      result.add(new PropositionSeries(inequalityPropositions, series.getConjunctions()));
    }

    inequalities.clear();
    return result;
  }
}

/** This class ties a String comment to its DocumentedMethod. */
class MethodComment {
  private Comment comment;
  private ExecutableMember method;

  public MethodComment(Comment comment, ExecutableMember method) {
    this.comment = comment;
    this.method = method;
  }

  public Comment getComment() {
    return comment;
  }

  public ExecutableMember getMethod() {
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
