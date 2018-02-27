package example;

public enum AnEnum {
  FOO, BAR, BAZ;

  /**
   * @param num must not be null
   * @return true if num is even, false otherwise
   */
  public boolean isEven(Integer num) {
    return num % 2 == 0;
  }
}
