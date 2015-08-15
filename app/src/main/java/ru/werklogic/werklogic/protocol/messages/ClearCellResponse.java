package ru.werklogic.werklogic.protocol.messages;

import ru.werklogic.werklogic.protocol.data.EmptyResponse;
import ru.werklogic.werklogic.protocol.data.IResponseData;
import ru.werklogic.werklogic.protocol.utils.Utils;

/**
 * Created by bmw on 17.06.2015.
 */
public class ClearCellResponse implements IResponseMessage {
    @Override
    public IResponseData readFrom(byte[] bytes) {
        if (Utils.findCR(bytes)){
            if (Utils.bytes2string(bytes).startsWith("02")) {
                return new EmptyResponse();
            }
        }
        return null;
    }
}
