package org.toradocu.translator.preprocess;

import static java.util.stream.Collectors.toList;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import java.util.List;
import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.Comment;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.translator.Parser;
import org.toradocu.translator.PropositionSeries;

/** Created by arianna on 29/06/17. */
public class NormalizeIt implements PreprocessingPhase {

  private static String normalizeComment(String comment, DocumentedExecutable method) {
    // "it" would be translated as a standalone subject, but more probably it is referred to another
    // more meaningful one:
    // probably a previous mentioned noun.
    if (comment.contains(" it ")) {
      final List<PropositionSeries> extractedPropositions =
          Parser.parse(new Comment(comment), method);
      final List<SemanticGraph> semanticGraphs =
          extractedPropositions.stream().map(PropositionSeries::getSemanticGraph).collect(toList());
      for (SemanticGraph sg : semanticGraphs) {
        List<IndexedWord> nouns = sg.getAllNodesByPartOfSpeechPattern("NN(.*)");
        if (!nouns.isEmpty()) {
          IndexedWord boh = nouns.stream().findFirst().get();
          comment = comment.replace(" it ", " " + boh.word() + " ");
        }
      }
    }

    return comment;
  }

  @Override
  public String run(BlockTag tag, DocumentedExecutable excMember) {
    return normalizeComment(tag.getComment().getText(), excMember);
  }
}
