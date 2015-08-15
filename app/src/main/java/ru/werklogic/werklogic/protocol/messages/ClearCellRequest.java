package ru.werklogic.werklogic.protocol.messages;

import java.io.IOException;
import java.io.OutputStream;

import ru.werklogic.werklogic.protocol.utils.Utils;

/**
 * Created by bmw on 17.06.2015.
 */
public class ClearCellRequest implements IRequestMessage {
    private byte sensorNumber;

    public ClearCellRequest(byte sensorNumber) {
        this.sensorNumber = sensorNumber;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        String data = "C" + ("" + (100 + sensorNumber)).substring(1);
        ru.werklogic.werklogic.utils.Utils.log("Посылка команды " + data + " в устройство");
        out.write(Utils.string2bytes(data + "\r"));
    }

    @Override
    public IResponseMessage getResponseMessage() {
        return new ClearCellResponse();
    }

}
