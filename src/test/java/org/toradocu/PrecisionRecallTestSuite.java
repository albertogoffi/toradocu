package org.toradocu;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  PrecisionRecallGuava19.class,
  PrecisionRecallCommonsCollections4.class,
  PrecisionRecallJGraphT.class
})
public class PrecisionRecallTestSuite {}
