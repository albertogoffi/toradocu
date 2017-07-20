package org.toradocu.translator;

import org.toradocu.extractor.BlockTag;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.translator.preprocess.PreprocessorFactory;
import org.toradocu.translator.spec.Specification;

public class CommentTranslator {

  public static <T extends BlockTag<Specification>> void translate(
      T tag, DocumentedExecutable excMember) {

    // Preprocessing.
    PreprocessorFactory.create(tag.getKind()).preprocess(tag, excMember);

    // Translation.
    final Translator<T> translator = TranslatorFactory.create(tag);
    final Specification specification = translator.translate(tag, excMember);

    // TODO Check the consistency of the generated specification.

    tag.setSpecification(specification);
  }
}
