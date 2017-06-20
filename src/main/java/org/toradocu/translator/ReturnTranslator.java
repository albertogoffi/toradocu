package org.toradocu.translator;

import static java.util.stream.Collectors.toList;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import org.toradocu.extractor.Comment;
import org.toradocu.extractor.ExecutableMember;
import org.toradocu.extractor.ReturnTag;
import org.toradocu.translator.spec.Postcondition;
import org.toradocu.translator.spec.Specification;

public class ReturnTranslator implements Translator<ReturnTag> {

  private static final String ARITHMETIC_OP_REGEX =
      "(([a-zA-Z]+[0-9]?_?)+) ?([-+*/%]) ?(([a-zA-Z]+[0-9]?_?)+)";

  /**
   * Translate arithmetic operations involved in the return, if found.
   *
   * @param method the ExecutableMember
   * @param commentToTranslate String comment to translate
   * @return the translation if any, or an empty String
   */
  private static String manageArithmeticOperation(
      ExecutableMember method, String commentToTranslate) {
    String translation = "";
    java.util.regex.Matcher matcherOp =
        Pattern.compile(ARITHMETIC_OP_REGEX).matcher(commentToTranslate);
    if (matcherOp.find()) {
      String firstFactor = matcherOp.group(1);
      String secFactor = matcherOp.group(4);
      String op = matcherOp.group(3);

      final List<PropositionSeries> extractedPropositions =
          Parser.parse(new Comment(commentToTranslate), method);
      final List<SemanticGraph> semanticGraphs =
          extractedPropositions.stream().map(PropositionSeries::getSemanticGraph).collect(toList());

      CodeElement<?> first = findCodeElement(method, firstFactor, semanticGraphs);
      if (first != null) {
        CodeElement<?> second = findCodeElement(method, secFactor, semanticGraphs);
        if (second != null)
          translation =
              "true ? result==" + first.getJavaExpression() + op + second.getJavaExpression();
      }
    }
    return translation;
  }

  /**
   * Returns a boolean Java expression that merges the conditions from the given set of conditions.
   * Each condition in the set is combined using an || conjunction.
   *
   * @param conditions the translated conditions for a throws tag (as Java boolean conditions)
   * @return a boolean Java expression that is true only if any of the given conditions is true
   */
  private static String mergeConditions(Set<String> conditions) {
    conditions.removeIf(String::isEmpty); // TODO Why should we have empty conditions here?

    String delimiter = " " + Conjunction.OR + " ";
    StringJoiner joiner = new StringJoiner(delimiter);
    for (String condition : conditions) {
      joiner.add("(" + condition + ")");
    }
    return joiner.toString();
  }

  /**
   * Translates the given {@code text} that is the second part of an @return Javadoc comment
   * according to the "standard pattern". Here "second part" means every word of the conditional
   * clause. Example: {@code @return true if the parameter is positive, false otherwise}. In this
   * comment the second part is "if the parameter is positive, false otherwise". This method
   * translates only the "false otherwise" part.
   *
   * @param text words representing the second part of an @return comment
   * @param method the method to which the @return tag belongs to
   * @return the translation of the given {@code text}
   */
  private static String translateLastPart(String text, ExecutableMember method) {
    final String lowerCaseText = text.toLowerCase();
    if (lowerCaseText.contains("true")) {
      return "result == true";
    } else if (lowerCaseText.contains("false")) {
      return "result == false";
    } else {
      // The result is not a plain boolean, so it must be a code element.
      String[] splittedText = text.split(" ");
      for (int i = 0; i < splittedText.length; i++) {
        if (!splittedText[i].equals("")) {
          final List<PropositionSeries> extractedPropositions =
              Parser.parse(new Comment(splittedText[i]), method);
          final List<SemanticGraph> semanticGraphs =
              extractedPropositions
                  .stream()
                  .map(PropositionSeries::getSemanticGraph)
                  .collect(toList());

          String translation = tryCodeElementMatch(method, splittedText[i], semanticGraphs);
          if (translation != null) return translation;
          //the empty String was found
          else if (splittedText[i].equals("\"\"")) return "result.equals(\"\")";
        }
      }
    }
    return null;
  }

  /**
   * Translates the given {@code text} that is the second part of an @return Javadoc comment
   * according to the "standard pattern". Here "second part" means every word of the conditional
   * clause. Example: {@code @return true if the parameter is null}. In this comment the second part
   * is "if the parameter is null".
   *
   * @param trueCase words representing the second part of an @return comment
   * @param method the method to which the @return tag belongs to
   * @return the translation of the given {@code text}
   */
  private static String translateSecondPart(String trueCase, ExecutableMember method) {
    // Identify propositions in the comment. Each sentence in the comment is parsed into a
    // PropositionSeries.

    //text = removeInitial(text, "if");  already done in Preprocess part
    List<PropositionSeries> extractedPropositions = Parser.parse(new Comment(trueCase), method);
    Set<String> conditions = new LinkedHashSet<>();
    for (PropositionSeries propositions : extractedPropositions) {
      BasicTranslator.translate(propositions, method);
      conditions.add(propositions.getTranslation());
    }
    return mergeConditions(conditions);
  }

