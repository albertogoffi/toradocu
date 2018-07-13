package org.toradocu.output.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.toradocu.extractor.*;
import org.toradocu.extractor.DocumentedParameter;
import randoop.condition.specification.OperationSpecification;
import randoop.condition.specification.PostSpecification;
import randoop.condition.specification.PreSpecification;
import randoop.condition.specification.ThrowsSpecification;

/** Created by arianna on 28/06/17. */
public class JsonOutput {
  public String signature;
  public String name;
  public org.toradocu.output.util.Type containingClass;
  public String targetClass;
  public boolean isVarArgs;
  public org.toradocu.output.util.Type returnType;
  public List<org.toradocu.output.util.Parameter> parameters;
  public List<ParamTagOutput> paramTags;
  public ReturnTagOutput returnTag;
  public List<ThrowsTagOutput> throwsTags;

  public JsonOutput(DocumentedExecutable member, OperationSpecification specification) {
    // TODO translate the executable member to a serializable format
    this.signature = member.getSignature();
    this.name = member.getName();
    this.containingClass =
        new org.toradocu.output.util.Type(
            member.getDeclaringClass().getName(),
            member.getDeclaringClass().getSimpleName(),
            member.getDeclaringClass().isArray());
    this.targetClass = member.getDeclaringClass().getName();
    this.isVarArgs = member.isVarArgs();

    String returnTypeName = member.getExecutable().getAnnotatedReturnType().getType().getTypeName();
    this.returnType =
        new org.toradocu.output.util.Type(
            returnTypeName, returnTypeName, returnTypeName.endsWith("]"));

    createParameters(member);
    createParamTags(member, specification.getPreSpecifications());
    createThrowsTags(member, specification.getThrowsSpecifications());
    createReturnTags(member, specification.getPostSpecifications());
  }

  private void createReturnTags(DocumentedExecutable member, List<PostSpecification> specs) {
    if (specs.size() > 2) {
      throw new IllegalArgumentException("Old Toradocu had a limited support for return specs");
    }

    ReturnTag mrt = member.returnTag();
    if (mrt != null) { // If member has a @return comment.
      String spec = "";
      if (!specs.isEmpty()) { // If Toradocu produced a translation for the @return comment.
        PostSpecification postSpec = specs.get(0);
        spec =
            postSpec.getGuard().getConditionText()
                + " ? "
                + postSpec.getProperty().getConditionText();
        if (specs.size() > 1) {
          postSpec = specs.get(1);
          spec += " : " + postSpec.getProperty().getConditionText();
        }
      }
      this.returnTag = new ReturnTagOutput(mrt.getComment().getText(), mrt.getKind().name(), spec);
    }
  }

  private void createThrowsTags(DocumentedExecutable member, List<ThrowsSpecification> specs) {
    this.throwsTags = new ArrayList<>();
    for (int i = 0; i < member.throwsTags().size(); i++) {
      ThrowsTag throwsTag = member.throwsTags().get(i);
      Class eType = throwsTag.getException();
      Type exType = new Type(eType.getName(), eType.getSimpleName(), eType.isArray());
      final ThrowsSpecification throwsSpecification = specs.get(i);
      String condition = throwsSpecification.getGuard().getConditionText();
      ThrowsTagOutput paramJsonObj =
          new ThrowsTagOutput(
              exType, throwsTag.getComment(), throwsTag.getKind().name(), condition);
      throwsTags.add(paramJsonObj);
    }
  }

  private void createParamTags(DocumentedExecutable member, List<PreSpecification> specs) {
    this.paramTags = new ArrayList<>();
    for (int i = 0; i < member.paramTags().size(); i++) {
      ParamTag paramTag = member.paramTags().get(i);
      DocumentedParameter param = paramTag.getParameter();
      Class pType = param.asReflectionParameter().getType();
      Type paramType = new Type(pType.getName(), pType.getSimpleName(), pType.isArray());
      Parameter paramObj = new Parameter(paramType, param.getName(), param.isNullable());
      final PreSpecification preSpecification = specs.get(i);
      ParamTagOutput paramJsonObj =
          new ParamTagOutput(
              paramObj,
              paramTag.getComment(),
              paramTag.getKind().name(),
              preSpecification.getGuard().getConditionText());

      paramTags.add(paramJsonObj);
    }
  }

  private void createParameters(DocumentedExecutable member) {
    this.parameters = new ArrayList<>();
    for (DocumentedParameter param : member.getParameters()) {
      Class pType = param.asReflectionParameter().getType();
      Type paramType = new Type(pType.getName(), pType.getSimpleName(), pType.isArray());
      Parameter paramObj = new Parameter(paramType, param.getName(), param.isNullable());
      parameters.add(paramObj);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    JsonOutput that = (JsonOutput) o;

    return (isVarArgs == that.isVarArgs)
        && Objects.equals(signature, that.signature)
        && Objects.equals(name, that.name)
        && Objects.equals(containingClass, that.containingClass)
        && Objects.equals(targetClass, that.targetClass)
        && Objects.equals(returnType, that.returnType)
        && Objects.equals(parameters, that.parameters)
        && Objects.equals(paramTags, that.paramTags)
        && Objects.equals(returnTag, that.returnTag)
        && Objects.equals(throwsTags, that.throwsTags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        isVarArgs,
        signature,
        name,
        containingClass,
        targetClass,
        returnType,
        parameters,
        paramTags,
        returnTag,
        throwsTags);
  }

  @Override
  public String toString() {
    return signature + "\n" + paramTags + "\n" + returnTag + "\n" + throwsTags;
  }
}
