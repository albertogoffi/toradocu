package org.toradocu.translator.preprocess;

import java.util.List;
import org.toradocu.extractor.Comment;
import org.toradocu.extractor.ExecutableMember;
import org.toradocu.extractor.Tag;

public class Preprocessor {

  private List<PreprocessingPhase> phases;

  Preprocessor(List<PreprocessingPhase> phases) {
    this.phases = phases;
  }

  public Tag preprocess(Tag tag, ExecutableMember excMember) {
    for (PreprocessingPhase phase : phases) {
      tag.setComment(new Comment(phase.run(tag, excMember)));
    }
    return tag;
  }
}