  /**
   * Translates the given {@code text} that is the first part of an @return Javadoc comment
   * according to the "standard pattern". Here "first part" means every word before the start of the
   * conditional clause. Examples: {@code @return true if the parameter is null}. In this comment
   * the first part is "true". In {@code @return a if a is lesser or equal to b, b otherwise} the
   * first part is "a", i.e. a code element. In {@code @return the converted value, is null if the
   * parameter is null} the first part is "result==null", with the complete translation being
   * "args[0]==null?result==null".
   *
   * @param predicate words representing the first part of an @return comment
   * @param method the ExecutableMember the @return comment belongs to
   * @return the translation of the given {@code text}
   * @throws IllegalArgumentException if the given {@code text} cannot be translated
   */
  private static String translateFirstPart(String predicate, ExecutableMember method) {
    String lowerCaseText = predicate.trim().toLowerCase();
    String translation;
    switch (lowerCaseText) {
      case "true":
      case "false":
        return "result == " + lowerCaseText;
      default:
        {
          //No return of type boolean: it must be a more complex boolean condition, or a code element.
          final List<PropositionSeries> extractedPropositions =
              Parser.parse(new Comment(predicate), method);
          final List<SemanticGraph> semanticGraphs =
              extractedPropositions
                  .stream()
                  .map(PropositionSeries::getSemanticGraph)
                  .collect(toList());

          translation =
              tryPredicateMatch(method, lowerCaseText, semanticGraphs, extractedPropositions);
          if (translation == null)
            translation = tryCodeElementMatch(method, lowerCaseText, semanticGraphs);
        }
    }
    //TODO: Change the exception with one more meaningful.
    //    throw new IllegalArgumentException(text + " cannot be translated: Pattern not supported");
    return translation;
  }

  /**
   * This method attempts to translate the return tag according to the classical pattern. Thus, it
   * will follow the call chain made by: translateFirstPart|translateSecondPart|translateLastPart.
   * This "standard pattern" applies only when the return tag comment contains an "if".
   *
   * @param method the ExecutableMember
   * @param comment the String comment to translate
   * @param predicateSplitPoint index of the "if"
   * @return the translation computed
   */
  private static String returnStandardPattern(
      ExecutableMember method, Comment comment, int predicateSplitPoint) {
    String translation = "";
    String commentText = comment.getText();
    if (commentText.contains(";")) {
      commentText = commentText.replace(";", ",");
    }
    String predicate = commentText.substring(0, predicateSplitPoint);
    final String[] tokens = commentText.substring(predicateSplitPoint + 3).split(",", 2);
    String trueCase = tokens[0];
    String falseCase = tokens.length > 1 ? tokens[1] : "";

    if (!predicate.isEmpty() && !trueCase.isEmpty()) {
      String predicateTranslation = translateFirstPart(predicate, method);
      if (predicateTranslation != null) {
        String conditionTranslation = translateSecondPart(trueCase, method);

        if (!predicateTranslation.isEmpty() && !conditionTranslation.isEmpty()) {
          translation = conditionTranslation + " ? " + predicateTranslation;
          String elsePredicate = translateLastPart(falseCase, method);
          if (elsePredicate != null) {
            translation = translation + " : " + elsePredicate;
          }
        }
      }
    }

    return translation;
  }

  /**
   * Return "not standard" pattern means that the comment referring to the return tag does not
   * contain an "if". Thus, it will not follow the call chain made by:
   * translateFirstPart|translateSecondPart|translateLastPart. In this pattern, the translation will
   * always be something like: "true ? [condition over result]". result in the condition could be
   * compared to: a plain true/false value; a more complex boolean expression, meaning that we must
   * check some property of the result; a code element.
   *
   * @param method the ExecutableMember the tag belongs to
   * @param comment the String comment belonging to the tag
   * @return a String translation if any, or an empty string
   */
  private static String returnNotStandard(ExecutableMember method, String comment) {
    String translation;
    final String[] truePatterns = {"true", "true always"};
    final String[] falsePatterns = {"false", "false always"};
    final String commentToTranslate = comment;

    final boolean truePatternsMatch =
        Arrays.stream(truePatterns)
            .map(p -> p.concat("."))
            .anyMatch(p -> p.equalsIgnoreCase(commentToTranslate));
    final boolean falsePatternsMatch =
        Arrays.stream(falsePatterns)
            .map(p -> p.concat("."))
            .anyMatch(p -> p.equalsIgnoreCase(commentToTranslate));

    if (truePatternsMatch) {
      translation = "true ? result==true";
    } else if (falsePatternsMatch) {
      translation = "true ? result==false";
    } else {
      translation = manageArithmeticOperation(method, commentToTranslate);
      if (translation.equals("")) {
        final List<PropositionSeries> extractedPropositions =
            Parser.parse(new Comment(comment), method);
        final List<SemanticGraph> semanticGraphs =
            extractedPropositions
                .stream()
                .map(PropositionSeries::getSemanticGraph)
                .collect(toList());

        translation = tryPredicateMatch(method, comment, semanticGraphs, extractedPropositions);
        if (translation == null) translation = tryCodeElementMatch(method, comment, semanticGraphs);
      }
    }
    return translation;
  }

