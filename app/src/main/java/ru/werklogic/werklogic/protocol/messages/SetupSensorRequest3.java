package ru.werklogic.werklogic.protocol.messages;

import java.io.IOException;
import java.io.OutputStream;

public class SetupSensorRequest3 implements IRequestMessage {

  private byte sensorNumber;

  public SetupSensorRequest3(byte sensorNumber) {
    this.sensorNumber = sensorNumber;
  }

  @Override
  public void writeTo(OutputStream out) throws IOException {
  }

  @Override
  public IResponseMessage getResponseMessage() {
    return new SetupSensorResponse3(sensorNumber);
  }

}
