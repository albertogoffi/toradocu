package org.toradocu.util;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;
import org.toradocu.extractor.JavadocExtractor;

/** Created by arianna on 07/08/17. */
public class RandomTestSelection {

  public static ArrayList<Path> getJavaProjectSources(String projectRoot) {
    ArrayList<Path> javaFiles = new ArrayList<Path>();

    try {
      Stream<Path> allProjectFiles =
          allProjectFiles =
              Files.find(
                  Paths.get(projectRoot),
                  Integer.MAX_VALUE,
                  (filePath, fileAttr) -> fileAttr.isRegularFile());

      allProjectFiles.forEach(
          item -> {
            if (item.toString().toLowerCase().endsWith(".java")) {
              javaFiles.add(item);
            }
          });

    } catch (IOException e) {
      e.printStackTrace();
    }

    return javaFiles;
  }

  public static Path pickRandomClass(ArrayList<Path> javaFiles) {
    JavadocExtractor extractor = new JavadocExtractor();
    List<CallableDeclaration<?>> methods = new ArrayList<CallableDeclaration<?>>();
    Path choosenClass = null;

    while (choosenClass == null) {
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
          choosenClass = javaFiles.get(index);
        }

      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }

    return choosenClass;
  }

  private static int findTagsForMethod(CallableDeclaration sourceMember) {
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

    for (String project : projects) {
      Path randomClass = pickRandomClass(getJavaProjectSources(project));
      String className =
          randomClass
              .toString()
              .replaceFirst(project, "")
              .replaceAll("/", ".")
              .replace(".java", "");

      System.out.println(className);
    }
  }
}
