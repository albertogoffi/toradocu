package org.toradocu.translator;

import static org.toradocu.util.ComplianceChecks.isSpecCompilable;

import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.ThrowsTag;
import randoop.condition.specification.Guard;
import randoop.condition.specification.ThrowsSpecification;

public class ThrowsTranslator {

  public ThrowsSpecification translate(ThrowsTag tag, DocumentedExecutable excMember) {
    final String commentTranslation =
        alwaysThrowException(tag.getComment().getText())
            ? "true"
            : BasicTranslator.translate(tag, excMember);

    final Guard guard = new Guard(tag.getComment().getText(), commentTranslation);
    final String exceptionName = tag.getException().getName();

    if (commentTranslation.isEmpty() || !isSpecCompilable(excMember, guard)) {
      return new ThrowsSpecification(
          tag.toString(), new Guard(tag.getComment().getText(), ""), exceptionName);
    }

    return new ThrowsSpecification(tag.toString(), guard, exceptionName);
  }

  /**
   * Returns true if an exception is always thrown by the {@code DocumentedExecutable}
   *
   * @param commentText the String comment belonging to the {@code ThrowsTag}
   * @return true if the comment just states "always", false otherwise
   */
  private boolean alwaysThrowException(String commentText) {
    commentText = commentText.replace(".", "");
    if (commentText.equalsIgnoreCase("always")) {
      return true;
    }

    return false;
  }
}
