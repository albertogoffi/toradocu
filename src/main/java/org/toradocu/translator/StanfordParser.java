package org.toradocu.translator;

import static java.util.stream.Collectors.toList;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.extractor.ExecutableMember;
import org.toradocu.extractor.Parameter;

/**
 * This class provides a method to get the semantic graph of a sentence produced by the Stanford
 * parser. To optimize execution time, the Stanford parser is initialized once in the static block
 * to ensure that its initialization phase is done only once.
 */
class StanfordParser {

  private static final LexicalizedParser LEXICALIZED_PARSER;
  private static final GrammaticalStructureFactory GSF;
  private static final Logger log = LoggerFactory.getLogger(StanfordParser.class);

  static {
    LEXICALIZED_PARSER = LexicalizedParser.loadModel();
    // tlp is the PennTreebankLanguagePack for English.
    TreebankLanguagePack tlp = LEXICALIZED_PARSER.treebankLanguagePack();
    if (!tlp.supportsGrammaticalStructures()) {
      throw new RuntimeException(
          "Error in the Stanford Parser configuration. Are models available?");
    }
    GSF = tlp.grammaticalStructureFactory();
  }

  /**
   * Takes some text and returns {@code SemanticGraph}s for each sentence in the text.
   *
   * @param text the text to return the semantic graphs for
   * @return a list of semantic graphs, one for each sentence in the text
   */
  static List<SemanticGraph> getSemanticGraphs(String text) {
    return getSemanticGraphs(text, null);
  }

  /**
   * Before asking for the SemanticGraph to the parser, manually tag code elements as NN and
   * inequalities placeholders as JJ.
   *
   * @param comment the String comment of the condition
   * @param method the ExecutableMember under analysis
   * @return the list of SemanticGraphs produced by the parser
   */
  static List<SemanticGraph> getSemanticGraphs(String comment, ExecutableMember method) {
    Iterable<List<HasWord>> hasWordComment = new DocumentPreprocessor(new StringReader(comment));

    ArrayList<List<HasWord>> sentences = new ArrayList<>();
    hasWordComment.forEach(sentences::add);
    List<SemanticGraph> result = new ArrayList<>();
    List<HasWord> codeElements = new ArrayList<>();
    List<String> arguments = new ArrayList<>();

    if (method != null) {
      arguments = method.getParameters().stream().map(Parameter::getName).collect(toList());
    }

    for (List<HasWord> sentence : sentences) {
      for (HasWord word : sentence) {
        if (arguments.contains(word.toString())) {
          codeElements.add(word);
        }
      }

      result.add(getSemanticGraph(sentence, codeElements));
    }
    return result;
  }

  private static SemanticGraph getSemanticGraph(
      List<HasWord> sentence, List<HasWord> codeElements) {
    // Parse the sentence.
    Tree tree = LEXICALIZED_PARSER.parse(new POSTagger().tagWords(sentence, codeElements));
    GrammaticalStructure gs = GSF.newGrammaticalStructure(tree);
    // Build the semantic graph.
    SemanticGraph semanticGraph = new SemanticGraph(gs.typedDependenciesCCprocessed());

    //    if (Toradocu.configuration != null && Toradocu.configuration.debug()) {
    log.debug("Input sentence: " + sentence + "\nSemantic Graph:\n" + semanticGraph);
    //    }

    return semanticGraph;
  }
}
