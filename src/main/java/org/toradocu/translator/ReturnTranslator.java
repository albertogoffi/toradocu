package org.toradocu.translator;

import static java.util.stream.Collectors.toList;
import static org.toradocu.util.ComplianceChecks.isPostSpecCompilable;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.toradocu.conf.Configuration;
import org.toradocu.extractor.Comment;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.ReturnTag;
import org.toradocu.util.Reflection;
import randoop.condition.specification.Guard;
import randoop.condition.specification.PostSpecification;
import randoop.condition.specification.Property;

public class ReturnTranslator {

  public List<PostSpecification> translate(ReturnTag tag, DocumentedExecutable excMember) {
    String commentText = tag.getComment().getText();
    // Manage translation of each sub-sentence linked by the Or conjunction separately
    String[] subSentences = manageOrConjunction(commentText);
    List<List<PostSpecification>> conditions = new ArrayList<>();

    for (String subSentence : subSentences) {
      // Split the sentence in three parts: predicate + true case + false case.
      // TODO Naive splitting. Make the split more reliable.
      final int predicateSplitPoint = subSentence.indexOf(" if ");
      if (predicateSplitPoint != -1) {
        conditions.add(
            returnStandardPattern(excMember, subSentence, tag.getComment(), predicateSplitPoint));
      } else {
        conditions.add(returnNotStandard(excMember, subSentence));
      }
    }

    return mergeOrConjunction(commentText, subSentences, conditions);
  }

  /**
   * Extracts sub-sentences that in the comment are linked by an Or conjunction.
   *
   * @param comment original comment text
   * @return the sub-sentences
   */
  private String[] manageOrConjunction(String comment) {
    // Ignore pattern expressions involving an "or"
    String placeholderText =
        comment
            .replaceAll("greater than or equal to", ">=")
            .replaceAll("less than or equal to", "<=")
            .replaceAll("lesser than or equal to", "<=")
            .replaceAll("lesser or equal to", "<=")
            .replaceAll("smaller than or equal to", "<=")
            .replaceAll(" logical or ", " logicalOR ");

    String subSentences[] = placeholderText.split(" or ");
    if (subSentences.length > 1) {
      // To help translation of the second sub-sentence, if any
      subSentences[1] = Configuration.RETURN_VALUE + " is " + subSentences[1];
    }
    return subSentences;
  }

  /**
   * We have translated the sub-sentences separately, now we have to join them with the conjunction.
   *
   * @param comment original comment text
   * @param subSentences the sub-sentences composing the comment
   * @param conditions the translated conditions
   * @return a {@code List<PostSpecification>}
   */
  private List<PostSpecification> mergeOrConjunction(
      String comment, String[] subSentences, List<List<PostSpecification>> conditions) {
    if (conditions.size() > 1) {
      if (!conditions.get(0).isEmpty() && !conditions.get(1).isEmpty()) {
        // Both the conditions were correctly translated and can be merged
        String property =
            "("
                + (conditions.get(0).get(0).getProperty().getConditionText()
                    + "||"
                    + conditions.get(1).get(0).getProperty().getConditionText())
                + ")";
        List<PostSpecification> specs = new ArrayList<>();
        specs.add(
            new PostSpecification(
                comment,
                new Guard(comment, conditions.get(0).get(0).getGuard().getConditionText()),
                new Property(comment, property)));
        return specs;
      } else if (conditions.get(0).isEmpty()
          && !conditions.get(1).isEmpty()
          && subSentences[1].contains(" if ")) {
        // We couldn't translate the main condition. Either the second one contains a correctly
        // translated "if" condition, or we would produce a biased translation
        return conditions.get(1);
      } else if (!conditions.get(0).isEmpty()
          && conditions.get(1).isEmpty()
          && subSentences[1].contains(" if ")) {
        return new ArrayList<>();
      } else if (conditions.get(0).isEmpty() && conditions.get(1).isEmpty()) {
        // We couldn't translate any of the sub-sentences, thus we return empty
        return new ArrayList<>();
      }
    }
    return conditions.get(0);
  }

