import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class Aspect_Template {

  public Object advice(ProceedingJoinPoint jp) throws Throwable {
    String output =
        "Triggered aspect: " + this.getClass().getName() + " (" + jp.getSourceLocation() + ")";
    Object target = jp.getTarget();
    Object[] args = jp.getArgs();

    if (!paramTagsSatisfied(target, args)) {
      System.err.println(output + " -> Ignored test case: inputs violate pre-conditions");
      throw new TestCaseAspect.InvalidParamException();
    } else {
      List<Class<?>> expectedExceptions = getExpectedExceptions(target, args);
      if (expectedExceptions.isEmpty()) {
        Object result = jp.proceed(args);
        return checkResult(result, target, args);
      } else {
        try {
          jp.proceed(args);
        } catch (Throwable e) {
          if (!expectedExceptions.contains(e.getClass())) {
            fail(
                output
                    + " -> Failure: Unexpected exception thrown: "
                    + e.getClass().getCanonicalName());
          } else {
            System.err.println(output + " -> Success: Expected exception caught");
            throw new TestCaseAspect.ExpectedException();
          }
        }
      }
      fail(
          output
              + " -> Failure: Expected exception not thrown. Expected exceptions were: "
              + getExpectedExceptionAsString(expectedExceptions));
      return null;
    }
  }

  private boolean paramTagsSatisfied(Object target, Object[] args) {}

  private Object checkResult(Object result, Object target, Object[] args) {}

  private List<Class<?>> getExpectedExceptions(Object target, Object[] args) {
    List<Class<?>> expectedExceptions = new ArrayList<Class<?>>();
  }

  private String getExpectedExceptionAsString(List<Class<?>> expectedExceptions) {
    String listAsString = "";
    for (Class<?> exception : expectedExceptions) {
      listAsString += exception.getName() + " ";
    }
    return listAsString;
  }
}
