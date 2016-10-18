package org.toradocu.util;

/**
 * This interface specifies that the implementing class is a builder for other class objects.
 *
 * @param <T> the type of object that this class builds
 */
public interface Builder<T> {
  /**
   * Build and return an instance of the class.
   *
   * @return an instance of the class built using this {@code Builder}
   */
  public T build();
}
