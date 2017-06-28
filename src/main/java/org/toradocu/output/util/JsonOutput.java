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
}
