package org.toradocu;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  PrecisionRecallCommonsCollections4.class,
  PrecisionRecallGuava19.class,
  PrecisionRecallCommonsMath3.class,
  PrecisionRecallJGraphT.class
})
public class PrecisionRecallTestSuite {}
