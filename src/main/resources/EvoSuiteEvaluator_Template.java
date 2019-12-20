

import static java.lang.Double.*;
import static java.lang.Math.*;

import java.util.ArrayList;

public class EvoSuiteEvaluator_Template {
    private static final double SMALL_DISTANCE = 1;
    private static final double BIG_DISTANCE = 1E300;
    
    private Object receiverObjectID = null;
    private Object[] args = null;

    public double test0() throws Exception {
       	final ArrayList<ValueCalculator> calculators = new ArrayList<>();
       	populateCalculators(calculators);
       	
        double d = 0d; 	
		for (ValueCalculator vc: calculators) {
        	try {
        		d += normalize(vc.calculate());
        	} catch (Throwable e) {
        		d += 1d;
        	}
        }
        if (d == 0.0d)
            System.out.println("test0 0 distance");
        return d;
    }
    private static abstract class ValueCalculator {
        double calculate() {
        	return condition() ? 0 : isNaN(cdistance()) ? BIG_DISTANCE : SMALL_DISTANCE + abs(cdistance());
        }
        abstract boolean condition();
        abstract double cdistance();
    }
    private double normalize(double val) {
    	return val / (1 + val);
    }
	private void populateCalculators(ArrayList<ValueCalculator> calculators) {
	}
}
