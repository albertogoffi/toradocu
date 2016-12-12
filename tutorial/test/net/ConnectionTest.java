package net;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConnectionTest {

  @Test
  public void open() throws Exception {
    Connection connection = new Connection();
    connection.open();
    connection.open();
  }

  @Test
  public void send() throws Exception {
    Connection connection = new Connection();
    connection.open();
    connection.send(null);
  }
}
