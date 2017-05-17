package org.toradocu.translator;

import java.util.ArrayList;
import java.util.List;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Tag;
import org.toradocu.translator.preprocess.*;

public class CommentTranslator {

  public static void translate(Tag tag, DocumentedMethod excMember) {

    // Preprocessing
    preprocessing(tag, excMember);
  }

  private static void preprocessing(Tag tag, DocumentedMethod excMember) {
    List<PreprocessingPhase> phases = new ArrayList<>();

    phases.add(new EndPeriod());

    switch (tag.getKind()) {
      case PARAM:
        phases.add(new RemoveCommas()); // TODO Make RemoveCommas a singleton?
        phases.add(new RemoveMayBe());
        phases.add(new MustWillShouldCanPatterns());
        break;
      case THROWS:
        phases.add(new RemoveCommas());
        phases.add(new NormalizeIfs());
        phases.add(new RemoveInitialIf());
        break;
      case RETURN:
        phases.add(new NormalizeIfs());
        break;
    }

    phases.add(new AddPlaceholders());

    new Preprocessor(phases).run(tag, excMember);
  }
}
