package ru.werklogic.werklogic.protocol.messages;

import java.io.IOException;
import java.io.OutputStream;

import ru.werklogic.werklogic.protocol.utils.Utils;

/**
 * Created by bmw on 16.06.2015.
 */
public class ReadByteRequest implements IRequestMessage {

    private int address;

    public ReadByteRequest(int address) {
        this.address = address;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        String data = "R" + Utils.byte2hex(address);
        ru.werklogic.werklogic.utils.Utils.log("Посылка команды " + data + " в устройство");
        out.write(Utils.string2bytes(data + "\r"));
    }

    @Override
    public IResponseMessage getResponseMessage() {
        return new ReadByteResponse();
    }

}
