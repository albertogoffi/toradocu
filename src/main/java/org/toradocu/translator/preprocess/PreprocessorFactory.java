package org.toradocu.translator.preprocess;

import java.util.ArrayList;
import java.util.List;
import org.toradocu.extractor.Tag;

public class PreprocessorFactory {

  private PreprocessorFactory() {}

  public static Preprocessor create(Tag.Kind tagKind) {
    List<PreprocessingPhase> phases = new ArrayList<>();

    // TODO Complete the method adding missing phases.
    switch (tagKind) {
      case PARAM:
        phases.add(new RemoveCommas()); // TODO Make RemoveCommas a singleton?
    }
    return new Preprocessor(phases);
  }
}
