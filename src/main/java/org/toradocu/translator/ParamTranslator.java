package org.toradocu.translator;

import java.util.ArrayList;
import java.util.List;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.ParamTag;
import randoop.condition.specification.Guard;
import randoop.condition.specification.PreSpecification;

public class ParamTranslator implements Translator<ParamTag> {

  @Override
  public List<PreSpecification> translate(ParamTag tag, DocumentedExecutable excMember) {
    final String commentTranslation = BasicTranslator.translate(tag, excMember);

    // TODO Replace empty strings!
    final Guard guard = new Guard("", commentTranslation);
    final PreSpecification spec = new PreSpecification("", guard);
    final List<PreSpecification> specs = new ArrayList<>();
    specs.add(spec);
    return specs;
  }
}
