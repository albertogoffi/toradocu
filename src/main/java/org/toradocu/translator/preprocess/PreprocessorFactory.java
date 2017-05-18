package org.toradocu.translator.preprocess;

import java.util.ArrayList;
import java.util.List;
import org.toradocu.extractor.Tag;

public class PreprocessorFactory {

  private PreprocessorFactory() {}

  public static Preprocessor create(Tag.Kind tagKind) {
    List<PreprocessingPhase> phases = new ArrayList<>();

    phases.add(new EndPeriod());
    phases.add(new Trim());

    // TODO Complete the method adding missing phases.
    switch (tagKind) {
      case PARAM:
        phases.add(new RemoveCommas()); // TODO Make RemoveCommas a singleton?
        phases.add(new RemoveMayBe());
        phases.add(new MustWillShouldCanPatterns());
        break;
      case THROWS:
        phases.add(new RemoveCommas());
        phases.add(new NormalizeIfs());
        phases.add(new RemoveInitialIf());
        break;
      case RETURN:
        phases.add(new NormalizeIfs());
        break;
    }

    return new Preprocessor(phases);
  }
}
