package org.toradocu.translator;

import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.Tag;
import org.toradocu.translator.preprocess.PreprocessorFactory;
import org.toradocu.translator.spec.Specification;

public class CommentTranslator {

  public static void translate(Tag tag, DocumentedExecutable excMember) {

    // Preprocessing.
    PreprocessorFactory.create(tag.getKind()).preprocess(tag, excMember);

    // Translation.
    final Translator<Tag> translator = TranslatorFactory.create(tag);
    final Specification specification = translator.translate(tag, excMember);

    // TODO Check the consistency of the generated specification.

    tag.setSpecification(specification);
  }
}
