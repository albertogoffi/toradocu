import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class Aspect_Template {

	public Object advice(ProceedingJoinPoint jp) throws Throwable {
		boolean expectedExceptionsComplete = false;
		String output = "Activated aspect: " + this.getClass().getName() + " (" + jp.getSourceLocation() + ")";
    	Object target = jp.getTarget();
        Object[] args = jp.getArgs();
        List<Class<?>> expectedExceptions = getExpectedExceptions(target, args);
        if (!expectedExceptions.isEmpty()) {
            try {
//            	System.err.print(output);
                jp.proceed(args);
            } catch (Throwable e) {
                if (expectedExceptionsComplete && !expectedExceptions.contains(e.getClass())) {
                	fail(output + " -> Failure: Unexpected exception thrown: " + e.getClass().getCanonicalName());
                } else {
                	System.err.println(output + " -> Success: Expected exception caught");
                    return null;
                }
            }
            // Case 3
            fail(output + " -> Failure: Expected exception not thrown. Expected exceptions were: " + getExpectedExceptionAsString(expectedExceptions));
        }
        // Case 1, case 2
        return jp.proceed(args);
	}
	
	private List<Class<?>> getExpectedExceptions(Object target, Object[] args) {
		List<Class<?>> expectedExceptions = new ArrayList<>();
	}
	
	private String getExpectedExceptionAsString(List<Class<?>> expectedExceptions) {
    	String listAsString = "";
    	for (Class<?> exception : expectedExceptions) {
    		listAsString += exception.getName() + " ";
    	}
    	return listAsString;
    }
}