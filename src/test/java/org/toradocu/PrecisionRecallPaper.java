package org.toradocu;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.toradocu.random.AccuracyRandomCommonsCollections4;
import org.toradocu.random.AccuracyRandomCommonsMath3;
import org.toradocu.random.AccuracyRandomFreeCol;
import org.toradocu.random.AccuracyRandomGraphStream;
import org.toradocu.random.AccuracyRandomGuava19;
import org.toradocu.random.AccuracyRandomJGraphT;
import org.toradocu.random.AccuracyRandomPlumeLib;

@RunWith(Suite.class)
@SuiteClasses({
    AccuracyRandomCommonsCollections4.class,
    AccuracyRandomCommonsMath3.class,
    AccuracyRandomFreeCol.class,
    AccuracyRandomGraphStream.class,
    AccuracyRandomGuava19.class,
    AccuracyRandomJGraphT.class,
    AccuracyRandomPlumeLib.class
g})
public class PrecisionRecallPaper {

}
