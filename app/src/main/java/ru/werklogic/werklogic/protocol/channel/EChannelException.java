package ru.werklogic.werklogic.protocol.channel;

public class EChannelException extends Exception {

  private static final long serialVersionUID = 1L;

  public EChannelException() {
  }

  public EChannelException(String message) {
    super(message);
  }

  public EChannelException(Throwable cause) {
    super(cause);
  }

  public EChannelException(String message, Throwable cause) {
    super(message, cause);
  }

}
