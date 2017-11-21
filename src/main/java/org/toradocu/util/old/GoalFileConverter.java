package org.toradocu.util.old;

import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.TypeVariable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import org.toradocu.Toradocu;
import org.toradocu.conf.Configuration;
import org.toradocu.extractor.ExecutableMember;
import org.toradocu.extractor.ThrowsTag;
import org.toradocu.util.GsonInstance;

/** Converts JSON goal files into the old format used by Toradocu v 0.1. */
public class GoalFileConverter {

  public static void main(String[] args) throws Exception {
    if (args.length < 3) {
      System.out.println("This program must be invoked with the following parameters:");
      System.out.println("1) Path to the file to be converted.");
      System.out.println("2) Path to the file where to save the conversion.");
      System.out.println(
          "3) Path to the binaries of the class for which conditions have to be " + "converted.");
      System.out.println(
          "4) OPTIONAL. Path to the expected output file. Used to check the "
              + "correctness of the conversion.");
      System.exit(1);
    }

    // Path to the JSON goal file to convert.
    final String inputFilePath = args[0];
    // Path where to save the converted goal file in plain text format.
    final String outputFilePath = args[1];
    // Path to the binaries from which load information with reflection.
    final String binPath = args[2];

    // We use Toradocu code to load reflection information.
    Toradocu.configuration = new Configuration();
    Toradocu.configuration.classDirs = binPath;

    java.lang.reflect.Type listType = new TypeToken<List<ExecutableMember>>() {}.getType();

    try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputFilePath));
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFilePath))) {

      List<ExecutableMember> methods = GsonInstance.gson().fromJson(reader, listType);
      for (ExecutableMember method : methods) {
        final Executable executable = method.getExecutable();
        if (executable == null) {
          System.err.println("Reflection error: Impossible to load method " + method.getName());
          continue;
        }

        // Ignore private and inherited executable members.
        if (Modifier.isPrivate(executable.getModifiers())
            || executable.isSynthetic()
            || !method.getTargetClass().equals(method.getDeclaringClass().getQualifiedName())) {
          continue;
        }

        StringBuilder methodId = new StringBuilder(methodIdOf(executable));
        for (ThrowsTag throwsTag : method.throwsTags()) {
          String commentTranslation =
              methodId
                  + " throws "
                  + throwsTag.exceptionType().getQualifiedName()
                  + " "
                  + throwsTag.getComment()
                  + " ==> ["
                  + throwsTag.getCondition().replace(" ", "")
                  + "]";
          writer.write(commentTranslation);
          writer.newLine();
        }
      }
      System.out.println("Output file: " + outputFilePath);
    }

    if (args.length > 3) {
      String expectedOutputFile = args[3];
      if (!isConversionCorrect(outputFilePath, expectedOutputFile)) {
        throw new Exception(
            "Conversion is not correct. "
                + outputFilePath
                + " different than "
                + expectedOutputFile);
      }
    }
  }

  private static boolean isConversionCorrect(String actualOutputFile, String expectedOutputFile)
      throws IOException {
    final List<String> actualResult = Files.readAllLines(Paths.get(actualOutputFile));
    final List<String> expectedResult = Files.readAllLines(Paths.get(expectedOutputFile));

    boolean correct = true;

    if (actualResult.size() != expectedResult.size()) {
      System.out.println(
          "Conversion has a number of lines ("
              + actualResult.size()
              + ") "
              + "different than expected ("
              + expectedResult.size()
              + ")");
      correct = false;
    }

    // Sort results so that the order does not influence the comparison.
    actualResult.sort(String::compareTo);
    expectedResult.sort(String::compareTo);

    for (int i = 0; i < actualResult.size(); i++) {
      String actual = actualResult.get(i);
      String expected = expectedResult.get(i);
      actual = actual.substring(0, actual.lastIndexOf("==>"));
      expected = expected.substring(0, expected.lastIndexOf("==>"));

      if (!Objects.equals(actual, expected)) {
        System.out.println("\nConverted result is different than expected:");
        System.out.println("ACTUAL: " + actual);
        System.out.println("EXPECTED: " + expected);

        System.out.println("\nACTUAL LIST:");
        actualResult.forEach(System.out::println);
        System.out.println("\nEXPECTED LIST:");
        expectedResult.forEach(System.out::println);

        return false;
      }
    }

    return correct;
  }

  private static String methodIdOf(Executable member) {
    StringBuilder methodId = new StringBuilder();
    if (member instanceof Method) {
      methodId.append(member.getDeclaringClass().getName()).append(".");
    }

    // Type parameters handling.
    methodId.append(typeArgumentOf(member));

    // Name.
    methodId.append(member.getName()).append("(");

    // Parameters handling.
    methodId.append(parametersOf(member));

    methodId.append(")");

    return methodId.toString();
  }

  private static String parametersOf(Executable member) {
    StringJoiner paramsJoiner = new StringJoiner(", ");

    for (Parameter parameter : member.getParameters()) {
      String parameterizedType = parameter.getParameterizedType().toString();
      if (parameterizedType.contains("<")) {
        if (parameter.isVarArgs() && parameterizedType.endsWith("[]")) {
          parameterizedType = parameterizedType.substring(0, parameterizedType.lastIndexOf("["));
          parameterizedType += "...";
        }
        paramsJoiner.add(parameterizedType);
      } else {
        final String param = parameter.toString();
        String paramType = param.substring(0, param.lastIndexOf(" "));
        paramType = paramType.replace("$", ".");
        paramsJoiner.add(paramType);
      }
    }
    return paramsJoiner.toString();
  }

  private static String typeArgumentOf(Executable member) {
    StringJoiner joiner = new StringJoiner(", ", "<", ">");
    joiner.setEmptyValue("");
    for (TypeVariable typeParameter : member.getTypeParameters()) {
      StringBuilder typeParameterName = new StringBuilder(typeParameter.getName());

      for (java.lang.reflect.Type bound : typeParameter.getBounds()) {
        String boundName = bound.getTypeName();
        if (!boundName.equals("java.lang.Object")) {
          typeParameterName.append(" extends ").append(boundName);
        }
      }

      joiner.add(typeParameterName.toString());
    }
    return joiner.toString();
  }
}
