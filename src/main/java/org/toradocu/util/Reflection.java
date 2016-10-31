package org.toradocu.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.Toradocu;
import org.toradocu.extractor.Parameter;
import org.toradocu.extractor.Type;

public class Reflection {

  private static URLClassLoader classLoader;
  private static final Logger log = LoggerFactory.getLogger(Reflection.class);

  /**
   * Returns the {@code Class} object for the class with the given name or null if the class could
   * not be retrieved.
   *
   * @param className the fully qualified name of a class
   * @return the {@code Class} object for the given class or null if the class cannot be found
   */
  public static Class<?> getClass(String className) {
    final String ERROR_MESSAGE = "Unable to load class " + className + ". Check the classpath.";
    try {
      URL classDir = Toradocu.configuration.getClassDir().toUri().toURL();
      if (classLoader == null) {
        classLoader = URLClassLoader.newInstance(new URL[] {classDir});
      } else {
        URL[] originalURLs = classLoader.getURLs();
        URL[] newURLs = new URL[originalURLs.length + 1];
        for (int i = 0; i < originalURLs.length; i++) {
          newURLs[i] = originalURLs[i];
        }
        newURLs[newURLs.length - 1] = classDir;
        classLoader = URLClassLoader.newInstance(newURLs);
      }
      return classLoader.loadClass(className);
    } catch (MalformedURLException | ClassNotFoundException e) {
      log.error(ERROR_MESSAGE);
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
}
