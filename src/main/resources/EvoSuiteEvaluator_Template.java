
import static java.lang.Double.*;
import static java.lang.Math.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class EvoSuiteEvaluator_Template {
	private static final double SMALL_DISTANCE = 1;
	private static final double BIG_DISTANCE = 1E300;
	private static final double MED_DISTANCE = 1E300;

	private Object ___INTERNAL__receiverObjectID__ = null;
	private Object[] ___INTERNAL__args__ = null;
	private Object ___INTERNAL__retVal_ = null;

	public double test0() throws Exception {
		try {
			___INTERNAL__retVal_ = null;
			DistanceAlgo algo = populateCalculators_preconds();
			double d = calculateDistance(algo);
			// logDistanceAndParamsOnEvoSuiteConsole(d, "test0");
			return d;
		} catch (Throwable e) {
			logOnEvosuiteConsole("this.getClass().getName()" + ": failed with unexpected exception : " + e);
			int i = 0;
			for (StackTraceElement msg : e.getStackTrace()) {
				logOnEvosuiteConsole("this.getClass().getName()" + ":                                    " + msg);
				if (i++ >= 10) {
					break;
				}
			}
			return 1d;
		}
	}

	public double test1(java.lang.Object ___retval) throws Exception {
		try {
			___INTERNAL__retVal_ = ___retval;
			DistanceAlgo algo = populateCalculators_postconds();
			double d = calculateDistance(algo);
			// logDistanceAndParamsOnEvoSuiteConsole(d, "test1");
			return d;
		} catch (Throwable e) {
			logOnEvosuiteConsole("this.getClass().getName()" + ": failed with unexpected exception : " + e);
			int i = 0;
			for (StackTraceElement msg : e.getStackTrace()) {
				logOnEvosuiteConsole("this.getClass().getName()" + ":                                    " + msg);
				if (i++ >= 10) {
					break;
				}
			}
			return 1d;
		}
	}

	public String toString() {
		String s = "";
		return s;
	}

	/*
	 * instantiate the calculator as follows:
	 * 
	 * DistanceCalculationAlgo calculatorAlgo = distanceAlgoAs...
	 * calculatorAlgo.calculators.add(new ...); calculatorAlgo.calculators.add(new
	 * ...); ...
	 */
	private DistanceAlgo populateCalculators_preconds() {
		DistanceAlgo algo = computeDistanceWithConjunctiveAlgo(); // Conjunctive algorithm by default. Customize if
																	// needed
		return algo;
	}

	private DistanceAlgo populateCalculators_postconds() {
		DistanceAlgo algo = computeDistanceWithConjunctiveAlgo(); // Conjunctive algorithm by default. Customize if
																	// needed
		return algo;
	}

	private double calculateDistance(DistanceAlgo algo) {
		if (algo == null) {
			throw new RuntimeException("No distance algo selected");
		}
		return algo.calculate();
	}

	DistanceAlgo computeDistanceWithConjunctiveAlgo() {
		return new DistanceAlgo() {
			double init() {
				return 0;
			}

			double compose(double current, double other) {
				return current + other;
			}
		};
	}

	DistanceAlgo computeDistanceWithDisjunctiveAlgo() {
		return new DistanceAlgo() {
			double init() {
				return 1;
			}

			double compose(double current, double other) {
				return current * other;
			}
		};
	}

	private abstract class DistanceAlgo {
		final ArrayList<DistanceCalculator> calculators = new ArrayList<>();

		double calculate() {
			if (calculators.isEmpty()) {
				return 0d;
			}
			double d = init();
			for (DistanceCalculator c : calculators) {
				try {
					d = compose(d, normalize(c.calculate()));
				} catch (Throwable e) {
					d += 1d;
				}
			}
			if (d == 0.0d)
				System.out.println("0 distance");
			return d;
		}

		private double normalize(double val) {
			return Double.isFinite(val) ? val / (1d + val) : 1d;
		}

		abstract double init();

		abstract double compose(double dist1, double dist2);
	}

	private interface DistanceCalculator {
		double calculate();
	}

	private abstract class ConditionDistanceCalculator implements DistanceCalculator {
		public double calculate() {
			return ___INTERNAL__retVal_ instanceof Throwable ? BIG_DISTANCE
					: condition() ? 0 : isNaN(cdistance()) ? MED_DISTANCE : SMALL_DISTANCE + abs(cdistance());
			// ___INTERNAL__retVal_ is always null for preconditions (test0),
			// but it can be Throwable for postconditions (test1) if the method under
			// evaluation is throwing exceptions,
			// meaning that the postcondition cannot be evaluated
		}

		abstract boolean condition();

		abstract double cdistance();
	}

	private abstract class NegConditionDistanceCalculator extends ConditionDistanceCalculator {
		public double calculate() {
			return ___INTERNAL__retVal_ instanceof Throwable ? 0
					: !condition() ? 0 : isNaN(cdistance()) ? MED_DISTANCE : SMALL_DISTANCE + abs(cdistance());
			// ___INTERNAL__retVal_ is always null for preconditions (test0),
			// but it can be Throwable for postconditions(test1) if the method under
			// evaluation is throwing exceptions,
			// meaning that the postcondition cannot be evaluated
		}
	}

	private abstract class ExceptionDistanceCalculator implements DistanceCalculator {
		public double calculate() {
			return !(___INTERNAL__retVal_ instanceof Throwable) ? MED_DISTANCE
					: currentExceptionCanonicalName().equals(exceptionCanonicalName()) ? 0 : BIG_DISTANCE;
		}

		String currentExceptionCanonicalName() {
			if (___INTERNAL__retVal_ == null) {
				return null;
			}
			Class<?> classOfretVal_ = ___INTERNAL__retVal_.getClass();
			try {
				// Unbox classes that EvoSuite mocked, if any
				Class<?> class_OverrideMock = loadClassAtRuntime("org.evosuite.runtime.mock.OverrideMock");
				while (class_OverrideMock.isAssignableFrom(classOfretVal_)
						&& (classOfretVal_.getCanonicalName().startsWith("shaded.org.evosuite.runtime.mock.")
								|| classOfretVal_.getCanonicalName().startsWith("org.evosuite.runtime.mock."))) {
					classOfretVal_ = classOfretVal_.getSuperclass();
				}
			} catch (ClassNotFoundException e) {
				logOnEvosuiteConsole("this.getClass().getName()"
						+ ": failed to dynamically load class org.evosuite.runtime.mock.OverrideMock: " + e);
			}
			// logOnEvosuiteConsole(this.getClass().getName() + ": " + " canonicalName is "
			// + classOfretVal_.getCanonicalName() + ", orginally was " +
			// ___INTERNAL__retVal_.getClass().getCanonicalName());
			return classOfretVal_.getCanonicalName();
		}

		abstract String exceptionCanonicalName();
	}

	private abstract class NegExceptionDistanceCalculator extends ExceptionDistanceCalculator {
		public double calculate() {
			String currentExcpCanonicalName = currentExceptionCanonicalName();
			double d = ___INTERNAL__retVal_ != null && currentExcpCanonicalName.equals(exceptionCanonicalName())
					? SMALL_DISTANCE
					: isEvosuiteException(currentExcpCanonicalName) ? BIG_DISTANCE : 0;
			// logOnEvosuiteConsole(this.getClass().getName() + ": " + " then distacnce is "
			// + d);
			return d;
		}

		private boolean isEvosuiteException(String canonicalName) {
			return canonicalName != null && (canonicalName.startsWith("shaded.org.evosuite.runtime.")
					|| canonicalName.startsWith("org.evosuite.runtime."));
		}
	}

	// Utils

	private void logDistanceAndParamsOnEvoSuiteConsole(double d, String callerName) {
		logOnEvosuiteConsole(this.getClass().getName() + ": " + callerName + ": distance is " + d);
		logOnEvosuiteConsole(this.getClass().getName() + ": " + callerName + ": Retval is "
				+ (___INTERNAL__retVal_ == null ? "null" : ___INTERNAL__retVal_.getClass().getCanonicalName()));
		logOnEvosuiteConsole(this.getClass().getName() + ": " + callerName + ": Receiver is "
				+ (___INTERNAL__receiverObjectID__ == null ? "null"
						: ___INTERNAL__receiverObjectID__.getClass().getCanonicalName()));
		for (Object ___obj : ___INTERNAL__args__) {
			logOnEvosuiteConsole(this.getClass().getName() + ": " + callerName + ": Param is "
					+ (___obj == null ? "null" : ___obj.getClass().getCanonicalName()));
		}

	}

	private void logOnEvosuiteConsole(String msg) {
		// Execute LoggingUtils.getEvoLogger().info(msg);
		try {
			Class<?> class_LoggingUtils = loadClassAtRuntime("org.evosuite.utils.LoggingUtils");
			Method method_getEvoLogger = class_LoggingUtils.getMethod("getEvoLogger", new Class[] {});
			Object object_evoLogger = method_getEvoLogger.invoke(null, new Object[] {});
			Method evoLogger_info = object_evoLogger.getClass().getMethod("info", new Class[] { String.class });
			evoLogger_info.invoke(object_evoLogger, new Object[] { msg });
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	private Class<?> loadClassAtRuntime(String canonicalName) throws ClassNotFoundException {
		Class<?> clazz;
		try {
			clazz = Class.forName("shaded." + canonicalName);
		} catch (ClassNotFoundException e) {
			clazz = Class.forName(canonicalName);
		}
		return clazz;
	}
}
