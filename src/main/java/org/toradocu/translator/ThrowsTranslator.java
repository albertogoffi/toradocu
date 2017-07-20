package org.toradocu.translator;

import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.ThrowsTag;
import randoop.condition.specification.Guard;
import randoop.condition.specification.ThrowsSpecification;

public class ThrowsTranslator implements Translator<ThrowsTag> {

  @Override
  public void translate(ThrowsTag tag, DocumentedExecutable excMember) {
    final String commentTranslation = BasicTranslator.translate(tag, excMember);

    // TODO Replace empty strings!
    final Guard guard = new Guard("", commentTranslation);
    final String exceptionName = tag.getException().getName();
    tag.setSpecification(new ThrowsSpecification("", guard, exceptionName));
  }
}
