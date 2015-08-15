package ru.werklogic.werklogic.protocol.messages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.werklogic.werklogic.protocol.data.IResponseData;
import ru.werklogic.werklogic.protocol.facade.ReadByteResponceData;
import ru.werklogic.werklogic.protocol.utils.Utils;

/**
 * Created by bmw on 16.06.2015.
 */
public class ReadByteResponse implements IResponseMessage {

    private Pattern pattern;

    public ReadByteResponse() {
        this.pattern = Pattern.compile("20:\\s*(\\w\\w)");
    }

    @Override
    public IResponseData readFrom(byte[] bytes) {
        if (Utils.findCR(bytes)) {
            String result = Utils.bytes2string(bytes);
            ru.werklogic.werklogic.utils.Utils.log("Получение строки " + result + " из устройства");
            if (result.startsWith("20:")) {
                Matcher matcher = pattern.matcher(result);
                if (matcher.find())
                    return new ReadByteResponceData(matcher.group(1));
            }
        }
        return null;
    }

}
