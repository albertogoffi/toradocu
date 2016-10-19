package org.toradocu;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.TestCaseStats;

public class PrecisionRecallPlumeLib extends AbstractPrecisionRecallTestSuite {

  private static final String PLUMELIB_SRC = "src/test/resources/src/plume-lib-1.1.0/java/src";
  private static final String PLUMELIB_BIN = "src/test/resources/bin/plume.jar";
  private static final String PLUMELIB_EXPECTED_DIR = "src/test/resources/plume-lib-1.1.0/";

  public PrecisionRecallPlumeLib() {
    super(PLUMELIB_SRC, PLUMELIB_BIN, PLUMELIB_EXPECTED_DIR);
  }

  @Test
  public void arraysMDETest() throws Exception {
    test("plume.ArraysMDE", 1.0, 1.0);
  }
}
