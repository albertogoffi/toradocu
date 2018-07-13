package org.toradocu.translator.preprocess;

import static java.util.stream.Collectors.toList;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.Comment;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.ParamTag;
import org.toradocu.translator.Parser;
import org.toradocu.translator.PropositionSeries;

public class ImplicitParamSubjectPatterns implements PreprocessingPhase {

  @Override
  public String run(BlockTag tag, DocumentedExecutable excMember) {
    String originalComment = tag.getComment().getText();
    String parameterName = ((ParamTag) tag).getParameter().getName();
    String[] positivePatterns = {"must be", "Must be", "will be", "Will be"};

    String[] negativePatterns = {
      "must not be",
      "Must not be",
      "must not return",
      "Must not return",
      "must never be",
      "Must never be",
      "must never return",
      "Must never return",
      "will not be",
      "Will not be",
      "will never be",
      "Will never be",
      "can't be",
      "Can't be",
      "cannot be",
      "Cannot be",
      "should not be",
      "Should not be",
      "shouldn't be",
      "Shouldn't be",
      "may not be",
      "May not be",
      "must'nt be",
      "Must'nt be"
    };

    String preProcessedComment =
        replacePatterns(originalComment, positivePatterns, parameterName, "");
    preProcessedComment =
        replacePatterns(preProcessedComment, negativePatterns, parameterName, "not");

    if (originalComment.equals(preProcessedComment)) {
      final List<PropositionSeries> extractedPropositions =
          Parser.parse(new Comment(originalComment), excMember);
      final List<SemanticGraph> semanticGraphs =
          extractedPropositions.stream().map(PropositionSeries::getSemanticGraph).collect(toList());

      preProcessedComment = originalComment.replace(";", ",");
      String[] beginnings = {"the", "a", "an", "any"};

      // param comment that contains a comma followed by a description (ignore non-mandatory
      // conditions)
      String commaPattern = ".*(, (?!default)(?!may be)(?!can be)(?!could be)(?!possibly))(.*)";
      Matcher commaMatcher = Pattern.compile(commaPattern).matcher(preProcessedComment);

      if (commaMatcher.find() && adjectivesFound(excMember, commaMatcher)) {
        return replaceCommaPattern(preProcessedComment, parameterName, beginnings);
      }

      // Manage param comment starting with an adjective
      preProcessedComment =
          manageFirstAdj(excMember, preProcessedComment, parameterName, beginnings);
    }
    return preProcessedComment;
  }

  /**
   * Manages param comments such as "x - non-null vector"
   *
   * @param excMember the executable member the comment belongs to
   * @param comment the comment text
   * @param parameterName the parameter name the comment refers to
   * @param beginnings possible comment beginnings
   * @return the comment text correctly replaced
   */
  private String manageFirstAdj(
      DocumentedExecutable excMember, String comment, String parameterName, String[] beginnings) {
    // TODO \\s?
    String[] tokens = comment.split(" ");
    boolean hasArticle = (Arrays.asList(beginnings).contains(tokens[0]));
    String mayBeAdj = hasArticle ? tokens[1] : tokens[0];

    final List<PropositionSeries> extractedPropositions =
        Parser.parse(new Comment(comment), excMember);
    final List<SemanticGraph> semanticGraphs =
        extractedPropositions.stream().map(PropositionSeries::getSemanticGraph).collect(toList());
    StringBuilder commentBuilder = new StringBuilder(comment);
    for (SemanticGraph sg : semanticGraphs) {
      List<IndexedWord> adjs = sg.getAllNodesByPartOfSpeechPattern("JJ(.*)");
      for (IndexedWord adj : adjs) {
        if (adj.word().equals(mayBeAdj)) {
          if (hasArticle)
            // delete article
            commentBuilder =
                new StringBuilder(commentBuilder.toString().replaceFirst(tokens[0], ""));

          String firstPart = "{@code " + parameterName + "} is " + mayBeAdj + ". ";
          commentBuilder.insert(0, firstPart);
          break;
        }
      }
    }
    comment = commentBuilder.toString();
    return comment;
  }

  /**
   * Manges param comments such as "x - a vector, non-null"
   *
   * @param comment comment text
   * @param parameterName the name of the parameter the comment refers to
   * @param beginnings possible comment beginnings
   * @return comment text with the comma pattern correctly replaced
   */
  private String replaceCommaPattern(String comment, String parameterName, String[] beginnings) {
    int lastComma = comment.lastIndexOf(",");
    String tokens[] = {
      comment.substring(0, lastComma), comment.substring(lastComma + 1, comment.length())
    };
    for (String begin : beginnings) {
      if (tokens[1].startsWith(begin + " ")) {
        tokens[1] = tokens[1].replaceFirst(begin + " ", "");
        break;
      }
    }
    comment = tokens[0] + ". {@code " + parameterName + "} is " + tokens[1];

    return comment;
  }

  /**
   * Tells whether there are adjectives in the portion of comment text which matched the comma
   * pattern (i.e. after the comma in the comment)
   *
   * @param excMember the {@code DocumentedExecutable} to which the comment belongs
   * @param commaMatcher comment text
   * @return true if adjectives were found, false otherwise
   */
  private boolean adjectivesFound(DocumentedExecutable excMember, Matcher commaMatcher) {
    final List<PropositionSeries> extractedPropositions =
        Parser.parse(new Comment(commaMatcher.group(2)), excMember);
    final List<SemanticGraph> semanticGraphs =
        extractedPropositions.stream().map(PropositionSeries::getSemanticGraph).collect(toList());

    for (SemanticGraph sg : semanticGraphs) {
      if (!sg.getAllNodesByPartOfSpeechPattern("JJ(.*)").isEmpty()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Replace the given patterns in the comment
   *
   * @param comment comment text
   * @param patterns the patterns, could be positive or negative
   * @param parameterName the parameter name necessary in the replacement
   * @param negation negation for the replacement or empty if patterns are positive
   * @return the comment with patterns correctly replaced
   */
  private String replacePatterns(
      String comment, String[] patterns, String parameterName, String negation) {
    for (String pattern : patterns) {
      if (comment.contains(pattern)) {
        comment =
            comment.replaceAll(
                "(, )?( It )?" + pattern, ". {@code " + parameterName + "} " + " is " + negation);
      }
    }
    return comment;
  }
}
