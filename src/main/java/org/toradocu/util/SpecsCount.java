package org.toradocu.util;

import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.ReturnTag;

/**
 * Given a goal JSON file as produced by Toradocu, this program prints the number of specifications
 * contained in the file. Notice that this program ignores inherited specs and specs of private
 * methods.
 *
 * <p>The output format of this program is the following (n is a non-negative integer): PRE n POST n
 * EXC n
 */
public class SpecsCount {

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      throw new IllegalArgumentException("Please provide a Toradocu JSON file.");
    }

    final String jsonFile = args[0];
    final List<DocumentedMethod> specs;
    try (BufferedReader file = Files.newBufferedReader(Paths.get(jsonFile))) {
      Type collectionType = new TypeToken<Collection<DocumentedMethod>>() {}.getType();
      specs = GsonInstance.gson().fromJson(file, collectionType);
    }

    int pre = 0, post = 0, exc = 0;

    for (DocumentedMethod method : specs) {
      if (method.getTargetClass().equals(method.getContainingClass().getQualifiedName())
          || !Modifier.isPrivate(method.getExecutable().getModifiers())) {
        pre +=
            method
                .paramTags()
                .stream()
                .map(t -> t.getCondition())
                .filter(c -> c.isPresent() && !c.get().isEmpty())
                .count();
        exc +=
            method
                .throwsTags()
                .stream()
                .map(t -> t.getCondition())
                .filter(c -> c.isPresent() && !c.get().isEmpty())
                .count();
        ReturnTag returnTag = method.returnTag();
        if (returnTag != null) {
          final String condition = returnTag.getCondition().orElse("");
          if (!condition.isEmpty()) {
            post += condition.split(";").length;
          }
        }
      }
    }

    System.out.println("PRE " + pre);
    System.out.println("POST " + post);
    System.out.println("EXC " + exc);
  }
}
