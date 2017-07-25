package org.toradocu.translator;

import java.util.List;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.ParamTag;
import org.toradocu.extractor.ReturnTag;
import org.toradocu.extractor.ThrowsTag;
import randoop.condition.specification.PostSpecification;
import randoop.condition.specification.PreSpecification;
import randoop.condition.specification.ThrowsSpecification;

public class CommentTranslator {

  public static PreSpecification translate(ParamTag tag, DocumentedExecutable excMember) {
    return new ParamTranslator().translate(tag, excMember);
  }

  public static List<PostSpecification> translate(ReturnTag tag, DocumentedExecutable excMember) {
    return new ReturnTranslator().translate(tag, excMember);
  }

  public static ThrowsSpecification translate(ThrowsTag tag, DocumentedExecutable excMember) {
    return new ThrowsTranslator().translate(tag, excMember);
  }
}
