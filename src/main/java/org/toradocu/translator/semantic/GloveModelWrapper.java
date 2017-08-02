package org.toradocu.translator.semantic;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;

/** Created by arianna on 31/07/17. */
public class GloveModelWrapper {

  private static GloveModelWrapper instance = null;

  private static WordVectors gloveTxtVectors = null;

  protected GloveModelWrapper() {
    // Exists only to defeat instantiation.
  }

  public static GloveModelWrapper getInstance() throws URISyntaxException {
    if (instance == null) {
      instance = new GloveModelWrapper();
      gloveTxtVectors = setUpGloveTxtVectors();
    }
    return instance;
  }

  private static WordVectors setUpGloveTxtVectors() throws URISyntaxException {
    URL glovePath = ClassLoader.getSystemClassLoader().getResource("glove.6B.300d.txt");
    File gloveTxt = Paths.get(glovePath.toURI()).toFile();
    WordVectors gloveVectors = null;
    try {
      gloveVectors = WordVectorSerializer.loadTxtVectors(gloveTxt);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return gloveVectors;
  }

  public WordVectors getGloveTxtVectors() {
    return gloveTxtVectors;
  }
}
