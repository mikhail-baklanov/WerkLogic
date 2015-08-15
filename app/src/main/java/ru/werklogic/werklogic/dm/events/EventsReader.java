package ru.werklogic.werklogic.dm.events;

/**
 * Created by bmw on 25.06.2015.
 */
public interface EventsReader {

    public boolean eof();
    public EventStorage.Event next();
    public void close();


}
