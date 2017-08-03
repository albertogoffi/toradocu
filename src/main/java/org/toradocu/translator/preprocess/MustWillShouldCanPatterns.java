package org.toradocu.translator.preprocess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.Comment;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.ParamTag;
import org.toradocu.translator.Parser;
import org.toradocu.translator.PropositionSeries;

import static java.util.stream.Collectors.toList;

public class MustWillShouldCanPatterns implements PreprocessingPhase {

  @Override
  public String run(BlockTag tag, DocumentedExecutable excMember) {
    String comment = tag.getComment().getText();
    String parameterName = ((ParamTag) tag).getParameter().getName();
    String[] patterns = {
            "must be",
            "must not be",
            "must never",
            "will be",
            "will not be",
            "will never be",
            "can't be",
            "cannot be",
            "should be",
            "should not be",
            "shouldn't be",
            "may not be",
            "Must be",
            "Must not be",
            "must'nt be",
            "Will be",
            "Will not be",
            "Can't be",
            "Cannot be",
            "Should be",
            "Should not be",
            "Shouldn't be",
            "May not be"
    };
    java.util.regex.Matcher matcher = Pattern.compile("\\(.*").matcher(comment);
    String separator = matcher.find() ? " " : ".";
    boolean noReplacedYet = true; //Tells if there was already a replacement in the phrase
    for (String pattern : patterns) {
      if (comment.contains(pattern)) {
        String replacement = separator + parameterName + " " + pattern;
        comment = comment.replace(pattern, replacement);
        noReplacedYet = false;
      }
    }


    //TODO make this a preprocessing phase
    comment = comment.replace(";", ",");
    String[] beginnings = {"the", "a", "any", " the", " a", " any"};
    String commaPattern = ".*(, ?(?!may be)(?!can be)(?!could be)(?!possibly))(.*)";
    java.util.regex.Matcher commaMatcher =
            Pattern.compile(commaPattern).matcher(comment);

    if(noReplacedYet && commaMatcher.find()) {
      //last group must not contain VERBS, and must contain an ADJ
      //e.g. "not null", "not empty"
      final List<PropositionSeries> extractedPropositions =
              Parser.parse(new Comment(commaMatcher.group(2)), excMember);
      final List<SemanticGraph> semanticGraphs =
              extractedPropositions
                      .stream()
                      .map(PropositionSeries::getSemanticGraph)
                      .collect(toList());
      boolean goOn = false;
      for (SemanticGraph sg : semanticGraphs) {
        if (sg.getAllNodesByPartOfSpeechPattern("VB(.*)").isEmpty() &&
                !sg.getAllNodesByPartOfSpeechPattern("JJ(.*)").isEmpty())
          goOn = true;
      }

      if (goOn) {
        int lastComma = comment.lastIndexOf(",");
        String tokens[] = {comment.substring(0, lastComma),
                comment.substring(lastComma + 1, comment.length())};

        for (String begin : beginnings) {
          if (tokens[1].startsWith(begin))
            tokens[1] = tokens[1].replaceFirst(begin, "");
        }


        comment = tokens[0] + ". " + parameterName + " is " + tokens[1];
        noReplacedYet = false;
      }
    }

    if (noReplacedYet) {
      String[] tokens = comment.split(" ");
      if (Arrays.asList(beginnings).contains(tokens[0])) {
        final List<PropositionSeries> extractedPropositions =
                Parser.parse(new Comment(comment), excMember);
        final List<SemanticGraph> semanticGraphs =
                extractedPropositions
                        .stream()
                        .map(PropositionSeries::getSemanticGraph)
                        .collect(toList());
        boolean goOn = false;
        for (SemanticGraph sg : semanticGraphs) {
          if (sg.getAllNodesByPartOfSpeechPattern("VB(.*)").isEmpty()){
            List<IndexedWord> adjs = sg.getAllNodesByPartOfSpeechPattern("JJ(.*)");
            for(IndexedWord adj : adjs)
              if(adj.word().equals(tokens[1]))
                goOn = true;
          }
        }
        // At this point, no verb was found. There remains
        // only one last pattern to match.
        comment = comment.replace(tokens[0], "");
        comment = comment.replace(tokens[1], "");
        comment = parameterName + " is " + tokens[1] + ". " + comment;
      }
    }

    return comment;
  }
}
