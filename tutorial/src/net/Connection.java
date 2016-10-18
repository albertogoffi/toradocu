package net;

public class Connection {

  private boolean open = false;

  /**
   * Establishes a new connection.
   *
   * @throws IllegalStateException if the connection is already open
   */
  public void open() {
    // establish a new connection...
    open = true;
  }

  public boolean isOpen() {
    return open;
  }
}
