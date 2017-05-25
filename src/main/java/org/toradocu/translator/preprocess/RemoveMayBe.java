package org.toradocu.translator.preprocess;

import org.toradocu.extractor.ExecutableMember;
import org.toradocu.extractor.Tag;

public class RemoveMayBe implements PreprocessingPhase {

  @Override
  public String run(Tag tag, ExecutableMember excMember) {
    String comment = tag.getComment().getText();

    if (comment.contains("may be")) {
      comment = comment.replaceAll("may be", "");
    }
    return comment;
  }
}
