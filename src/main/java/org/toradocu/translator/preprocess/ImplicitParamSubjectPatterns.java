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
    String comment = tag.getComment().getText();
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

    boolean noReplacedYet = true; //Tells if there was already a replacement in the phrase
    for (String pattern : positivePatterns) {
      String stringToReplace = pattern;
      if (comment.contains(". It " + pattern)) {
        stringToReplace = ". It " + pattern;
      }
      if (comment.contains(pattern)) {
        String replacement = ". {@code " + parameterName + "} " + " is ";
        comment = comment.replace(stringToReplace, replacement);
        noReplacedYet = false;
      }
    }
    for (String pattern : negativePatterns) {
      String stringToReplace = pattern;
      if (comment.contains(". It " + pattern)) {
        stringToReplace = ". It " + pattern;
      }
      if (comment.contains(pattern)) {
        String replacement = ". {@code " + parameterName + "} " + " is not ";
        comment = comment.replace(stringToReplace, replacement);
        noReplacedYet = false;
      }
    }

    //manage description following a comma
    if (noReplacedYet) {
      comment = comment.replace(";", ",");
      String[] beginnings = {"the", "a", "an", "any"};
      String commaPattern = ".*(, (?!default)(?!may be)(?!can be)(?!could be)(?!possibly))(.*)";
      //ignore "possible" values, i.e. not mandatory conditions
      Matcher commaMatcher = Pattern.compile(commaPattern).matcher(comment);

      if (commaMatcher.find()) {
        // covers cases as: "..., not null"
        if (findNextAdj(excMember, commaMatcher)) {
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
          noReplacedYet = false;
        }
      }

      //manage comment starting with an adjective
      if (noReplacedYet) {
        String[] tokens = comment.split(" ");
        boolean hasArticle = (Arrays.asList(beginnings).contains(tokens[0]));
        String mayBeAdj = "";
        mayBeAdj = hasArticle ? tokens[1] : tokens[0];

        //covers cases as: "the non-null..." or "non-null..."
        final List<PropositionSeries> extractedPropositions =
            Parser.parse(new Comment(comment), excMember);
        final List<SemanticGraph> semanticGraphs =
            extractedPropositions
                .stream()
                .map(PropositionSeries::getSemanticGraph)
                .collect(toList());
        for (SemanticGraph sg : semanticGraphs) {
          List<IndexedWord> adjs = sg.getAllNodesByPartOfSpeechPattern("JJ(.*)");
          for (IndexedWord adj : adjs) {
            if (adj.word().equals(mayBeAdj)) {
              if (hasArticle)
                //delete article
                comment = comment.replaceFirst(tokens[0], "");

              String firstPart = "{@code " + parameterName + "} is " + mayBeAdj + ". ";
              comment = firstPart + comment;
              break;
            }
          }
        }
      }
    }
    return comment;
  }

  /**
   * Search for adjectives in the {@code SemanticGraph} of the portion of the comment text (which
   * matched the comma pattern) that follows the comma
   *
   * @param excMember the {@code DocumentedExecutable} to which the comment belongs
   * @param commaMatcher comment text
   * @return true if adjectives were found, false otherwise
   */
  private boolean findNextAdj(DocumentedExecutable excMember, Matcher commaMatcher) {
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
}
