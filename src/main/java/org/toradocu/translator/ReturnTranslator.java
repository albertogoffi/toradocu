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
import org.toradocu.extractor.ExecutableMember;
import org.toradocu.extractor.Parameter;
import org.toradocu.extractor.ReturnTag;
import org.toradocu.translator.spec.Guard;
import org.toradocu.translator.spec.Postcondition;
import org.toradocu.translator.spec.Specification;

public class ReturnTranslator implements Translator<ReturnTag> {

  private static final String ARITHMETIC_OP_REGEX =
      "(([a-zA-Z]+[0-9]?_?)+) ?([-+*/%]) ?(([a-zA-Z]+[0-9]?_?)+)";

  /**
   * Translate arithemtic operations between arguments, if found.
   *
   * @param method the ExecutableMember
   * @param commentToTranslate String comment to translate
   * @return the translation
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
      int firstIndex = searchForCode(firstFactor, method);
      if (firstIndex != -1) {
        int secIndex = searchForCode(secFactor, method);
        if (secIndex != -1)
          translation = "true ? result==args[" + firstIndex + "]" + op + "args[" + secIndex + "]";
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

  private static int searchForCode(String text, ExecutableMember method) {
    List<String> arguments =
        method.getParameters().stream().map(Parameter::getName).collect(toList());
    for (int i = 0; i < arguments.size(); i++) if (arguments.get(i).equals(text)) return i;

    return -1;
  }

  /**
   * Translates the given {@code text} that is the second part of an @return Javadoc comment. Here
   * "second part" means every word of the conditional clause. Example: {@code @return true if the
   * parameter is positive, false otherwise}. In this comment the second part is "if the parameter
   * is positive, false otherwise". This method translates only the "false otherwise" part.
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
      String[] splittedText = text.split(" ");
      for (int i = 0; i < splittedText.length; i++) {
        int index = searchForCode(splittedText[i], method);
        if (index != -1) return "result == args[" + index + "]";
        //the empty String was found
        else if (splittedText[i].equals("\"\"")) return "result.equals(\"\")";
      }
    }
    return null;
  }

  /**
   * Called if all the previous stantard tentatives of matching failed.
   *
   * @param method the ExecutableMember under analysis
   * @param comment the comment to translate
   * @return a match if found, otherwise null
   */
  private static String lastAttemptMatch(ExecutableMember method, String comment) {
    //Try a match looking at the semantic graph.
    String match = null;
    comment = comment.replace(";", "").replace(",", "");
    for (SemanticGraph sg : Parser.getSemanticgraphs(comment, method)) {
      //First: search for a verb.
      List<IndexedWord> verbs = sg.getAllNodesByPartOfSpeechPattern("VB(.*)");
      if (!verbs.isEmpty()) {
        List<PropositionSeries> extractedPropositions =
            Parser.getPropositionSeries(comment, method);
        for (PropositionSeries prop : extractedPropositions) {
          Set<String> conditions = new LinkedHashSet<>();
          for (Proposition p : prop.getPropositions()) {
            match =
                Matcher.predicateMatch(
                    method, new GeneralCodeElement("result"), p.getPredicate(), p.isNegative());
            if (match != null) break;
          }
        }
      } else { // No verb found: process nouns and their adjectives
        List<IndexedWord> nouns = sg.getAllNodesByPartOfSpeechPattern("NN(.*)");
        List<IndexedWord> adj = sg.getAllNodesByPartOfSpeechPattern("JJ(.*)");
        if (match == null) {
          String wordToMatch = "";
          for (IndexedWord n : nouns) {
            for (IndexedWord a : adj) wordToMatch += a.word();
            wordToMatch += n.word();
            Set<CodeElement<?>> subject = Matcher.subjectMatch(wordToMatch, method);
            if (!subject.isEmpty()) match = subject.stream().findFirst().get().getJavaExpression();
          }
        }
      }
    }
    return match;
  }

  /**
   * Translates the given {@code text} that is the second part of an @return Javadoc comment. Here
   * "second part" means every word of the conditional clause. Example: {@code @return true if the
   * parameter is null}. In this comment the second part is "if the parameter is null".
   *
   * @param text words representing the second part of an @return comment
   * @param method the method to which the @return tag belongs to
   * @return the translation of the given {@code text}
   */
  private static String translateSecondPart(String text, ExecutableMember method) {
    // Identify propositions in the comment. Each sentence in the comment is parsed into a
    // PropositionSeries.

    //text = removeInitial(text, "if");  already done in Preprocess part
    List<PropositionSeries> extractedPropositions = Parser.getPropositionSeries(text, method);
    Set<String> conditions = new LinkedHashSet<>();
    // Identify Java code elements in propositions.
    for (PropositionSeries propositions : extractedPropositions) {
      ConditionTranslator.translate(propositions, method);
      conditions.add(propositions.getTranslation());
    }
    return mergeConditions(conditions);
  }

