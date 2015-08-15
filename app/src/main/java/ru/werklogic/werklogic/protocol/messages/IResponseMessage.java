package ru.werklogic.werklogic.protocol.messages;

import ru.werklogic.werklogic.protocol.data.IResponseData;

public interface IResponseMessage {

  IResponseData readFrom(byte[] bytes);

}
