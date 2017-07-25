package org.toradocu.output.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.toradocu.extractor.*;
import org.toradocu.extractor.DocumentedParameter;
import org.toradocu.translator.spec.Postcondition;

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

  public JsonOutput(DocumentedExecutable member) {
    //TODO translate the executable member to a serializable format
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
    this.parameters = new ArrayList<>();
    this.paramTags = new ArrayList<>();
    this.throwsTags = new ArrayList<>();
    ReturnTag mrt = member.returnTag();
    if (mrt != null) {
      Postcondition msp = member.returnTag().getSpecification();
      String spec = "";
      if (msp != null) spec = msp.getGuard().getCondition();
      this.returnTag = new ReturnTagOutput(mrt.getComment().getText(), mrt.getKind().name(), spec);
    }

    for (DocumentedParameter param : member.getParameters()) {
      Class pType = param.asReflectionParameter().getType();
      org.toradocu.output.util.Type paramType =
          new org.toradocu.output.util.Type(
              pType.getName(), pType.getSimpleName(), pType.isArray());
      org.toradocu.output.util.Parameter paramObj =
          new org.toradocu.output.util.Parameter(paramType, param.getName(), param.isNullable());
      parameters.add(paramObj);
    }

    for (ParamTag paramTag : member.paramTags()) {
      DocumentedParameter param = paramTag.getParameter();
      Class pType = param.asReflectionParameter().getType();
      org.toradocu.output.util.Type paramType =
          new org.toradocu.output.util.Type(
              pType.getName(), pType.getSimpleName(), pType.isArray());
      org.toradocu.output.util.Parameter paramObj =
          new org.toradocu.output.util.Parameter(paramType, param.getName(), param.isNullable());
      ParamTagOutput paramJsonObj =
          new ParamTagOutput(
              paramObj,
              paramTag.getComment(),
              paramTag.getKind().name(),
              paramTag.getSpecification().getGuard().getCondition());

      paramTags.add(paramJsonObj);
    }
    for (ThrowsTag throwsTag : member.throwsTags()) {
      Class eType = throwsTag.getException();
      org.toradocu.output.util.Type exType =
          new org.toradocu.output.util.Type(
              eType.getName(), eType.getSimpleName(), eType.isArray());

      ThrowsTagOutput paramJsonObj =
          new ThrowsTagOutput(
              exType,
              throwsTag.getComment(),
              throwsTag.getKind().name(),
              throwsTag.getSpecification().getGuard().getCondition());

      throwsTags.add(paramJsonObj);
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
}
