package org.toradocu;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.toradocu.random.AccuracyRandomCommonsCollections4;
import org.toradocu.random.AccuracyRandomCommonsMath3;

@RunWith(Suite.class)
@SuiteClasses({
    AccuracyRandomCommonsCollections4.class,
    AccuracyRandomCommonsMath3.class,
//    PrecisionRecallFreeCol.class,
//    PrecisionRecallGuava19.class,
//    PrecisionRecallJGraphT.class,
//    PrecisionRecallPlumeLib.class
})
public class PrecisionRecallPaper {

}