  /**
   * Translates the given {@code text} that is the first part of an @return Javadoc comment. Here
   * "first part" means every word before the start of the conditional clause. Example:
   * {@code @return true if the parameter is null}. In this comment the first part is "true".
   *
   * @param text words representing the first part of an @return comment
   * @param method the ExecutableMember the @return comment belongs to
   * @return the translation of the given {@code text}
   * @throws IllegalArgumentException if the given {@code text} cannot be translated
   */
  private static String translateFirstPart(String text, ExecutableMember method) {
    String lowerCaseText = text.trim().toLowerCase();
    String match = null;
    switch (lowerCaseText) {
      case "true":
      case "false":
        return "result == " + lowerCaseText;
      default:
        {
          int index = searchForCode(text, method);
          if (index != -1) return "result == args[" + index + "]";
          else {
            match = lastAttemptMatch(method, text);
            if (match != null) {
              if (!match.contains("result==")) return "result.equals(" + match + ")";
              else return match;
            }
          }
        }
    }
    //TODO: Change the exception with one more meaningful.
    //    throw new IllegalArgumentException(text + " cannot be translated: Pattern not supported");
    return match;
  }

  /**
   * This method attempts to translate the return tag according to the classical pattern.
   *
   * @param method the ExecutableMember
   * @param comment the String comment to translate
   * @param predicateSplitPoint index of the "if"
   * @return the translation computed
   */
  private static String returnStandardPattern(
      ExecutableMember method, String comment, int predicateSplitPoint) {
    String translation = "";
    if (comment.contains(";")) comment = comment.replace(";", ",");
    String predicate = comment.substring(0, predicateSplitPoint);
    final String[] tokens = comment.substring(predicateSplitPoint + 3).split(",", 2);
    String trueCase = tokens[0];
    String falseCase = tokens.length > 1 ? tokens[1] : "";

    if (!predicate.isEmpty() && !trueCase.isEmpty()) {
      //          try {
      String predicateTranslation = translateFirstPart(predicate, method);
      if (predicateTranslation != null) {
        String conditionTranslation = translateSecondPart(trueCase, method);

        if (!predicateTranslation.isEmpty() && !conditionTranslation.isEmpty()) {
          translation = conditionTranslation + " ? " + predicateTranslation;
          // Else case might not be present.
          String elsePredicate = translateLastPart(falseCase, method);
          if (elsePredicate != null) {
            translation = translation + " : " + elsePredicate;
          }
        }
      }
      /* To validate the return values of the method under test we will have a code similar
       * to this in the generated aspect:
       *
       * if ( condition ) {
       *   return check;
       * else {
       *   return anotherCheck;
       * }
       *
       * Example: @return true if the values are equal.
       * translation: args[0].equals(args[1]) ? true
       *
       * if (args[0].equals(args[1])) {
       *   return result.equals(true);
       * }
       *
       * We use the characters '?' and ':' to separate the different parts of the translation.
       * We need that because the translation is currently a plain a String.
       */
      //          }
      //          catch (IllegalArgumentException e) {
      //            //TODO: Change the exception with one more meaningful.
      //            // Pattern not supported.
      //            log.warn("Unable to translate: \"" + comment + "\"");
      //          }

      else {
        String match = lastAttemptMatch(method, comment);
        if (match != null) translation = match;
        else translation = "";
      }
    }
    return translation;
  }

  @Override
  public Specification translate(ReturnTag tag, ExecutableMember excMember) {
    String comment = tag.getComment();
    // Assumption: the comment is composed of a single sentence. We should probably split multiple
    // sentence comments using the Stanford Parser, and then work on each single sentence.
    String translation = "";

    // Split the sentence in three parts: predicate + true case + false case.
    // TODO Naive splitting. Make the split more reliable.
    final int predicateSplitPoint = comment.indexOf(" if ");
    if (predicateSplitPoint != -1) {
      translation = returnStandardPattern(excMember, comment, predicateSplitPoint);
    } else {
      final String[] truePatterns = {"true", "true always"};
      final String[] falsePatterns = {"false", "false always"};
      final String commentToTranslate = comment;

      // Comments always end with a period (added by Toradocu where missing). Therefore, we
      // have to add period(s) here.
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
        translation = manageArithmeticOperation(excMember, commentToTranslate);
        if (translation.equals("")) {
          // All the previous attempts failed: try the last strategies (e.g. search for missing subjects)
          String match = lastAttemptMatch(excMember, comment);
          if (match != null) {
            if (match.contains("result")) translation = "true ?" + match;
            else translation = "true ? result.equals(" + match + ")";
          }
        }
      }
    }

    // TODO Create the specification with the derived merged conditions.

    return parseTranslation(translation);
  }

  private Specification parseTranslation(String translation) {
    String[] splitTranslation = translation.split("//?");
    String guard = splitTranslation[0];
    String[] properties = splitTranslation[1].split(":");
    String trueProp = properties[0];
    String falseProp = properties[1];
    if (falseProp == null) falseProp = "";

    return new Postcondition(new Guard(guard), new Guard(trueProp), new Guard(falseProp));
  }
}
