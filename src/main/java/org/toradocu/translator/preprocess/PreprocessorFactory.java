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
        phases.add(new ImplicitParamSubjectPatterns());
        phases.add(new ExpandRange());
        phases.add(new RemoveCommas()); // TODO Make RemoveCommas a singleton?
        phases.add(new RemoveMayBe());
        phases.add(new NormalizeNonNullNonEmpty());
        phases.add(new NormalizeIt());
        phases.add(new Trim());
        break;
      case THROWS:
        phases.add(new ExpandRange());
        phases.add(new RemoveCommas());
        phases.add(new NormalizeIfs());
        phases.add(new RemoveInitialIf());
        phases.add(new NormalizeNonNullNonEmpty());
        phases.add(new NormalizeIt());
        phases.add(new Trim());
        break;
      case RETURN:
        phases.add(new ExpandRange());
        phases.add(new NormalizeIfs());
        phases.add(new NormalizeNonNullNonEmpty());
        phases.add(new NormalizeIt());
        phases.add(new NormalizeWhether());
        phases.add(new Trim());
        break;
    }

    return new Preprocessor(phases);
  }
}
