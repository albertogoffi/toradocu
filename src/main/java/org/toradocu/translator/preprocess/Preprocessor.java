package org.toradocu.translator.preprocess;

import java.util.List;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Tag;

public class Preprocessor {

  private List<PreprocessingPhase> phases;

  public Preprocessor(List<PreprocessingPhase> phases) {
    this.phases = phases;
  }

  void addPhase(PreprocessingPhase phase) {
    phases.add(phase);
  }

  public Tag preprocess(Tag tag, DocumentedMethod excMember) {
    for (PreprocessingPhase phase : phases) {
      tag.setComment(phase.run(tag, excMember));
    }
    return tag;
  }
}
