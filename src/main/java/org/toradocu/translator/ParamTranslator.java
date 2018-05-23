package org.toradocu.translator;

import static org.toradocu.util.ComplianceChecks.isSpecCompilable;

import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.ParamTag;
import randoop.condition.specification.Guard;
import randoop.condition.specification.PreSpecification;

public class ParamTranslator {

  public PreSpecification translate(ParamTag tag, DocumentedExecutable excMember) {
    final String commentTranslation =
        isDescriptiveComment(tag.getComment().getText())
            ? ""
            : BasicTranslator.translate(tag, excMember);

    final Guard guard = new Guard(tag.getComment().getText(), commentTranslation);

    if (commentTranslation.isEmpty() || !isSpecCompilable(excMember, guard)) {
      return new PreSpecification(tag.toString(), new Guard(tag.getComment().getText(), ""));
    }

    return new PreSpecification(tag.toString(), guard);
  }

  private boolean isDescriptiveComment(String text) {
    return text.matches("(.*) is (If|if) (.*)");
  }
}
