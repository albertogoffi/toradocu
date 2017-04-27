package org.toradocu.util;

import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Given the fully qualified name of a class, this program prints the number of constructors and
 * methods of the given class. Note that synthetic and private executable members are ignored.
 */
public class ExecutableMembers {

  public static void main(String[] args) {
    if (args.length != 1) {
      throw new IllegalArgumentException("Please provide a fully qualified name of a class.");
    }

    Class<?> clazz;
    try {
      // We assume to have the class to load in the classpath.
      clazz = Class.forName(args[0]);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Unable to load class " + args[0], e);
    }
    List<Executable> members = new ArrayList<>();
    Collections.addAll(members, clazz.getDeclaredConstructors());
    Collections.addAll(members, clazz.getDeclaredMethods());
    // Ignore synthetic and private executable members.
    members.removeIf(m -> Modifier.isPrivate(m.getModifiers()) || m.isSynthetic());
    System.out.println(members.size());
  }
}
