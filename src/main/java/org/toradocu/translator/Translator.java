package org.toradocu.translator;

import org.toradocu.extractor.ExecutableMember;
import org.toradocu.extractor.Tag;
import org.toradocu.translator.spec.Specification;

public interface Translator<T extends Tag> {
  Specification translate(T tag, ExecutableMember excMember);
}
