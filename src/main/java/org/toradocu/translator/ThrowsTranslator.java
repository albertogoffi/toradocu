package org.toradocu.translator;

import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.ThrowsTag;
import org.toradocu.translator.spec.Specification;

public class ThrowsTranslator implements Translator<ThrowsTag> {

  @Override
  public Specification translate(ThrowsTag tag, DocumentedMethod excMember) {

    return BasicTranslator.translate(tag, excMember);

    //    // TODO Create the specification with the derived merged conditions.
    //
    //    return null;
  }
}
