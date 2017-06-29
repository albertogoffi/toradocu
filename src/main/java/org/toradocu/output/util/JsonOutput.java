package org.toradocu.output.util;

import com.google.gson.*;
import java.util.ArrayList;
import java.util.List;
import org.toradocu.extractor.*;
import org.toradocu.extractor.Parameter;
import org.toradocu.translator.spec.Postcondition;

/** Created by arianna on 28/06/17. */
public class JsonOutput {

  String signature;
  String name;
  org.toradocu.output.util.Type containingClass;
  String targetClass;
  boolean isVarArgs;
  org.toradocu.output.util.Type returnType;
  List<org.toradocu.output.util.Parameter> parameters;
  List<ParamTagOutput> paramTags;
  ReturnTagOutput returnTag;
  List<ThrowsTagOutput> throwsTags;

  public JsonOutput(ExecutableMember member) {
    //TODO translate the executable member to a serializable format
    this.signature = member.getSignature();
    this.name = member.getName();
    this.containingClass =
        new org.toradocu.output.util.Type(
            member.getContainingClass().getName(),
            member.getContainingClass().getSimpleName(),
            member.getContainingClass().isArray());
    this.targetClass = member.getContainingClass().getName();
    this.isVarArgs = member.isVarArgs();

    String returnTypeName = member.getExecutable().getAnnotatedReturnType().getType().getTypeName();
    this.returnType =
        new org.toradocu.output.util.Type(
            returnTypeName, returnTypeName, returnTypeName.endsWith("]"));
    this.parameters = new ArrayList<org.toradocu.output.util.Parameter>();
    this.paramTags = new ArrayList<ParamTagOutput>();
    this.throwsTags = new ArrayList<ThrowsTagOutput>();
    ReturnTag mrt = member.returnTag();
    if (mrt != null) {
      Postcondition msp = member.returnTag().getSpecification();
      String spec = "";
      if (msp != null) spec = msp.getGuard().getCondition();
      this.returnTag = new ReturnTagOutput(mrt.getComment().getText(), mrt.getKind().name(), spec);
    }

    for (Parameter param : member.getParameters()) {
      Class pType = param.asReflectionParameter().getType();
      org.toradocu.output.util.Type paramType =
          new org.toradocu.output.util.Type(
              pType.getName(), pType.getSimpleName(), pType.isArray());
      org.toradocu.output.util.Parameter paramObj =
          new org.toradocu.output.util.Parameter(paramType, param.getName(), param.isNullable());
      parameters.add(paramObj);
    }

    for (Tag tag : member.getTags()) {
      if (tag instanceof ParamTag) {
        Parameter param = ((ParamTag) tag).getParameter();
        Class pType = ((ParamTag) tag).getParameter().asReflectionParameter().getType();
        org.toradocu.output.util.Type paramType =
            new org.toradocu.output.util.Type(
                pType.getName(), pType.getSimpleName(), pType.isArray());
        org.toradocu.output.util.Parameter paramObj =
            new org.toradocu.output.util.Parameter(paramType, param.getName(), param.isNullable());
        ParamTagOutput paramJsonObj =
            new ParamTagOutput(
                paramObj,
                tag.getComment(),
                tag.getKind().name(),
                tag.getSpecification().getGuard().getCondition());

        paramTags.add(paramJsonObj);
      } else if (tag instanceof ThrowsTag) {
        Class eType = ((ThrowsTag) tag).getException();
        org.toradocu.output.util.Type exType =
            new org.toradocu.output.util.Type(
                eType.getName(), eType.getSimpleName(), eType.isArray());

        ThrowsTagOutput paramJsonObj =
            new ThrowsTagOutput(
                exType,
                tag.getComment(),
                tag.getKind().name(),
                tag.getSpecification().getGuard().getCondition());

        throwsTags.add(paramJsonObj);
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    JsonOutput that = (JsonOutput) o;

    if (isVarArgs != that.isVarArgs) return false;
    if (signature != null ? !signature.equals(that.signature) : that.signature != null)
      return false;
    if (name != null ? !name.equals(that.name) : that.name != null) return false;
    if (containingClass != null
        ? !containingClass.equals(that.containingClass)
        : that.containingClass != null) return false;
    if (targetClass != null ? !targetClass.equals(that.targetClass) : that.targetClass != null)
      return false;
    if (returnType != null ? !returnType.equals(that.returnType) : that.returnType != null)
      return false;
    if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null)
      return false;
    if (paramTags != null ? !paramTags.equals(that.paramTags) : that.paramTags != null)
      return false;
    if (returnTag != null ? !returnTag.equals(that.returnTag) : that.returnTag != null)
      return false;
    return throwsTags != null ? throwsTags.equals(that.throwsTags) : that.throwsTags == null;
  }
}
