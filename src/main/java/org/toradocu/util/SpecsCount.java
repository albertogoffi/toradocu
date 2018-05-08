package org.toradocu.util;

import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import org.toradocu.output.util.JsonOutput;
import org.toradocu.output.util.ReturnTagOutput;
import org.toradocu.output.util.TagOutput;

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
    final List<JsonOutput> specs;
    Path path = Paths.get(jsonFile);
    if (Files.isSymbolicLink(path)) {
      path = Files.readSymbolicLink(path);
    }

    try (BufferedReader file = Files.newBufferedReader(path)) {
      Type collectionType = new TypeToken<Collection<JsonOutput>>() {}.getType();
      specs = GsonInstance.gson().fromJson(file, collectionType);
    }

    int pre = 0, post = 0, exc = 0;

    for (JsonOutput output : specs) {
      pre +=
          output.paramTags.stream().map(TagOutput::getCondition).filter(c -> !c.isEmpty()).count();
      exc +=
          output.throwsTags.stream().map(TagOutput::getCondition).filter(c -> !c.isEmpty()).count();
      ReturnTagOutput returnTag = output.returnTag;
      if (returnTag != null) {
        final String condition = returnTag.getCondition();
        if (!condition.isEmpty()) {
          post += 1;
        }
      }
    }

    System.out.println("PRE " + pre);
    System.out.println("POST " + post);
    System.out.println("EXC " + exc);
  }
}
