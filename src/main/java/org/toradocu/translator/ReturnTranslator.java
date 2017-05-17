package org.toradocu.translator;

import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.ReturnTag;
import org.toradocu.translator.spec.Specification;

public class ReturnTranslator implements Translator<ReturnTag> {

  @Override
  public Specification translate(ReturnTag tag, DocumentedMethod excMember) {
    return null;
  }
}
