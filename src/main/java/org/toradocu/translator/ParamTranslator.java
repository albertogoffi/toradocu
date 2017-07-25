package org.toradocu.translator;

import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.ParamTag;
import randoop.condition.specification.Guard;
import randoop.condition.specification.PreSpecification;

public class ParamTranslator {

  public PreSpecification translate(ParamTag tag, DocumentedExecutable excMember) {
    final String commentTranslation = BasicTranslator.translate(tag, excMember);

    // TODO Replace empty strings!
    final Guard guard = new Guard("", commentTranslation);
    return new PreSpecification("", guard);
  }
}
