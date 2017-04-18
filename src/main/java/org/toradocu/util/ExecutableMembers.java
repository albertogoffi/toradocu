package org.toradocu.util;

/**
 * Given the fully qualified name of a class, this program prints the number of public constructors
 * and methods of the given class. Note that package-private and protected executable members are
 * ignored.
 */
public class ExecutableMembers {

  public static void main(String[] args) {
    if (args.length != 1) {
      throw new IllegalArgumentException("Please provide a fully qualified name of a class.");
    }

    try {
      // We assume to have the class to load in the classpath.
      Class<?> clazz = Class.forName(args[0]);
      int members = clazz.getConstructors().length + clazz.getMethods().length;
      System.out.println(members);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Unable to load class " + args[0], e);
    }
  }
}
