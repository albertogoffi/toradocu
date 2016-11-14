package org.toradocu.util;

import java.io.IOException;
import java.io.OutputStream;

/** This class is an output stream that does nothing. */
public class NullOutputStream extends OutputStream {

  /**
   * Does nothing with the given byte.
   *
   * @param b a byte that is ignored
   * @throws IOException never
   */
  @Override
  public void write(int b) throws IOException {
    // This method is intentionally empty.
  }
}
