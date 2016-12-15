package net;

/** A network connection. */
public class Connection {

  private boolean open = false;

  /** @returns true if the connection is open, false otherwise */
  public boolean isOpen() {
    return open;
  }

  /**
   * Establishes a new connection.
   *
   * @throws IllegalStateException if the connection is already open
   */
  public void open() {
    // Establish a new connection.

    // ... code elided ...

    open = true;
  }

  /**
   * Sends a message.
   *
   * @throws NullPointerException if message is null
   */
  public void send(String message) {
    // Optimization: no action if message is empty.
    if (message.isEmpty()) {
      return;
    }

    // Send the message.

    // ... code elided ...
  }
}
