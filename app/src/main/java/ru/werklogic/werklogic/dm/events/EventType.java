package ru.werklogic.werklogic.dm.events;

/**
 * Created by bmw on 25.06.2015.
 */
public enum EventType {
    ALERT("A"), // срабатываение датчика
    READ("R"), // данное событие "закрывает" все предыдущие срабатывания датчика
    ON("U"), // датчик активирован пользователем. События по нему сохраняются
    OFF("D"); // датчик деактивирован пользователем. События по нему не сохраняются

    EventType(String code) {
        this.code = code;
    }
    private String code;

    public String getCode() {
        return code;
    }

    public static EventType fromCode(String code) {
        for (EventType value: values())
        if (value.getCode().equals(code))
            return value;
        return null;
    }
}
