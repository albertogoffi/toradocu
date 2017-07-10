package org.toradocu.translator;

import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.Tag;
import org.toradocu.translator.spec.Specification;

public interface Translator<T extends Tag> {
  Specification translate(T tag, DocumentedExecutable excMember);
}
