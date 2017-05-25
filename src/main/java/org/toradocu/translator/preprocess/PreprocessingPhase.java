package org.toradocu.translator.preprocess;

import org.toradocu.extractor.ExecutableMember;
import org.toradocu.extractor.Tag;

public interface PreprocessingPhase {
  String run(Tag tag, ExecutableMember excMember);
}
