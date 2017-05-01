package org.toradocu;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    PrecisionRecallCommonsCollections4.class,
    PrecisionRecallCommonsMath3.class,
    PrecisionRecallGuava19.class,
    PrecisionRecallJGraphT.class,
    PrecisionRecallPlumeLib.class
})
public class PrecisionRecallPaper {

}
