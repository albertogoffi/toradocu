package org.toradocu.translator;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.Toradocu;

/**
 * This class provides a method to get the semantic graph of a sentence produced by the Stanford
 * parser. To optimize execution time, the Stanford parser is initialized once in the static block
 * to ensure that its initialization phase is done only once.
 */
public class StanfordParser {

  private static final StanfordCoreNLP pipeline;
  private static final Logger log = LoggerFactory.getLogger(StanfordParser.class);

  static {
    // Creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and
    // coreference resolution.
    Properties props = new Properties();
    // Complete annotations list was "tokenize, ssplit, pos, lemma, ner, parse, dcoref".
    props.setProperty("annotators", "tokenize, ssplit, parse");
    pipeline = new StanfordCoreNLP(props);
  }

  /**
   * Takes some text and returns {@code SemanticGraph}s for each sentence in the text.
   *
   * @param text the text to return the semantic graphs for
   * @return a list of semantic graphs, one for each sentence in the text
   */
  public static List<SemanticGraph> getSemanticGraphs(String text) {
    List<SemanticGraph> result = new ArrayList<>();

    // Create an empty Annotation just with the given text.
    Annotation document = new Annotation(text);
    // Run all Annotators on this text.
    pipeline.annotate(document);
    // Retrieve sentences in text.
    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
    // Add semantic graph for each sentence to result.
    for (CoreMap sentence : sentences) {
      result.add(sentence.get(CollapsedCCProcessedDependenciesAnnotation.class));
      if (Toradocu.configuration != null && Toradocu.configuration.debug()) {
        log.debug(
            "Input sentence: "
                + sentence
                + "\n"
                + "Semantic Graph:\n"
                + result.get(result.size() - 1));
      }
    }

    return result;
  }
}
