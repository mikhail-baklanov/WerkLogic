package ru.werklogic.werklogic.dm;

/**
 * Created by bmw on 20.08.2015.
 */
public enum ActionType {
    NONE(0), // реакция отсутстсвует
    SIGNAL(1), // сигнализирование о срабатывании датчика охраны
    TO_SPY(2), // на охрану
    TO_NORMAL(3), // переход в обычный режим без охраны
    SWITCH(4) // последовательное переключение между режимами охрана/нормальный
    ;
    private int position;

    ActionType(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

}
