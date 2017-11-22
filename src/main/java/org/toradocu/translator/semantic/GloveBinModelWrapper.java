package org.toradocu.translator.semantic;

import de.jungblut.glove.GloveRandomAccessReader;
import de.jungblut.glove.impl.GloveBinaryRandomAccessReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Created by arianna on 31/07/17. */
public class GloveBinModelWrapper {

  private static GloveBinModelWrapper instance = null;

  private static GloveRandomAccessReader gloveBinaryReader = null;

  protected GloveBinModelWrapper() {
    try {
      gloveBinaryReader = createGloVeReader();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static GloveBinModelWrapper getInstance() throws URISyntaxException {
    if (instance == null) {
      instance = new GloveBinModelWrapper();
    }
    return instance;
  }

  /**
   * Copy GloVe models from resources to a local folder and instantiate the reader.
   *
   * @return the reader
   */
  private GloveRandomAccessReader createGloVeReader() throws URISyntaxException, IOException {
    String gloveBinaries = "glove-binary";
    String file1 = "dict.bin";
    String file2 = "vectors.bin";

    // Copy GloVe models in Toradocu jar to glove-binary folder and use them.
    String file1Path = "/" + gloveBinaries + "/" + file1;
    String file2Path = "/" + gloveBinaries + "/" + file2;
    InputStream gloveBinary1 = getClass().getResourceAsStream(file1Path);
    InputStream gloveBinary2 = getClass().getResourceAsStream(file2Path);
    Path destinationFile1 = Paths.get(gloveBinaries, file1);
    Path destinationFile2 = Paths.get(gloveBinaries, file2);

    Path folderPath = Paths.get(gloveBinaries);
    if (!Files.exists(folderPath)) {
      Files.createDirectory(folderPath);
    }
    if (Files.list(folderPath).count() == 0) {
      Files.copy(gloveBinary1, destinationFile1);
      Files.copy(gloveBinary2, destinationFile2);
    }
    return new GloveBinaryRandomAccessReader(folderPath);
  }

  public GloveRandomAccessReader getGloveBinaryReader() {
    return gloveBinaryReader;
  }
}
