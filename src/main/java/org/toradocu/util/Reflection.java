package org.toradocu.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.Toradocu;
import org.toradocu.extractor.Parameter;
import org.toradocu.extractor.Type;

public class Reflection {

  private static final Logger log = LoggerFactory.getLogger(Reflection.class);

  /** Makes constructor private to prevent the instantiation of this class objects. */
  private Reflection() {}

  /**
   * Returns the {@code Class} object for the class with the given name or null if the class could
   * not be retrieved.
   *
   * @param className the fully qualified name of a class
   * @return the {@code Class} object for the given class or null if the class cannot be found
   */
  public static Class<?> getClass(String className) {
    try {
      return getClassLoader().loadClass(className);
    } catch (ClassNotFoundException e) {
      log.error("Unable to load class " + className + ". Check the classpath.");
      // TODO Does it make sense to continue or rather would be better to stop the execution?
      return null;
    }
  }

  /**
   * Check the type of all the specified parameters. Returns true if all the specified {@code
   * parameters} have the types specified in the array {@code types}.
   *
   * @param parameters the parameters whose type has to be checked
   * @param types the types that the parameters should have
   * @return true if all the specified {@code parameters} have the types specified in the array
   *     {@code types}. False otherwise.
   */
  public static boolean checkTypes(Parameter[] parameters, Class<?>[] types) {
    if (types.length != parameters.length) {
      return false;
    }

    for (int i = 0; i < types.length; i++) {
      final Type parameterType = parameters[i].getType();
      if (parameterType.isArray() && types[i].isArray()) {
        // Build the parameter type name (the name encodes the dimension of the array)
        String parameterTypeName = "L" + parameterType.getComponentType().getQualifiedName();
        for (int j = 0; j < parameterType.dimension(); j++) {
          parameterTypeName = "[" + parameterTypeName;
        }
        // Compare the two type names
        if (parameterTypeName.equals(types[i].getComponentType().getName())) {
          return false;
        }
      } else {
        if (!parameterType.equalsTo(types[i])) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Returns a new class loader that load classes from paths specified with option --class-dir.
   *
   * @return a new class loader that load classes from paths specified with option --class-dir
   */
  private static ClassLoader getClassLoader() {
    List<String> binariesPaths =
        Toradocu.configuration == null
            ? Collections.emptyList()
            : Toradocu.configuration.getClassDir();
    URL[] urls = new URL[binariesPaths.size()];
    for (int i = 0; i < urls.length; i++) {
      try {
        urls[i] = Paths.get(binariesPaths.get(i)).toUri().toURL();
      } catch (MalformedURLException e) {
        // TODO Move this check in the configuration to validate the input from the beginning.
        // TODO Notice that we don't take any particular action if any provided path is wrong.
        log.error(
            "Impossible to load binaries from "
                + binariesPaths.get(i)
                + ". Check the correctness of the path provided with option --class-dir.",
            e);
      }
    }
    return URLClassLoader.newInstance(urls, ClassLoader.getSystemClassLoader());
  }
}
