package ru.werklogic.werklogic.protocol.messages;

import java.io.IOException;
import java.io.OutputStream;

public interface IRequestMessage {

  void writeTo(OutputStream out) throws IOException;
  IResponseMessage getResponseMessage();
  
}
