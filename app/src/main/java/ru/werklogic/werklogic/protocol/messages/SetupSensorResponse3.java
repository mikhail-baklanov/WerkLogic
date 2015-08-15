package ru.werklogic.werklogic.protocol.messages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.werklogic.werklogic.protocol.data.HardwareSensorInfo;
import ru.werklogic.werklogic.protocol.data.IResponseData;
import ru.werklogic.werklogic.protocol.utils.Utils;

public class SetupSensorResponse3 implements IResponseMessage {

    private byte sensorNumber;
    private Pattern pattern;

    public SetupSensorResponse3(byte sensorNumber) {
        this.sensorNumber = sensorNumber;
        this.pattern = Pattern.compile(("" + (100 + sensorNumber)).substring(1) +
                "\\s+\\w+\\s+(\\d+)\\s+([HL])\\s+0");
    }

    @Override
    public IResponseData readFrom(byte[] bytes) {
        if (Utils.findCR(bytes)) {
            String s = Utils.bytes2string(bytes);
            Matcher matcher = pattern.matcher(s);
            if (matcher.find()) {
                return new HardwareSensorInfo(sensorNumber, Integer.parseInt(matcher.group(1)) /* button */, "H".equals(matcher.group(2)) /* battery High/Low */);
            }
        }
        return null;
    }

}
