package ru.werklogic.werklogic.service;

/**
 * Created by bmw on 14.07.2015.
 */
public interface RunnableWithParameter<T> {
    void run(T t);
}
