package ru.werklogic.werklogic.dm;

import android.app.Application;

import ru.werklogic.werklogic.utils.ExceptionHandler;
import ru.werklogic.werklogic.utils.Utils;

/**
 * Created by bmw on 15.05.2015.
 */
public class WerkLogicApplication extends Application {
    /*
    Приложение при старте:
    - создает модель данных
    - запускает сервис, сервис запускает нить канала USB (UsbChannel).

    При подсоединении USB  устройства:
    - у сервиса вызывается метод connectUSBDevice
    - у модели данных вызывается метод инициализации списка датчиков (чтения контрольной суммы и загрузка списка датчиков)
    - устанавливается обработчик данных для канала USB

    При отсоединении USB  устройства:
    - вызывается метод disconnectUSBDevice у сервиса
    - сбрасывается обработчик данных для канала USB

    ВСЕ методы работы с данными хранятся в модели.
    Модель данных содержит ссылку на объект приложения и оповещает его широковещательными сообщениями об изменении модели.
    Вызовы методов модели данных синхронизируются относительно объекта самой модели для корректного изменения параллельными потоками.

    Состояния сервиса:
    - начальное
    - нить канала запущена, устройство отсоединено
    - устройство подсоединено

     */
    private static final SensorState.State INIT_SENSOR_STATE = SensorState.State.INIT_ACTIVE;
    private DataModel dataModel;

    @Override
    public void onCreate() {
        super.onCreate();

        Utils.createAppDirs(); // создание папок приложения
        Utils.log("Создание объекта Application");

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

        dataModel = new DataModel(this);

        //Utils.startService(this);
    }

    public DataModel getDataModel() {
        return dataModel;
    }

}
