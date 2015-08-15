package ru.werklogic.werklogic.protocol.messages;

import java.io.IOException;
import java.io.OutputStream;

import ru.werklogic.werklogic.protocol.utils.Utils;

public class SetupSensorRequest1 implements IRequestMessage {

    private byte sensorNumber;

    public SetupSensorRequest1(byte sensorNumber) {
        this.sensorNumber = sensorNumber;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        String data = "P" + ("" + (100 + sensorNumber)).substring(1);
        ru.werklogic.werklogic.utils.Utils.log("Посылка команды " + data + " в устройство");
        out.write(Utils.string2bytes(data + "\r"));
    }

    @Override
    public IResponseMessage getResponseMessage() {
        return new SetupSensorResponse1();
    }

}
