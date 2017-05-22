package org.toradocu.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.toradocu.Checks;
import org.toradocu.conf.Configuration;
import org.toradocu.extractor.ExecutableMember;
import org.toradocu.extractor.ParamTag;
import org.toradocu.extractor.Parameter;
import org.toradocu.extractor.ReturnTag;
import org.toradocu.extractor.Tag;
import org.toradocu.extractor.ThrowsTag;
import randoop.condition.specification.Guard;
import randoop.condition.specification.Identifiers;
import randoop.condition.specification.Operation;
import randoop.condition.specification.OperationSpecification;
import randoop.condition.specification.PostSpecification;
import randoop.condition.specification.PreSpecification;
import randoop.condition.specification.Property;
import randoop.condition.specification.ThrowsSpecification;

/**
 * Translates Toradocu specifications into Randoop input specifications. See methods {@code
 * translate} to convert a Toradocu data structure to a Randoop specification type.
 */
public class RandoopSpecs {

  /**
   * Converts the specifications of the given executable class member into Randoop operation
   * specifications.
   *
   * @param method a documented executable member of a class
   * @return specifications for {@code method} in the Randoop operation specification format
   */
  public static OperationSpecification translate(ExecutableMember method) {
    Operation operation = Operation.getOperation(method.getExecutable());

    List<String> params =
        method.getParameters().stream().map(Parameter::getName).collect(Collectors.toList());
    Identifiers identifiers =
        new Identifiers(params, Configuration.RECEIVER, Configuration.RETURN_VALUE);

    List<PreSpecification> paramSpecs =
        method
            .paramTags()
            .stream()
            .map(t -> RandoopSpecs.translate(t, method))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    List<ThrowsSpecification> throwsSpecs =
        method
            .throwsTags()
            .stream()
            .map(t -> RandoopSpecs.translate(t, method))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    List<PostSpecification> returnSpecs = new ArrayList<>();
    ReturnTag returnTag = method.returnTag();
    if (returnTag != null) {
      returnSpecs = RandoopSpecs.translate(returnTag, method);
      if (returnSpecs == null) {
        returnSpecs = new ArrayList<>();
      }
    }
    return new OperationSpecification(operation, identifiers, throwsSpecs, returnSpecs, paramSpecs);
  }

  /**
   * Converts the specifications for the given tag into Randoop {@code param} specifications.
   *
   * @param tag the tag whose comment translation has to be converted to Randoop format
   * @param method the executable member the tag belongs to
   * @return the Randoop {@code param} specification, or {@code null} if the tag comment has not
   *     been translated by Toradocu, or if the translation is empty
   */
  public static PreSpecification translate(ParamTag tag, ExecutableMember method) {
    String condition = tag.getCondition();
    return condition.isEmpty()
        ? null
        : new PreSpecification(
            tag.getComment(), new Guard(tag.getComment(), processCondition(condition, method)));
  }

  /**
   * Converts the specifications for the given tag into Randoop {@code return} specifications.
   *
   * @param tag the tag whose comment translation has to be converted to Randoop format
   * @param method the executable member the tag belongs to
   * @return the Randoop {@code return} specification, or {@code null} if the tag comment has not
   *     been translated by Toradocu, or if the translation is empty
   */
  public static List<PostSpecification> translate(ReturnTag tag, ExecutableMember method) {
    String condition = tag.getCondition();
    if (condition.isEmpty()) {
      return null;
    }
    if (condition.indexOf("?") <= 0) {
      return null;
    }

    // Remove whitespaces to do not influence parsing.
    condition = condition.replace(" ", "");

    String guardCondition = condition.substring(0, condition.indexOf("?"));
    Guard guard = new Guard("", processCondition(guardCondition, method));

    String tagKind = format(tag.getKind());
    String description = tagKind + " " + tag.getComment();

    List<PostSpecification> specs = new ArrayList<>();
    String propertiesStr = condition.substring(condition.indexOf("?") + 1, condition.length());
    String[] properties = propertiesStr.split(":", 2);
    Property property = new Property(tag.getComment(), processCondition(properties[0], method));
    specs.add(new PostSpecification(description, guard, property));
    if (properties.length > 1) {
      Property elseProperty =
          new Property(tag.getComment(), processCondition(properties[1], method));
      specs.add(new PostSpecification(description, guard, elseProperty));
    }
    return specs;
  }

  /**
   * Converts the specifications for the given tag into Randoop {@code throws} specifications.
   *
   * @param tag the tag whose comment translation has to be converted to Randoop format
   * @param method the executable member the tag belongs to
   * @return the Randoop {@code throws} specification, or {@code null} if the tag comment has not
   *     been translated by Toradocu, or if the translation is empty
   */
  public static ThrowsSpecification translate(ThrowsTag tag, ExecutableMember method) {
    String condition = tag.getCondition();
    if (condition.isEmpty()) {
      return null;
    }

    String tagKind = format(tag.getKind());
    String description = tagKind + " " + tag.exception().getSimpleName() + " " + tag.getComment();
    Guard guard = new Guard(tag.getComment(), processCondition(condition, method));
    return new ThrowsSpecification(description, guard, tag.exception().getQualifiedName());
  }

  /**
   * Format the a {@code Tag.Kind} as expected in Randoop specifications, i.e., removing the initial
   * {@code "@"}.
   *
   * @param tagKind a tag kind
   * @return the tag kind without the initial {@code "@"}
   */
  private static String format(Tag.Kind tagKind) {
    return tagKind.toString().replace("@", "");
  }

  /**
   * Replace "args" identifiers in specifications generated by Toradocu with the actual parameter
   * name the identifiers refers to.
   *
   * @param condition the condition in which replace names
   * @param method the documented method the condition specifies
   * @return the given condition with the actual parameter names
   */
  private static String processCondition(String condition, ExecutableMember method) {
    Checks.nonNullParameter(condition, "condition");
    Checks.nonNullParameter(method, "method");

    for (int index = 0; index < method.getParameters().size(); index++) {
      String paramName = method.getParameters().get(index).getName();
      condition = condition.replace("args[" + index + "]", paramName);
    }
    return condition;
  }
}
