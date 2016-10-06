package org.toradocu.util;

import java.util.ArrayList;
import java.util.List;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.ThrowsTag;

public class Stats {

  public static List<MethodStats> getStats(
      List<DocumentedMethod> actualMethodList, List<DocumentedMethod> expectedMethodList) {
    if (actualMethodList.size() != expectedMethodList.size()) {
      throw new IllegalArgumentException(
          "Actual and expected method list should be of the same size.");
    }

    List<MethodStats> stats = new ArrayList<>();

    for (int methodIndex = 0; methodIndex < expectedMethodList.size(); methodIndex++) {
      DocumentedMethod expectedMethod = expectedMethodList.get(methodIndex);
      ThrowsTag[] expectedMethodTags = expectedMethod.throwsTags().toArray(new ThrowsTag[0]);
      DocumentedMethod actualMethod = actualMethodList.get(methodIndex);
      ThrowsTag[] actualMethodTags = actualMethod.throwsTags().toArray(new ThrowsTag[0]);
      if (expectedMethodTags.length != actualMethodTags.length) {
        throw new IllegalArgumentException(
            "The number of @throws ("
                + expectedMethodTags.length
                + ") of method "
                + actualMethod
                + " is different than expected ("
                + actualMethodTags.length
                + ")");
      }

      MethodStats methodStats =
          new MethodStats(actualMethod.getContainingClass() + "." + actualMethod.getSignature());
      for (int tagIndex = 0; tagIndex < expectedMethodTags.length; tagIndex++) {
        ThrowsTag expectedTag = expectedMethodTags[tagIndex];
        ThrowsTag actualTag = actualMethodTags[tagIndex];
        String expectedCondition = expectedTag.getCondition().get();
        String actualCondition = actualTag.getCondition().get();

        // Ignore conditions for which there is no known translation
        if (!expectedCondition.isEmpty()) {
          if (expectedCondition.equals(actualCondition)) {
            methodStats.addCorrectTranslation();
          } else {
            if (actualCondition.isEmpty()) {
              methodStats.addMissingTranslation();
            } else {
              methodStats.addWrongTranslation();
            }
          }
        }
      }
      stats.add(methodStats);
    }
    return stats;
  }
}
