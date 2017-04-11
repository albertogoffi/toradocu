package org.toradocu.translator;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import java.util.List;

public class CustomStanfordParser {

  private static final LexicalizedParser LEXICALIZED_PARSER;
  private static final GrammaticalStructureFactory GSF;

  static {
    LEXICALIZED_PARSER = LexicalizedParser.loadModel();
    TreebankLanguagePack tlp =
        LEXICALIZED_PARSER.treebankLanguagePack(); // a PennTreebankLanguagePack for English
    GSF =
        tlp.supportsGrammaticalStructures()
            ? tlp.grammaticalStructureFactory()
            : null; //FIXME This leads to a NullPointerException. Better to throw a meaningful exception here!
  }

  public static SemanticGraph getSemanticGraph(List<HasWord> sentence, List<HasWord> codeElements) {
    Tree tree =
        LEXICALIZED_PARSER.parse(
            new POSTagger().tagWords(sentence, codeElements)); // parse the sentence
    GrammaticalStructure gs = GSF.newGrammaticalStructure(tree);
    return new SemanticGraph(gs.typedDependenciesCCprocessed()); // build the dependency graph
  }
}
