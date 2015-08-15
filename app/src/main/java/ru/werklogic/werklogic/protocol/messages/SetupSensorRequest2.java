package ru.werklogic.werklogic.protocol.messages;

import java.io.IOException;
import java.io.OutputStream;

public class SetupSensorRequest2 implements IRequestMessage {

  @Override
  public void writeTo(OutputStream out) throws IOException {
  }

  @Override
  public IResponseMessage getResponseMessage() {
    return new SetupSensorResponse2();
  }

}
