package org.toradocu.util.old;

import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import org.toradocu.extractor.ExecutableMember;
import org.toradocu.extractor.ReturnTag;
import org.toradocu.util.GsonInstance;

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
    final List<ExecutableMember> specs;
    try (BufferedReader file = Files.newBufferedReader(Paths.get(jsonFile))) {
      Type collectionType = new TypeToken<Collection<ExecutableMember>>() {}.getType();
      specs = GsonInstance.gson().fromJson(file, collectionType);
    }

    int pre = 0, post = 0, exc = 0;

    for (ExecutableMember method : specs) {
      if (method.getTargetClass().equals(method.getDeclaringClass().getQualifiedName())
          || !Modifier.isPrivate(method.getExecutable().getModifiers())) {
        pre +=
            method
                .paramTags()
                .stream()
                .map(t -> t.getCondition())
                .filter(c -> !c.isEmpty())
                .count();
        exc +=
            method
                .throwsTags()
                .stream()
                .map(t -> t.getCondition())
                .filter(c -> !c.isEmpty())
                .count();
        ReturnTag returnTag = method.returnTag();
        if (returnTag != null) {
          final String condition = returnTag.getCondition();
          if (!condition.isEmpty()) {
            post += 1;
          }
        }
      }
    }

    System.out.println("PRE " + pre);
    System.out.println("POST " + post);
    System.out.println("EXC " + exc);
  }
}