  /**
   * Check if the type of the code element is primitive or not.
   *
   * @param codeElement the code element for which verifying the type
   * @return true if the type is primitive, false otherwise
   */
  private static boolean checkIfPrimitive(CodeElement<?> codeElement) {
    //TODO naive, don't we have any better?
    String[] primitives = {"int", "char", "float", "double", "long", "short", "byte"};
    Set<String> ids = codeElement.getIdentifiers();
    for (int i = 0; i != primitives.length; i++) if (ids.contains(primitives[i])) return true;

    return false;
  }

  /**
   * Called to search for a complex boolean condition involved in the return tag. Example: "return
   * x, must not be null" produces (result==null) == false .
   *
   * @param method the ExecutableMember the tag belongs to
   * @param comment the String comment belonging to the tag
   * @param semanticGraphs list of {@code SemanticGraph} related to the comment
   * @param extractedPropositions list of {@code PropositionSeries} extracted from the comment
   * @return a String predicate match if any, or null
   */
  private static String tryPredicateMatch(
      ExecutableMember method,
      String comment,
      List<SemanticGraph> semanticGraphs,
      List<PropositionSeries> extractedPropositions) {
    String predicateMatch = null;
    comment = comment.replace(";", "").replace(",", "");

    for (SemanticGraph sg : semanticGraphs) {
      List<IndexedWord> verbs = sg.getAllNodesByPartOfSpeechPattern("VB(.*)");
      if (!verbs.isEmpty()) {
        for (PropositionSeries prop : extractedPropositions) {
          for (Proposition p : prop.getPropositions()) {
            predicateMatch =
                new Matcher()
                    .predicateMatch(
                        method, new GeneralCodeElement("result"), p.getPredicate(), p.isNegative());
            if (predicateMatch != null) break;
          }
        }
      }
    }
    return predicateMatch;
  }

  /**
   * Search for a code element in the comment text of the return tag and produces a suitable
   * translation, if any.
   *
   * @param method the {@code ExecutableMember} the comment belongs to
   * @param text the comment text
   * @param semanticGraphs the {@code SemanticGraph} related to the comment
   * @return a String translation if any, null otherwise
   */
  private static String tryCodeElementMatch(
      ExecutableMember method, String text, List<SemanticGraph> semanticGraphs) {
    CodeElement<?> codeElementMatch = findCodeElement(method, text, semanticGraphs);
    if (codeElementMatch != null) {
      boolean isPrimitive = checkIfPrimitive(codeElementMatch);
      if (isPrimitive) return "result == " + codeElementMatch.getJavaExpression();
      else return "result.equals(" + codeElementMatch.getJavaExpression() + ")";
    }
    return null;
  }

  /**
   * Called by tryCodeElementMatch to search for a code element involved in the return tag.
   *
   * @param method the ExecutableMember the tag belongs to
   * @param comment the String comment belonging to the tag
   * @param semanticGraphs list of {@code SemanticGraph} related to the comment
   * @return a code element if any, or null
   */
  private static CodeElement<?> findCodeElement(
      ExecutableMember method, String comment, List<SemanticGraph> semanticGraphs) {
    //Try a match looking at the semantic graph.
    CodeElement<?> codeElementMatch = null;
    comment = comment.replace(";", "").replace(",", "").replace("'", "");
    for (SemanticGraph sg : semanticGraphs) {
      // No verb found: process nouns and their adjectives
      List<IndexedWord> nouns = sg.getAllNodesByPartOfSpeechPattern("NN(.*)");
      List<IndexedWord> adj = sg.getAllNodesByPartOfSpeechPattern("JJ(.*)");
      String wordToMatch = "";
      for (IndexedWord n : nouns) {
        for (IndexedWord a : adj) wordToMatch += a.word();
        wordToMatch += n.word();
        Set<CodeElement<?>> subject = new Matcher().subjectMatch(wordToMatch, method);
        if (!subject.isEmpty()) codeElementMatch = subject.stream().findFirst().get();
      }
    }
    return codeElementMatch;
  }

  @Override
  public Specification translate(ReturnTag tag, ExecutableMember excMember) {
    String comment = tag.getComment().getText();
    // Assumption: the comment is composed of a single sentence. We should probably split multiple
    // sentence comments using the Stanford Parser, and then work on each single sentence.
    final String translation;

    // Split the sentence in three parts: predicate + true case + false case.
    // TODO Naive splitting. Make the split more reliable.
    final int predicateSplitPoint = comment.indexOf(" if ");
    if (predicateSplitPoint != -1) {
      translation = returnStandardPattern(excMember, tag.getComment(), predicateSplitPoint);
    } else {
      translation = returnNotStandard(excMember, comment);
    }

    // TODO Create the specification with the derived merged conditions.
    // TODO shoulnd't we check if translation is null/empty before trying the parse?
    return Postcondition.create(translation);
  }
}
