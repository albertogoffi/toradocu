package org.toradocu.translator.semantic;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
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
      try {
        gloveTxtVectors = setUpGloveTxtVectors();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return instance;
  }

  private static WordVectors setUpGloveTxtVectors() throws Exception {
    String gloveTxtFolder = "glove-txt";
    String gloveTxtFile = "glove.6B.300d.txt";

    // Copy GloVe models in Toradocu jar to glove-txt folder and use them.
    String filePath = "/" + gloveTxtFile;
    InputStream gloveInputStream = GloveModelWrapper.class.getResourceAsStream(filePath);
    Path destinationFile = Paths.get(gloveTxtFolder, gloveTxtFile);

    Path folderPath = Paths.get(gloveTxtFolder);
    if (!Files.exists(folderPath)) {
      Files.createDirectory(folderPath);
    }
    if (Files.list(folderPath).count() == 0) {
      Files.copy(gloveInputStream, destinationFile);
    }
    WordVectors gloveVectors = null;
    try {
      File gloveFinalFile = destinationFile.toFile();
      gloveVectors = WordVectorSerializer.loadTxtVectors(gloveFinalFile);
      gloveFinalFile.deleteOnExit();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return gloveVectors;
  }

  public WordVectors getGloveTxtVectors() {
    return gloveTxtVectors;
  }
}
