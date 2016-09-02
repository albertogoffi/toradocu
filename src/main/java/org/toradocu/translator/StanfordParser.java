package org.toradocu.translator;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * This class initializes the Stanford Parser and uses it to return the semantic graphs for
 * sentences.
 */
public class StanfordParser {

  private static final StanfordCoreNLP pipeline;

  static {
    /* Creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and
     * coreference resolution. */
    Properties props = new Properties();
    /* Complete annotations list was "tokenize, ssplit, pos, lemma, ner, parse, dcoref" */
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
    }

    return result;
  }
}
