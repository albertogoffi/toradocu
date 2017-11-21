package org.toradocu.util.old;

import com.github.javaparser.ParseException;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.toradocu.extractor.ExecutableMember;
import org.toradocu.extractor.ParamTag;
import org.toradocu.extractor.ReturnTag;
import org.toradocu.extractor.ThrowsTag;
import org.toradocu.util.GsonInstance;

/**
 * Given two Toradocu specs JSON files F1 and F2, this program copies the condition translations
 * present in F1 to F2 (for the comments present in both F1 and F2).
 *
 * <p>Output is in JSON format and it is printed on the standard output.
 */
public class JsonUpdater {

  public static void main(String[] args) throws IOException, ParseException {
    // Validate command line arguments.
    if (args.length != 2) {
      throw new IllegalArgumentException(
          "This program must be invoked with the following arguments:"
              + "\n1. Old Toradocu JSON file to update."
              + "\n2. New Toradocu JSON file.");
    }

    final Type collectionType = new TypeToken<Collection<ExecutableMember>>() {}.getType();

    final String oldJSONSpecs = args[0];
    final String newJSONSpecs = args[1];

    // Load content of JSON files.
    final List<ExecutableMember> oldSpecs, newSpecs;
    try (BufferedReader oldSpecsFile = Files.newBufferedReader(Paths.get(oldJSONSpecs));
        BufferedReader newSpecsFile = Files.newBufferedReader(Paths.get(newJSONSpecs))) {
      oldSpecs = GsonInstance.gson().fromJson(oldSpecsFile, collectionType);
      newSpecs = GsonInstance.gson().fromJson(newSpecsFile, collectionType);
    }

    // Copy goal translations from old specs to new specs.
    for (ExecutableMember newMethod : newSpecs) {

      // Find in old specs the method corresponding to newMethod in new specs.
      List<ExecutableMember> matchingMethods =
          oldSpecs
              .stream()
              .filter(m -> m.getSignature().equals(newMethod.getSignature()))
              .collect(Collectors.toList());
      // Sanity checks.
      if (matchingMethods.size() == 0) {
        throw new AssertionError("Method " + newMethod + " not present in " + oldJSONSpecs);
      }
      if (matchingMethods.size() > 1) {
        throw new AssertionError(
            "Multiple matching methods found for " + newMethod + ":\n" + matchingMethods);
      }

      // oldMethod is the method in old specs corresponding to newMethod.
      final ExecutableMember oldMethod = matchingMethods.get(0);

      updateParamTags(oldJSONSpecs, oldMethod, newMethod);
      updateReturnTag(oldMethod, newMethod);
      updateThrowsTags(oldJSONSpecs, oldMethod, newMethod);
    }

    // Print the JSON on the standard output.
    System.out.println(GsonInstance.gson().toJson(newSpecs, collectionType));
  }

  private static void updateReturnTag(ExecutableMember oldMethod, ExecutableMember newMethod) {
    final ReturnTag oldReturnTag = oldMethod.returnTag();
    final ReturnTag newReturnTag = newMethod.returnTag();
    if (oldReturnTag == null && newReturnTag != null) {
      // The opposite situation is legal. When oldReturnTag != null and newReturnTag == null, it
      // means that the comment was inherited in the old specs.
      throw new AssertionError(
          "Mismatch between old and new return tags of method " + oldMethod.getSignature());
    }
    if (newReturnTag != null) {
      if (!oldReturnTag.getComment().equals(newReturnTag.getComment())) {
        throw new AssertionError(
            "Mismatch between old and new return tag comments of method "
                + oldMethod.getSignature());
      }
      newReturnTag.setCondition(oldReturnTag.getCondition());
    }
  }

  // Copy @param translations from old specs (oldMethod) to new specs (newMethod).
  private static void updateParamTags(
      String oldJSONSpecs, ExecutableMember oldMethod, ExecutableMember newMethod) {
    final Set<ParamTag> oldParamTags = oldMethod.paramTags();
    for (ParamTag newParamTag : newMethod.paramTags()) {
      final List<ParamTag> matchingOldParamTags =
          oldParamTags
              .stream()
              .filter(
                  t ->
                      t.parameter().getName().equals(newParamTag.parameter().getName())
                          && t.getComment().equals(newParamTag.getComment()))
              .collect(Collectors.toList());
      // Sanity checks.
      if (matchingOldParamTags.size() == 0) {
        throw new AssertionError(
            "BlockTag "
                + newParamTag
                + " not present in method "
                + oldMethod.getSignature()
                + " of "
                + oldJSONSpecs);
      }
      if (matchingOldParamTags.size() > 1) {
        throw new AssertionError(
            "Multiple matching tags found for " + newParamTag + ":\n" + matchingOldParamTags);
      }

      // oldMethod is the method in old specs corresponding to newMethod.
      final ParamTag oldTag = matchingOldParamTags.get(0);

      // Set the goal condition in new specs.
      newParamTag.setCondition(oldTag.getCondition());
    }
  }

  private static void updateThrowsTags(
      String oldJSONSpecs, ExecutableMember oldMethod, ExecutableMember newMethod) {
    final Set<ThrowsTag> oldThrowsTags = oldMethod.throwsTags();
    for (ThrowsTag newThrowsTag : newMethod.throwsTags()) {
      final List<ThrowsTag> matchingOldThrowsTags =
          oldThrowsTags
              .stream()
              .filter(
                  t ->
                      t.exceptionType().equals(newThrowsTag.exceptionType())
                          && t.getComment().equals(newThrowsTag.getComment()))
              .collect(Collectors.toList());
      // Sanity checks.
      if (matchingOldThrowsTags.size() == 0) {
        throw new AssertionError(
            "BlockTag "
                + newThrowsTag
                + " not present in method "
                + oldMethod.getSignature()
                + " of "
                + oldJSONSpecs);
      }
      if (matchingOldThrowsTags.size() > 1) {
        throw new AssertionError(
            "Multiple matching tags found for " + newThrowsTag + ":\n" + matchingOldThrowsTags);
      }

      // oldMethod is the method in old specs corresponding to newMethod.
      final ThrowsTag oldTag = matchingOldThrowsTags.get(0);

      // Set the goal condition in new specs.
      newThrowsTag.setCondition(oldTag.getCondition());
    }
  }
}
