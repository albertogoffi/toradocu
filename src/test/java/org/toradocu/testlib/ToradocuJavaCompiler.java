package org.toradocu.testlib;

import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class ToradocuJavaCompiler {

  /**
   * Compiles the given Java files using the system Java compiler.
   *
   * @param files Java files to compile
   * @return true if compilation does not produce any compilation error, false otherwise
   */
  public static boolean run(List<String> files) {
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    final CompilationTask task = createCompilationTask(diagnostics, files);
    boolean compilationOK = task.call();
    if (!compilationOK) {
      printCompilationErrors(diagnostics);
    }
    return compilationOK;
  }

  private static CompilationTask createCompilationTask(
      DiagnosticCollector<JavaFileObject> diagnostics, List<String> files) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
    final Iterable<? extends JavaFileObject> compilationUnit =
        fileManager.getJavaFileObjectsFromStrings(files);

    return compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnit);
  }

  private static void printCompilationErrors(DiagnosticCollector<JavaFileObject> diagnostics) {
    for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
      System.err.format(
          "Error on line %d in %s:%n%s%n",
          diagnostic.getLineNumber(),
          diagnostic.getSource().toString(),
          diagnostic.getMessage(Locale.getDefault()));
    }
  }
}