  /**
   * Translate arithmetic and bit-wise operations involving arguments, if found.
   *
   * @param method the DocumentedExecutable
   * @param matcherOp matcher in which an operation was found
   * @return the translation if any, or an empty String
   */
  private static String manageArgsOperation(
      DocumentedExecutable method, java.util.regex.Matcher matcherOp) {
    String firstFactor = matcherOp.group(1);
    String secFactor = matcherOp.group(3);
    String op = matcherOp.group(2);

    CodeElement<?> first = null;
    Set<CodeElement<?>> subject = new Matcher().subjectMatch(firstFactor, method);
    if (!subject.isEmpty()) {
      first = subject.stream().findFirst().get();
    }
    if (first != null) {
      CodeElement<?> second = null;
      subject = new Matcher().subjectMatch(secFactor, method);
      if (!subject.isEmpty()) {
        second = subject.stream().findFirst().get();
      }
      if (second != null) {
        Type returnType = method.getReturnType().getType();
        if (checkSameType(method, second)
            && checkSameType(method, first)
            && Reflection.isPrimitive(returnType)) {
          return Configuration.RETURN_VALUE
              + "=="
              + first.getJavaExpression()
              + op
              + second.getJavaExpression();
        }
      }
    }

    // if an operation was found, but between incompatible types, return empty translation
    return "";
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
  private static String translateLastPart(String text, DocumentedExecutable method) {
    final String lowerCaseText = text.toLowerCase();
    if (lowerCaseText.contains("true")) {
      return Configuration.RETURN_VALUE + " == true";
    } else if (lowerCaseText.contains("false")) {
      return Configuration.RETURN_VALUE + " == false";
    } else {
      // The result is not a plain boolean, so it must be a code element.
      String[] splittedText = text.split(" ");
      for (String token : splittedText) {
        if (!token.isEmpty()) {
          if (token.equals("\"\"")) { // The empty String was found
            return Configuration.RETURN_VALUE + ".equals(\"\")";
          }

          final List<PropositionSeries> extractedPropositions =
              Parser.parse(new Comment(Configuration.RETURN_VALUE + " " + token), method);
          final List<SemanticGraph> semanticGraphs =
              extractedPropositions
                  .stream()
                  .map(PropositionSeries::getSemanticGraph)
                  .collect(toList());

          String translation =
              tryPredicateMatch(
                  method,
                  semanticGraphs,
                  extractedPropositions,
                  Configuration.RETURN_VALUE + " " + token);
          if (translation == null) {
            translation = tryCodeElementMatch(method, token);
          }
          if (translation != null) {
            if (translation.contains("{") && translation.contains("}")) {
              String argument =
                  translation.substring(translation.indexOf("{") + 1, translation.indexOf("}"));

              Set<CodeElement<?>> argMatches;
              argMatches = new Matcher().subjectMatch(argument, method);
              if (argMatches.isEmpty()) {
                //            ConditionTranslator.log.trace("Failed predicate translation for: " + p
                // + " due to variable not found.");
                return null;
              } else {
                Iterator<CodeElement<?>> it = argMatches.iterator();
                String replaceTarget = "{" + argument + "}";
                // Naive solution: picks the first match from the list.
                String replacement = it.next().getJavaExpression();
                translation = translation.replace(replaceTarget, replacement);
              }
            }
            return translation;
          }
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
   * @param comment the comment text
   * @return the translation of the given {@code text}
   */
  private static String translateSecondPart(
      String trueCase, DocumentedExecutable method, Comment comment) {
    // Identify propositions in the comment. Each sentence in the comment is parsed into a
    // PropositionSeries.

    // text = removeInitial(text, "if");  already done in Preprocess part
    List<PropositionSeries> extractedPropositions =
        Parser.parse(new Comment(trueCase, comment.getWordsMarkedAsCode()), method);
    Set<String> conditions = new LinkedHashSet<>();
    for (PropositionSeries propositions : extractedPropositions) {
      BasicTranslator.translate(propositions, method, comment.getText());
      conditions.add(propositions.getTranslation());
    }
    return BasicTranslator.mergeConditions(conditions);
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
   * @param method the DocumentedExecutable the @return comment belongs to
   * @return the translation of the given {@code text}
   * @throws IllegalArgumentException if the given {@code text} cannot be translated
   */
  private static String translateFirstPart(String predicate, DocumentedExecutable method) {
    String parsedComment = predicate.trim().toLowerCase().replace(",", "");
    String translation;
    switch (parsedComment) {
      case "true":
      case "false":
        return Configuration.RETURN_VALUE + " == " + parsedComment;
      default:
        {
          // No return of type boolean: it must be a more complex boolean condition, or a code
          // element.
          final List<PropositionSeries> extractedPropositions =
              Parser.parse(new Comment(parsedComment), method);
          final List<SemanticGraph> semanticGraphs =
              extractedPropositions
                  .stream()
                  .map(PropositionSeries::getSemanticGraph)
                  .collect(toList());

          translation =
              tryPredicateMatch(method, semanticGraphs, extractedPropositions, parsedComment);
          if (translation == null) {
            translation = tryCodeElementMatch(method, parsedComment);
          } else {
            if (translation.contains("{") && translation.contains("}")) {
              translation = extractVariablesFound(translation, method);
            }
            return translation;
          }
        }
    }
    // TODO: Change the exception with one more meaningful.
    //    throw new IllegalArgumentException(text + " cannot be translated: Pattern not supported");
    return translation;
  }

  /**
   * This method attempts to translate the return tag according to the classical pattern. Thus, it
   * will follow the call chain made by: translateFirstPart|translateSecondPart|translateLastPart.
   * This "standard pattern" applies only when the return tag comment contains an "if".
   *
   * @param method the DocumentedExecutable
   * @param textToTranslate the String text to translate
   * @param comment original {@code Comment}
   * @param predicateSplitPoint index of the "if"
   * @return the translation produced
   */
  private static List<PostSpecification> returnStandardPattern(
      DocumentedExecutable method,
      String textToTranslate,
      Comment comment,
      int predicateSplitPoint) {
    List<PostSpecification> specs = new ArrayList<>();

    if (textToTranslate.contains(";")) {
      textToTranslate = textToTranslate.replace(";", ",");
    }
    String predicate = textToTranslate.substring(0, predicateSplitPoint);
    final String[] tokens = textToTranslate.substring(predicateSplitPoint + 3).split(",", 2);
    String trueCase = tokens[0];
    String falseCase = tokens.length > 1 ? tokens[1] : "";

    if (falseCase.equals("")) {
      // The comment doesn't express a false-case. Can we complete the condition?
      if (predicate.equalsIgnoreCase("true")) {
        // Covers cases of comment that say "True if..." without explicit "false otherwise"
        falseCase = "false otherwise";
      } else if (trueCase.equalsIgnoreCase("false")) {
        // Covers the symmetric case
        falseCase = "true otherwise";
      }
    }

    if (!predicate.isEmpty() && !trueCase.isEmpty()) {
      String predicateTranslation = translateFirstPart(predicate, method);
      if (predicateTranslation != null) {
        String conditionTranslation = translateSecondPart(trueCase, method, comment);

        if (!conditionTranslation.isEmpty() && !predicateTranslation.isEmpty()) {
          Guard trueGuard = new Guard(textToTranslate, conditionTranslation);
          Property trueProperty = new Property(textToTranslate, predicateTranslation);
          if (isPostSpecCompilable(method, trueGuard, trueProperty)) {
            specs.add(new PostSpecification(textToTranslate, trueGuard, trueProperty));
          }
          String elsePredicate = translateLastPart(falseCase, method);
          if (elsePredicate != null) {
            String invertedGuard = "(" + conditionTranslation + ")==false";
            Guard falseGuard = new Guard(textToTranslate, invertedGuard);
            Property falseProperty = new Property(textToTranslate, elsePredicate);
            if (isPostSpecCompilable(method, falseGuard, falseProperty)) {
              specs.add(new PostSpecification(textToTranslate, falseGuard, falseProperty));
            }
          }
        }
      }
    }

    return specs;
  }

  /**
   * Return "not standard" pattern means that the comment referring to the return tag does not
   * contain an "if". Thus, it will not follow the call chain made by:
   * translateFirstPart|translateSecondPart|translateLastPart. In this pattern, the translation will
   * always be something like: "true ? [condition over result]". result in the condition could be
   * compared to: a plain true/false value; a more complex boolean expression, meaning that we must
   * check some property of the result; a code element.
   *
   * @param method the DocumentedExecutable the tag belongs to
   * @param comment the String comment belonging to the tag
   * @return a String translation if any, or an empty string
   */
  private static List<PostSpecification> returnNotStandard(
      DocumentedExecutable method, String comment) {
    List<PostSpecification> specs = new ArrayList<>();

    String translation = null;
    final String[] truePatterns = {"true", "true always", "true, always", "always true"};
    final String[] falsePatterns = {"false", "false always", "false, always", "always false"};
    final String commentToTranslate = comment;

    final boolean truePatternsMatch =
        Arrays.stream(truePatterns)
            .map(p -> p.concat("."))
            .anyMatch(p -> p.equalsIgnoreCase(commentToTranslate));
    final boolean falsePatternsMatch =
        Arrays.stream(falsePatterns)
            .map(p -> p.concat("."))
            .anyMatch(p -> p.equalsIgnoreCase(commentToTranslate));

    Guard guard = new Guard(comment, "true");
    Property property = null;
    if (truePatternsMatch) {
      property = new Property(comment, Configuration.RETURN_VALUE + "==true");
    } else if (falsePatternsMatch) {
      property = new Property(comment, Configuration.RETURN_VALUE + "==false");
    } else {

      final String ARITHMETIC_OP_REGEX = "([a-zA-Z0-9_]+) ?([-+*/%]) ?([a-zA-Z0-9_]+)";

      final String BITWISE_OP_REGEX = "([a-zA-Z0-9_]+) ?(<<<?|>>>?) ?([a-zA-Z0-9_]+)";

      final String BINARY_OP_REGEX = "([a-zA-Z0-9_]+) ?(\\^|&|\\|) ?([a-zA-Z0-9_]+)";

      java.util.regex.Matcher matcherArithmeticOp =
          Pattern.compile(ARITHMETIC_OP_REGEX).matcher(commentToTranslate);

      java.util.regex.Matcher matcherBitOp =
          Pattern.compile(BITWISE_OP_REGEX).matcher(commentToTranslate);

      java.util.regex.Matcher matcherBinOp =
          Pattern.compile(BINARY_OP_REGEX).matcher(commentToTranslate);

      if (matcherArithmeticOp.find()) {
        translation = manageArgsOperation(method, matcherArithmeticOp);
      } else if (matcherBitOp.find()) {
        translation = manageArgsOperation(method, matcherBitOp);
      } else if (matcherBinOp.find()) {
        translation = manageArgsOperation(method, matcherBinOp);
        if (!translation.isEmpty()) {
          String[] isolateBinaryOp = translation.split("(?<===)");
          translation = isolateBinaryOp[0] + "(" + isolateBinaryOp[1] + ")";
        }
      }
      if (translation == null) {
        final List<PropositionSeries> extractedPropositions =
            Parser.parse(new Comment(comment), method);
        final List<SemanticGraph> semanticGraphs =
            extractedPropositions
                .stream()
                .map(PropositionSeries::getSemanticGraph)
                .collect(toList());

        translation = tryPredicateMatch(method, semanticGraphs, extractedPropositions, comment);
        if (translation == null) {
          translation = tryCodeElementMatch(method, comment);
        }
      }

      if (translation != null && !translation.isEmpty()) {
        if (translation.contains("{") && translation.contains("}")) {
          translation = extractVariablesFound(translation, method);
        }
        if (translation != null) {
          property = new Property(comment, translation);
        }
      }
    }
    if (property != null && isPostSpecCompilable(method, guard, property)) {
      specs.add(new PostSpecification(comment, guard, property));
    }
    return specs;
  }

  /**
   * If the regex found a variable, it will be wrapped between curly brackets. This method extract
   * the corresponding expression for the variable and substitute the brackets and their content.
   *
   * @param translation original translation containing curly brackets
   * @param method the method to which the condition is referred
   * @return the translation with the right substitution if any, null otherwise
   */
  private static String extractVariablesFound(String translation, DocumentedExecutable method) {
    String argument = translation.substring(translation.indexOf("{") + 1, translation.indexOf("}"));

    Set<CodeElement<?>> argMatches;
    argMatches = new Matcher().subjectMatch(argument, method);
    if (argMatches.isEmpty()) {
      //            ConditionTranslator.log.trace("Failed predicate translation for: " + p + " due
      // to variable not found.");
      return null;
    } else {
      Iterator<CodeElement<?>> it = argMatches.iterator();
      String replaceTarget = "{" + argument + "}";
      // Naive solution: picks the first match from the list.
      CodeElement<?> codeElement = it.next();
      String replacement = codeElement.getJavaExpression();

      String type = "";
      if (codeElement instanceof ClassCodeElement) {
        type = ((ClassCodeElement) codeElement).getJavaCodeElement().getName();
      }

      translation = translation.replace(replaceTarget, replacement);
      return translation;
    }
  }

  /**
   * Called to search for a complex boolean condition involved in the return tag. Example: "return
   * x, must not be null" produces (result==null) == false .
   *
   * @param method the DocumentedExecutable the tag belongs to
   * @param semanticGraphs list of {@code SemanticGraph} related to the comment
   * @param extractedPropositions list of {@code PropositionSeries} extracted from the comment
   * @param comment the comment text
   * @return a String predicate match if any, or null
   */
  private static String tryPredicateMatch(
      DocumentedExecutable method,
      List<SemanticGraph> semanticGraphs,
      List<PropositionSeries> extractedPropositions,
      String comment) {
    String predicateMatch = null;

    for (SemanticGraph sg : semanticGraphs) {
      List<IndexedWord> verbs = sg.getAllNodesByPartOfSpeechPattern("VB(.*)");
      if (!verbs.isEmpty()) {
        for (PropositionSeries prop : extractedPropositions) {
          for (Proposition p : prop.getPropositions()) {
            predicateMatch =
                new Matcher()
                    .predicateMatch(
                        method,
                        new GeneralCodeElement(Configuration.RETURN_VALUE, null),
                        p,
                        comment);
            if (predicateMatch != null) {
              break;
            }
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
   * @param method the {@code DocumentedExecutable} the comment belongs to
   * @param text the comment text
   * @return a String translation if any, null otherwise
   */
  private static String tryCodeElementMatch(DocumentedExecutable method, String text) {
    final List<PropositionSeries> extractedPropositions = Parser.parse(new Comment(text), method);
    final List<SemanticGraph> semanticGraphs =
        extractedPropositions.stream().map(PropositionSeries::getSemanticGraph).collect(toList());

    CodeElement<?> codeElementMatch = findCodeElement(method, semanticGraphs);
    if (codeElementMatch != null) {
      // check if return type of method is the same of the code element that matches
      boolean isSameType = checkSameType(method, codeElementMatch);

      if (isSameType) {
        if (Reflection.isPrimitive(method.getReturnType().getType())) {
          return Configuration.RETURN_VALUE + " == " + codeElementMatch.getJavaExpression();
        } else {
          return Configuration.RETURN_VALUE
              + ".equals("
              + codeElementMatch.getJavaExpression()
              + ")";
        }
      }
    }
    return null;
  }

  /**
   * Check if the method return type is the same type of code element.
   *
   * @param method method which return type must be checked
   * @param codeElement code element to compare
   * @return true if the types match, false otherwise
   */
  private static boolean checkSameType(DocumentedExecutable method, CodeElement<?> codeElement) {
    Type methodReturn = method.getReturnType().getType();

    if (methodReturn instanceof TypeVariable || methodReturn instanceof GenericArrayType) {
      // TODO naive but we have not better choice for now.
      return true;
    }

    if (methodReturn instanceof ParameterizedType) {
      final Class<?> methodReturnRawType =
          (Class<?>) ((ParameterizedType) methodReturn).getRawType();

      if (codeElement instanceof FieldCodeElement) {
        return ((FieldCodeElement) codeElement)
            .getJavaCodeElement()
            .getType()
            .isAssignableFrom(methodReturnRawType);
      }
      if (codeElement instanceof ParameterCodeElement) {
        return ((ParameterCodeElement) codeElement)
            .getJavaCodeElement()
            .getType()
            .isAssignableFrom(methodReturnRawType);
      }
    } else {
      if (codeElement instanceof FieldCodeElement) {
        return ((FieldCodeElement) codeElement)
            .getJavaCodeElement()
            .getType()
            .isAssignableFrom((Class<?>) methodReturn);
      }
      if (codeElement instanceof ParameterCodeElement) {
        return ((ParameterCodeElement) codeElement)
            .getJavaCodeElement()
            .getType()
            .isAssignableFrom((Class<?>) methodReturn);
      }
    }
    return false;
  }

  /**
   * Called by tryCodeElementMatch to search for a code element involved in the return tag.
   *
   * @param method the DocumentedExecutable the tag belongs to
   * @param semanticGraphs list of {@code SemanticGraph} related to the comment
   * @return a code element if any, or null
   */
  private static CodeElement<?> findCodeElement(
      DocumentedExecutable method, List<SemanticGraph> semanticGraphs) {
    // Try a match looking at the semantic graph.
    CodeElement<?> codeElementMatch = null;

    for (SemanticGraph sg : semanticGraphs) {
      if (!sg.getAllNodesByPartOfSpeechPattern("IN").isEmpty()
          || !sg.getAllNodesByPartOfSpeechPattern("WDT").isEmpty()) {
        // there are relations such as "of...", "in...", "that/which..." usually sign of bias in our
        // assumption
        return null;
      }
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
}
