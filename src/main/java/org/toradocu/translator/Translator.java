package org.toradocu.translator;

import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Tag;

public interface Translator<T extends Tag> {
  Specification translate(T tag, DocumentedMethod excMember);
}
