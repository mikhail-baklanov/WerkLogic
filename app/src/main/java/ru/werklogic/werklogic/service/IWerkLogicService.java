package ru.werklogic.werklogic.service;

import android.hardware.usb.UsbDevice;

import ru.werklogic.werklogic.ICommandProcessor;
import ru.werklogic.werklogic.commands.BaseCommand;
import ru.werklogic.werklogic.protocol.facade.Rec31Facade;

/**
 * Created by bmw on 26.06.2015.
 */
public interface IWerkLogicService {
    boolean connectUsbDevice(UsbDevice usbDevice);
    void disconnectUsbDevice();
    /** Получение контрольной суммы. 8-шестнадцатеричных знаков.
     *  @return контрольная сумма или null, если контрольная сумма не получена.
     * */
    String readChecksum();
    void setupSensor(byte sensorNumber, Rec31Facade.ISetupDetectorListener iSetupDetectorListener);
    boolean deleteSensor(byte sensorNumber);
    void processCommand(BaseCommand command);
}
