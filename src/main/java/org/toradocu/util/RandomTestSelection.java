package org.toradocu.util;

import static java.util.stream.Collectors.toList;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.toradocu.extractor.JavadocExtractor;

public class RandomTestSelection {

  /**
   * Obtain all java source files in project root folder.
   *
   * @param projectRoot path to project root folder
   * @return number of java files in project root folder
   */
  private static List<Path> getJavaProjectSources(String projectRoot) {
    try {
      return Files.find(
              Paths.get(projectRoot),
              Integer.MAX_VALUE,
              (filePath, fileAttr) -> fileAttr.isRegularFile())
          .filter(f -> f.toFile().getName().endsWith(".java"))
          .collect(toList());
    } catch (IOException e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  /**
   * Given a list of java files, pick a random one satisfying the criteria - at least 5 Javadoc
   * tags, not considering: getters, setters, toString, equals and hashCode
   *
   * @param javaFiles list of path to java source files
   * @return the class randomly picked satisfying the criteria
   */
  private static Path pickRandomClass(List<Path> javaFiles) {
    JavadocExtractor extractor = new JavadocExtractor();
    List<CallableDeclaration<?>> methods;
    Path chosenClass = null;

    while (chosenClass == null) {
      int index = new Random().nextInt(javaFiles.size());
      String sourcePath = javaFiles.get(index).toString();
      if (sourcePath.contains("package-info.java")) {
        continue;
      }
      String className =
          sourcePath.substring(sourcePath.lastIndexOf("/") + 1, sourcePath.lastIndexOf("."));
      try {
        methods = extractor.getExecutables(className, sourcePath);
        int numberOfTags = 0;
        for (CallableDeclaration m : methods) {
          numberOfTags += findTagsForMethod(m);
        }
        if (numberOfTags > 4) {
          chosenClass = javaFiles.get(index);
        }

      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }

    return chosenClass;
  }

  /**
   * Count total number of classes satisfying the criteria - at least 5 Javadoc tags, not
   * considering: getters, setters, toString, equals and hashCode. i.e. the so-called "documented
   * classes
   *
   * @param javaFiles list of path to java source files
   * @return total number of documented classes
   */
  private static int countDocumented(List<Path> javaFiles) {
    JavadocExtractor extractor = new JavadocExtractor();
    List<CallableDeclaration<?>> methods;
    int count = 0;

    for (Path path : javaFiles) {
      int index = new Random().nextInt(javaFiles.size());
      String sourcePath = path.toString();
      if (sourcePath.contains("package-info.java")) {
        continue;
      }
      String className =
          sourcePath.substring(sourcePath.lastIndexOf("/") + 1, sourcePath.lastIndexOf("."));
      try {
        methods = extractor.getExecutables(className, sourcePath);
        int numberOfTags = 0;
        for (CallableDeclaration m : methods) {
          numberOfTags += findTagsForMethod(m);
        }
        if (numberOfTags > 4) {
          count++;
        }

      } catch (Exception e) {
        // ignore parse errors
      }
    }

    return count;
  }

  /**
   * Count Javadoc tags for a given member (CallableDeclaration) if not getter, setter, toString,
   * equals or hashCode
   *
   * @param sourceMember the member
   * @return total number of Javadoc tags
   */
  private static int findTagsForMethod(CallableDeclaration<?> sourceMember) {
    String name = sourceMember.getNameAsString();
    if (name.equals("equals")
        || name.equals("hashCode")
        || name.equals("toString")
        || name.startsWith("get")
        || name.startsWith("set")) {
      return 0;
    }

    final Optional<Javadoc> javadocOpt = sourceMember.getJavadoc();
    int count = 0;
    if (javadocOpt.isPresent()) {
      final Javadoc javadocComment = javadocOpt.get();
      final List<JavadocBlockTag> blockTags = javadocComment.getBlockTags();
      for (JavadocBlockTag blockTag : blockTags) {
        switch (blockTag.getType()) {
          case PARAM:
            count++;
            break;
          case RETURN:
            count++;
            break;
          case EXCEPTION:
          case THROWS:
            count++;
            break;
          default:
            // ignore other block tags
            break;
        }
      }
    }

    return count;
  }

  public static void main(String[] args) {
    final String COMMONSCOLLECTIONS_4_SRC =
        "src/test/resources/src/commons-collections4-4.1-src/src/main/java/";
    final String COMMONSMATH_3_SRC =
        "src/test/resources/src/commons-math3-3.6.1-src/src/main/java/";
    final String FREECOL_SRC = "src/test/resources/src/freecol-0.11.6/src/";
    final String GRAPHSTREAM_SRC = "src/test/resources/src/gs-core-1.3-sources/";
    final String GUAVA_19_SRC = "src/test/resources/src/guava-19.0-sources/";
    final String JGRAPHT_SRC = "src/test/resources/src/jgrapht-core-0.9.2-sources/";
    final String PLUMELIB_SRC = "src/test/resources/src/plume-lib-1.1.0/java/src/";

    String[] projects = {
      COMMONSCOLLECTIONS_4_SRC,
      COMMONSMATH_3_SRC,
      FREECOL_SRC,
      GRAPHSTREAM_SRC,
      GUAVA_19_SRC,
      JGRAPHT_SRC,
      PLUMELIB_SRC
    };

    System.out.println(countDocumented(getJavaProjectSources(args[0])));
  }
}
