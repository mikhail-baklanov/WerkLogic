package ru.werklogic.werklogic.dm;

/**
 * Created by bmw on 20.08.2015.
 */
public enum ActionType {
    NONE, // реакция отсутстсвует
    SIGNAL, // сигнализирование о срабатывании датчика охраны
    TO_SPY, // на охрану
    TO_NORMAL // переход в обычный режим без охраны
}
