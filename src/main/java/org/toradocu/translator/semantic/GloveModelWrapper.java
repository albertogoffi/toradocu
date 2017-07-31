package org.toradocu.translator.semantic;

import java.io.File;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;

/** Created by arianna on 31/07/17. */
public class GloveModelWrapper {

  private static GloveModelWrapper instance = null;

  private static WordVectors gloveTxtVectors = null;

  protected GloveModelWrapper() {
    // Exists only to defeat instantiation.
  }

  public static GloveModelWrapper getInstance() {
    if (instance == null) {
      instance = new GloveModelWrapper();
      gloveTxtVectors = setUpGloveTxtVectors();
    }
    return instance;
  }

  private static WordVectors setUpGloveTxtVectors() {

    File gloveTxt = new File("/home/arianna/Scaricati/glove-master/target/glove.6B.300d.txt");
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
