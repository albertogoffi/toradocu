package example;

public class VariablesComparison {

  public VariablesComparison() {
    super();
  }

  /** @return true if first=second */
  public boolean returnEqOnlyLetters(int first, int second) {
    return first == second;
  }

  /** @return true if v1=v2 */
  public boolean returnEqLettersNumbers(int v1, int v2) {
    return v1 == v2;
  }

  /** @return true if v1>=v2 */
  public boolean returnGELettersNumbers(int v1, int v2) {
    return v1 >= v2;
  }

  /** @return true if v1 is smaller than v2 */
  public boolean returnLTLettersNumbers(int v1, int v2) {
    return v1 < v2;
  }

  /**
   * @param v1 must be >= v2
   * @param v2
   * @return difference between v1 and v2
   */
  public int paramGELettersNumbers(int v1, int v2) {
    return v1 - v2;
  }

  /**
   * @param first
   * @param second must be smaller than first
   * @return difference between first and second
   */
  public int paramLTLetters(int first, int second) {
    return first - second;
  }

  /** @throws IllegalArgumentException if v2 > v1 */
  public int throwsGTLettersNumbers(int v1, int v2) {
    return v1 - v2;
  }

  /** @throws IllegalArgumentException if first is smaller than second */
  public int throwsLTLetters(int first, int second) {
    return first - second;
  }
}
