package ru.werklogic.werklogic.protocol.messages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.werklogic.werklogic.protocol.data.HardwareSensorInfo;
import ru.werklogic.werklogic.protocol.data.IResponseData;
import ru.werklogic.werklogic.protocol.utils.Utils;

public class SensorEvent implements IResponseMessage {

  private Pattern pattern;

  public SensorEvent() {
    this.pattern = Pattern.compile("(\\d\\d)\\s+\\w+\\s+(\\d+)\\s+([HL])\\s+0");
  }

  @Override
  public IResponseData readFrom(byte[] bytes) {
    if (Utils.findCR(bytes)) {
      Matcher matcher = pattern.matcher(Utils.bytes2string(bytes));
      if (matcher.find()) {
        return new HardwareSensorInfo((byte)Integer.parseInt(matcher.group(1)) /* sensor number */, Integer.parseInt(matcher.group(2)) /* button */, "H".equals(matcher.group(3)) /* battery High/Low */);
      }
    }
    return null;
  }

}
