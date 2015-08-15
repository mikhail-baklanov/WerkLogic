package ru.werklogic.werklogic.dm.events;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ru.werklogic.werklogic.dm.SensorState;
import ru.werklogic.werklogic.utils.Utils;

/**
 * Created by bmw on 25.06.2015.
 */
public class EventStorage {

    private static final String DATE_FORMAT_STRING = "yyyyMMddHHmmss";
    public static final int EVENT_STRING_LEN = DATE_FORMAT_STRING.length() + 2 /* code + LF */;
    public static final String CHARSET = "ISO-8859-1";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);

    public static EventsReader getEventsReader(List<SensorState> sensors) {
        return new MultipleSensorsEventsReader(sensors);
    }

    public static EventsReader getEventsReader(SensorState sensor) {
        return new SingleSensorEventsReader(sensor);
    }

    public static void saveEvent(String sensorGuid, Date eventTime, EventType eventType) {
        String fileName = Utils.getSensorEventsFileName(sensorGuid);
        String data = DATE_FORMAT.format(eventTime) + eventType.getCode() + "\n";
        try {
            byte[] bytes = data.getBytes(CHARSET);
            if (bytes.length != EVENT_STRING_LEN)
                throw new Exception("Длина сохраняемой в файле строки события " + data +
                        " не соответствует ожидаемой длине, равной " + EVENT_STRING_LEN);
            File outFile = new File(Utils.getEventsDir(), fileName);
            OutputStream outStream = new FileOutputStream(outFile, true);
            outStream.write(bytes);
            outStream.close();
        } catch (Exception e) {
            Utils.log("Ошибка записи в файл " + fileName + ": " + Utils.getStackTrace(e));
        }
    }

    public static class Event {
        private SensorState sensor;
        private Date eventTime;
        private EventType eventType;

        public Event(SensorState sensor, String data) {
            this.sensor = sensor;
            try {
                this.eventTime = DATE_FORMAT.parse(data.substring(0, DATE_FORMAT_STRING.length()));
            } catch (ParseException e) {
                Utils.log("Ошибка парсинга строки события [" + data + "] для датчика " + sensor.getGuid());
            }
            this.eventType = EventType.fromCode(data.substring(DATE_FORMAT_STRING.length()));
        }

        public SensorState getSensor() {
            return sensor;
        }

        public Date getEventTime() {
            return eventTime;
        }

        public EventType getEventType() {
            return eventType;
        }

        @Override
        public String toString() {
            return "Event{" +
                    "sensor='" + (sensor == null || sensor.getName() == null ? "<без имени>" : sensor.getName()) +"'" +
                    ", eventTime=" + eventTime +
                    ", eventType=" + eventType +
                    '}';
        }
    }

}
