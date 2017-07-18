package org.toradocu.translator;

import java.util.ArrayList;
import java.util.List;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.ThrowsTag;
import randoop.condition.specification.Guard;
import randoop.condition.specification.Specification;
import randoop.condition.specification.ThrowsSpecification;

public class ThrowsTranslator implements Translator<ThrowsTag> {

  @Override
  public List<? extends Specification> translate(ThrowsTag tag, DocumentedExecutable excMember) {
    final String commentTranslation = BasicTranslator.translate(tag, excMember);

    // TODO Replace empty strings!
    final Guard guard = new Guard("", commentTranslation);
    final String exceptionName = tag.getException().getName();
    final ThrowsSpecification spec = new ThrowsSpecification("", guard, exceptionName);
    final List<ThrowsSpecification> specs = new ArrayList<>();
    specs.add(spec);
    return specs;
  }
}
