package org.toradocu.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.toradocu.conf.Configuration;

public class Reflection {

  /** Logger of this class. */
  private static org.slf4j.Logger log = LoggerFactory.getLogger(Reflection.class);

  private static final Map<String, Class> primitiveClasses = initializePrimitivesMap();

  private static Map<String, Class> initializePrimitivesMap() {
    Map<String, Class> map = new HashMap<>(9);
    map.put("int", Integer.TYPE);
    map.put("long", Long.TYPE);
    map.put("double", Double.TYPE);
    map.put("float", Float.TYPE);
    map.put("bool", Boolean.TYPE);
    map.put("char", Character.TYPE);
    map.put("byte", Byte.TYPE);
    map.put("void", Void.TYPE);
    map.put("short", Short.TYPE);
    return map;
  }

  /** Makes constructor private to prevent the instantiation of this class objects. */
  private Reflection() {}

  /**
   * Returns the {@code Class} object for the class with the given name or null if the class could
   * not be retrieved.
   *
   * @param className the fully qualified name of a class
   * @return the {@code Class} object for the given class
   * @throws ClassNotFoundException if class {@code className} cannot be loaded
   */
  public static Class<?> getClass(String className) throws ClassNotFoundException {
    if (primitiveClasses.containsKey(className)) {
      return primitiveClasses.get(className);
    }

    // The order here is important. We have to first look in the paths specified by the user and
    // then in the default class path. The default classpath contains the dependencies of Toradocu
    // that could clash with the system under analysis.
    final List<URL> urls = Configuration.INSTANCE.classDirs;
    final URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[urls.size()]), null);
    try {
      return loader.loadClass(className);
    } catch (ClassNotFoundException e) {
      return Class.forName(className);
    }
  }

  //  /**
  //   * Check the type of all the specified parameters. Returns true if all the specified {@code
  //   * parameters} have the types specified in the array {@code types}.
  //   *
  //   * @param parameters the parameters whose type has to be checked
  //   * @param types the types that the parameters should have
  //   * @return true if all the specified {@code parameters} have the types specified in the array
  //   *     {@code types}. False otherwise.
  //   */
  //  public static boolean checkTypes(Parameter[] parameters, Class<?>[] types) {
  //    if (types.length != parameters.length) {
  //      return false;
  //    }
  //
  //    for (int i = 0; i < types.length; i++) {
  //      final Type parameterType = parameters[i].getType();
  //      if (parameterType.isArray() && types[i].isArray()) {
  //        // Build the parameter type name (the name encodes the dimension of the array)
  //        String parameterTypeName = "L" + parameterType.getComponentType().getQualifiedName();
  //        for (int j = 0; j < parameterType.dimension(); j++) {
  //          parameterTypeName = "[" + parameterTypeName;
  //        }
  //        // Compare the two type names
  //        if (parameterTypeName.equals(types[i].getComponentType().getName())) {
  //          return false;
  //        }
  //      } else {
  //        if (!parameterType.equalsTo(types[i])) {
  //          return false;
  //        }
  //      }
  //    }
  //    return true;
  //  }
}
