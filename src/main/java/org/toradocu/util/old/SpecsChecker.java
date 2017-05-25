package org.toradocu.util.old;

import static java.util.stream.Collectors.toList;

import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.toradocu.extractor.ExecutableMember;
import org.toradocu.extractor.ParamTag;
import org.toradocu.extractor.ThrowsTag;
import org.toradocu.util.GsonInstance;

public class SpecsChecker {

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("This program must be invoked with the following parameter:");
      System.out.println("1) Path to the JSON specs file to be checked.");
      System.exit(1);
    }

    // Path to the JSON goal file to convert.
    final String inputFilePath = args[0];

    java.lang.reflect.Type listType = new TypeToken<List<ExecutableMember>>() {}.getType();
    try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputFilePath))) {
      System.out.println("Checking specs from " + inputFilePath);
      List<ExecutableMember> methods = GsonInstance.gson().fromJson(reader, listType);
      checkThrowsConsistency(methods);
      checkParamThrowsConsistency(methods);
    }
  }

  private static void checkThrowsConsistency(List<ExecutableMember> methods) {
    for (ExecutableMember method : methods) {
      final Set<ThrowsTag> throwsTags = method.throwsTags();
      final List<String> postconditions =
          throwsTags
              .stream()
              .map(t -> t.getCondition().replaceAll(" ", ""))
              .filter(t -> !t.isEmpty())
              .collect(toList());
      // Check if there are duplicated exceptional postconditions.
      if (new HashSet<>(postconditions).size() != postconditions.size()) {
        System.out.println(
            "\tERROR: Found conflicting specs! Duplicated exceptional postconditions in method "
                + method
                + ": "
                + postconditions);
      }
    }
  }

  private static void checkParamThrowsConsistency(List<ExecutableMember> methods) {
    for (ExecutableMember method : methods) {
      final Set<ParamTag> paramTags = method.paramTags();
      final Set<ThrowsTag> throwsTags = method.throwsTags();

      for (ParamTag param : paramTags) {
        String precondition = param.getCondition().replaceAll(" ", "");
        if (precondition.isEmpty()) {
          continue;
        }
        final List<String> conflictingSpecs =
            throwsTags
                .stream()
                .map(tag -> tag.getCondition())
                .map(postcondition -> postcondition.replaceAll(" ", ""))
                .filter(postcondition -> postcondition.equals(precondition))
                .collect(toList());
        if (!conflictingSpecs.isEmpty()) {
          System.out.println(
              "\tERROR: Found conflicting specs! Precondition "
                  + precondition
                  + " has a conflict with exceptional postconditions: "
                  + conflictingSpecs);
        }
      }
    }
  }
}
