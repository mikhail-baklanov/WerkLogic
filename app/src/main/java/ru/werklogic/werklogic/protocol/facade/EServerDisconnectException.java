package ru.werklogic.werklogic.protocol.facade;

public class EServerDisconnectException extends Exception {

  private static final long serialVersionUID = 1L;

  public EServerDisconnectException() {
  }

  public EServerDisconnectException(String message) {
    super(message);
  }

  public EServerDisconnectException(Throwable cause) {
    super(cause);
  }

  public EServerDisconnectException(String message, Throwable cause) {
    super(message, cause);
  }

}
