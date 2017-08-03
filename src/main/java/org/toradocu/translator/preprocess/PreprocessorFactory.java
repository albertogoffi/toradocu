package org.toradocu.translator.preprocess;

import java.util.ArrayList;
import java.util.List;
import org.toradocu.extractor.BlockTag;

public class PreprocessorFactory {

  private PreprocessorFactory() {}

  public static Preprocessor create(BlockTag.Kind tagKind) {
    List<PreprocessingPhase> phases = new ArrayList<>();

    phases.add(new EndPeriod());
    phases.add(new Trim());

    switch (tagKind) {
      case PARAM:
        phases.add(new MustWillShouldCanPatterns());
        phases.add(new RemoveCommas()); // TODO Make RemoveCommas a singleton?
        phases.add(new RemoveMayBe());
        phases.add(new NormalizeNonNullNonEmpty());
        phases.add(new NormalizeIt());
        break;
      case THROWS:
        phases.add(new RemoveCommas());
        phases.add(new NormalizeIfs());
        phases.add(new RemoveInitialIf());
        phases.add(new NormalizeNonNullNonEmpty());
        phases.add(new NormalizeIt());
        break;
      case RETURN:
        phases.add(new NormalizeIfs());
        phases.add(new NormalizeNonNullNonEmpty());
        phases.add(new NormalizeIt());
        break;
    }

    return new Preprocessor(phases);
  }
}
