import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.fail;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class Aspect_1 {

    @Around("call(com.google.common.collect.ArrayListMultimap com.google.common.collect.ArrayListMultimap.create(int, int))")
    public Object advice(ProceedingJoinPoint jp) throws Throwable {
        boolean expectedExceptionsComplete = false;
        String output = "Triggered aspect: " + this.getClass().getName() + " (" + jp.getSourceLocation() + ")";
        Object target = jp.getTarget();
        Object[] args = jp.getArgs();
        List<Class<?>> expectedExceptions = getExpectedExceptions(target, args);
        if (!expectedExceptions.isEmpty()) {
            try {
                jp.proceed(args);
            } catch (Throwable e) {
                if (!expectedExceptions.contains(e.getClass())) {
                    fail(output + " -> Failure: Unexpected exception thrown: " + e.getClass().getCanonicalName());
                } else {
                    System.err.println(output + " -> Success: Expected exception caught");
                    return null;
                }
            }
            fail(output + " -> Failure: Expected exception not thrown. Expected exceptions were: " + getExpectedExceptionAsString(expectedExceptions));
        }
        return jp.proceed(args);
    }

    private List<Class<?>> getExpectedExceptions(Object target, Object[] args) {
        List<Class<?>> expectedExceptions = new ArrayList<Class<?>>();
        try {
            // @throws java.lang.IllegalArgumentException if expectedKeys or expectedValuesPerKey is negative
            if (((int) args[0]) < 0 || ((int) args[1]) < 0) {
                try {
                    expectedExceptions.add(Class.forName("java.lang.IllegalArgumentException"));
                } catch (ClassNotFoundException e) {
                    System.err.println("Class not found!" + e);
                }
            }
        } catch (java.lang.NullPointerException e) {
        }
        return expectedExceptions;
    }

    private String getExpectedExceptionAsString(List<Class<?>> expectedExceptions) {
        String listAsString = "";
        for (Class<?> exception : expectedExceptions) {
            listAsString += exception.getName() + " ";
        }
        return listAsString;
    }
}
