package org.toradocu.util;

import java.util.ArrayList;
import java.util.List;

public class FakeSourceBuilder {

  private List<String> conditions = new ArrayList<>();
  private List<String> arguments = new ArrayList<>();
  private List<String> imports = new ArrayList<>();
  private static FakeSourceBuilder instance = null;

  public static FakeSourceBuilder getInstance() {
    if (instance == null) {
      instance = new FakeSourceBuilder();
    }
    return instance;
  }

  public String buildSource() {
    StringBuilder fakeSource = new StringBuilder();

    for (String anImport : imports) {
      fakeSource.append("import ");
      fakeSource.append(anImport);
      fakeSource.append(";");
      fakeSource.append("\n");
    }
    fakeSource.append("public class GeneratedSpecs {");
    fakeSource.append("\n");
    fakeSource.append("public void foo (");
    fakeSource.append(String.join(",", arguments));
    fakeSource.append(") {");
    fakeSource.append("\n");
    for (String condition : conditions) {
      fakeSource.append("if(");
      fakeSource.append(condition);
      fakeSource.append(")");
      fakeSource.append("\n");
    }
    fakeSource.append("                return; } }");
    return fakeSource.toString();
  }

  public void addArgument(String type, String argument) {
    arguments.add(type + " " + argument);
  }

  public void addCondition(String condition) {
    conditions.add(condition);
  }

  public void addImport(String anImport) {
    imports.add(anImport);
  }
}
