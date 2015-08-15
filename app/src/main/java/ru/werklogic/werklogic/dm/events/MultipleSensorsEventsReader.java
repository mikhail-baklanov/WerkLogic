package ru.werklogic.werklogic.dm.events;

import java.util.ArrayList;
import java.util.List;

import ru.werklogic.werklogic.dm.SensorState;

/**
 * Created by bmw on 25.06.2015.
 */
public class MultipleSensorsEventsReader implements EventsReader {
    public List<EventsReader> readers = new ArrayList<>();
    public List<EventStorage.Event> events = new ArrayList<>();
    public boolean eof;

    public MultipleSensorsEventsReader(List<SensorState> sensors) {
        eof = true;
        for (SensorState s : sensors) {
            EventsReader reader = new SingleSensorEventsReader(s);
            readers.add(reader);
            EventStorage.Event event = reader.next();
            if (event != null)
                eof = false;
            events.add(event);
        }
    }

    @Override
    public boolean eof() {
        return eof;
    }

    @Override
    public EventStorage.Event next() {
        EventStorage.Event maxDateEvent = null;
        int readerIndex = 0;
        int eofCount = 0;
        for (int i = 0; i < readers.size(); i++) {
            EventStorage.Event event = events.get(i);
            if (event != null) {
                if (maxDateEvent == null || maxDateEvent.getEventTime().getTime() < event.getEventTime().getTime()){
                    maxDateEvent = event;
                    readerIndex = i;
                }
            } else eofCount++;
        }
        if (maxDateEvent != null) {
            events.set(readerIndex, readers.get(readerIndex).next());
            if (events.get(readerIndex) == null)
                eofCount++;
            if (eofCount == readers.size())
                eof = true;
        }
        return maxDateEvent;
    }

    @Override
    public void close() {
        for (EventsReader r : readers)
            r.close();
    }
}
