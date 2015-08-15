package ru.werklogic.werklogic.dm.events;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import ru.werklogic.werklogic.dm.SensorState;
import ru.werklogic.werklogic.utils.Utils;

/**
 * Created by bmw on 25.06.2015.
 */
public class SingleSensorEventsReader implements EventsReader {
    private RandomAccessFile file;
    private String fileName;
    private long offset;
    private SensorState sensor;

    public SingleSensorEventsReader(SensorState sensor) {
        this.sensor = sensor;
        fileName = Utils.getSensorEventsFileName(sensor.getGuid());
        File f = new File(Utils.getEventsDir(), fileName);
        if (!f.exists())
            return;

        try {
            file = new RandomAccessFile(f, "r");
            offset = file.length();
//            Utils.log("Размер файла " + fileName + " равен " + offset);
        } catch (FileNotFoundException e) {
            Utils.log("Ошибка открытия файла " + fileName + " для чтения событий");
        } catch (IOException e) {
            Utils.log("Ошибка чтения размера для файла " + fileName);
        }
    }

    @Override
    public boolean eof() {
        if (file == null)
            return true;
        return offset < EventStorage.EVENT_STRING_LEN;
    }

    @Override
    public EventStorage.Event next() {
        if (file == null)
            return null;

        if (offset >= 0)
            offset -= EventStorage.EVENT_STRING_LEN;
        if (offset < 0)
            return null;

        try {
//            Utils.log("Перемещение указателя в файле " + fileName + " на позицию " + offset);
            file.seek(offset);
            byte[] buffer = new byte[EventStorage.EVENT_STRING_LEN - 1];
//            Utils.log("Чтение данных из файла " + fileName);
            int len = file.read(buffer);
//            Utils.log("Прочитано " + len + " байт");
            if (len < EventStorage.EVENT_STRING_LEN - 1)
                throw new IOException("Из файла событий " + fileName + " прочитано менее ожидаемого числа байт");
//            Utils.log("Прочитано " + len + " байт");
            EventStorage.Event event = new EventStorage.Event(sensor, new String(buffer, EventStorage.CHARSET));
//            Utils.log("Создано событие " + event);
            return event;
        } catch (IOException e) {
            Utils.log("Ошибка ввода/вывода для файла событий " + fileName);
       }

        return null;
    }

    @Override
    public void close() {
        if (file != null)
            try {
                file.close();
            } catch (IOException e) {
                Utils.log("Ошибка закрытия файла событий " + fileName);
            } finally {
                file = null;
            }
    }
}
