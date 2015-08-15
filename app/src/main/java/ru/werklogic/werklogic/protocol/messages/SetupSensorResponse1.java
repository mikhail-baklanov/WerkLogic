package ru.werklogic.werklogic.protocol.messages;

import ru.werklogic.werklogic.protocol.data.IResponseData;
import ru.werklogic.werklogic.protocol.data.EmptyResponse;
import ru.werklogic.werklogic.protocol.utils.Utils;

public class SetupSensorResponse1 implements IResponseMessage {

  @Override
  public IResponseData readFrom(byte[] bytes) {
    if (Utils.findCR(bytes)){
      if (Utils.bytes2string(bytes).startsWith("03:")) {
        return new EmptyResponse();
      }
    }
    return null;
  }

}
