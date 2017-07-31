package org.toradocu.translator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.ParamTag;
import org.toradocu.extractor.ReturnTag;
import org.toradocu.extractor.ThrowsTag;
import org.toradocu.translator.preprocess.PreprocessorFactory;
import randoop.condition.specification.Operation;
import randoop.condition.specification.OperationSpecification;
import randoop.condition.specification.PostSpecification;
import randoop.condition.specification.PreSpecification;
import randoop.condition.specification.ThrowsSpecification;

/** Translates comments into procedure specifications. */
public class CommentTranslator {

  /**
   * Translates the given @param comment into a precondition specification.
   *
   * @param tag the comment to be translated
   * @param excMember the executable member commented with {@code tag}
   * @return a precondition specification (an empty specification if the translation fails)
   */
  public static PreSpecification translate(ParamTag tag, DocumentedExecutable excMember) {
    PreprocessorFactory.create(tag.getKind()).preprocess(tag, excMember);
    return new ParamTranslator().translate(tag, excMember);
  }

  /**
   * Translates the given @return comment into a list of precondition specifications.
   *
   * @param tag the comment to be translated
   * @param excMember the executable member commented with {@code tag}
   * @return a list of precondition specifications (each specification can be empty if the
   *     translation failed)
   */
  public static List<PostSpecification> translate(ReturnTag tag, DocumentedExecutable excMember) {
    PreprocessorFactory.create(tag.getKind()).preprocess(tag, excMember);
    return new ReturnTranslator().translate(tag, excMember);
  }

  /**
   * Translates the given @throws/@exception comment into a precondition specification.
   *
   * @param tag the comment to be translated
   * @param excMember the executable member commented with {@code tag}
   * @return a precondition specification (an empty specification if the translation fails)
   */
  public static ThrowsSpecification translate(ThrowsTag tag, DocumentedExecutable excMember) {
    PreprocessorFactory.create(tag.getKind()).preprocess(tag, excMember);
    return new ThrowsTranslator().translate(tag, excMember);
  }

  /**
   * Creates the specifications from the comments of the given executable members.
   *
   * @param members the executable members whose comments have to be translated into specifications
   * @return a map that associates each executable member (key) with its operation specification
   *     that includes pre-, post-, and exceptional specifications.
   */
  public static Map<DocumentedExecutable, OperationSpecification> createSpecifications(
      List<DocumentedExecutable> members) {
    Map<DocumentedExecutable, OperationSpecification> specs = new LinkedHashMap<>();
    for (DocumentedExecutable member : members) {
      Operation operation = Operation.getOperation(member.getExecutable());
      OperationSpecification spec = new OperationSpecification(operation);

      List<PreSpecification> preSpecifications = new ArrayList<>();
      for (ParamTag paramTag : member.paramTags()) {
        preSpecifications.add(CommentTranslator.translate(paramTag, member));
      }
      spec.addParamSpecifications(preSpecifications);

      List<ThrowsSpecification> throwsSpecifications = new ArrayList<>();
      for (ThrowsTag throwsTag : member.throwsTags()) {
        throwsSpecifications.add(CommentTranslator.translate(throwsTag, member));
      }
      spec.addThrowsSpecifications(throwsSpecifications);

      List<PostSpecification> postSpecifications = new ArrayList<>();
      ReturnTag returnTag = member.returnTag();
      if (returnTag != null) {
        postSpecifications.addAll(CommentTranslator.translate(returnTag, member));
      }
      spec.addReturnSpecifications(postSpecifications);

      specs.put(member, spec);
    }
    return specs;
  }
}
