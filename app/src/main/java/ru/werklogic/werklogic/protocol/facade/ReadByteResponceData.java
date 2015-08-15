package ru.werklogic.werklogic.protocol.facade;

import ru.werklogic.werklogic.protocol.data.IResponseData;

/**
 * Created by bmw on 16.06.2015.
 */
public class ReadByteResponceData implements IResponseData {
    private String hexes;

    public ReadByteResponceData(String hexes) {
        if (hexes == null || hexes.length() != 2)
            throw new RuntimeException("Передано неверное хначение в конструктор ReadByteResponceData. Ожидается 2-х символьное шестнадцатеричное представление");
        this.hexes = hexes;
    }

    public String getHexes() {
        return hexes;
    }
}
