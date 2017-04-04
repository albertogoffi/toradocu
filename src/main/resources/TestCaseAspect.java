import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class TestCaseAspect {

  @Around("execution(@org.junit.Test * *(..))")
  public Object advice(ProceedingJoinPoint jp) throws Throwable {
    try {
      return jp.proceed();
    } catch (InvalidParamException | ExpectedException e) {
      // NOP: The advised method has been invoked with an improper input or, as expected, throws an
      // exception. Ignore the test case (make it a pass).
      return null;
    }
  }

  public static class InvalidParamException extends IllegalArgumentException {}

  public static class ExpectedException extends RuntimeException {}
}
