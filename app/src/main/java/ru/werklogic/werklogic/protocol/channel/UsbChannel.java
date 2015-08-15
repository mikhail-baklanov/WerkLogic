package ru.werklogic.werklogic.protocol.channel;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.werklogic.werklogic.utils.Utils;

import static ru.werklogic.werklogic.utils.Utils.log;

public class UsbChannel extends Thread implements IChannel {
    private int endPointType = UsbConstants.USB_ENDPOINT_XFER_INT;

    private UsbEndpoint epOut;
    private UsbEndpoint epIn;
    private UsbInterface intf;
    private UsbDeviceConnection connection;
    private UsbManager manager;
    private boolean connected;
    private UsbRequest writeRequest = new UsbRequest();
    private UsbRequest readRequest = new UsbRequest();
    private List<IChannelListener> listeners = new ArrayList<>();
    private ByteBuffer readBuffer;

    public UsbChannel(UsbManager manager) {
        this.manager = manager;
    }

    public void disconnect() {
        close();
        connected = false;
    }

    private void close() {
        if (connection != null) {
            try {
                connection.releaseInterface(intf);
                connection.close();
                connection = null;
            } catch (Exception e) {
                log("Ошибка закрытия соединения канала с USB: " + Utils.getStackTrace(e));
            }
        }
        epIn = null;
        epOut = null;
        readBuffer = null;
        readRequest.close();
        writeRequest.close();
    }

    public boolean connect(UsbDevice device) {
        if (connected) {
            log("Вызов connect() при уже установленном соединении !!!");
            return false;
        }

        close();

        if (device == null) {
            log("Вызов connect() с параметром device=null !!!");
            return false;
        }

//        log("interface count = " + device.getInterfaceCount());
        if (device.getInterfaceCount() > 0) {
            intf = device.getInterface(0);
            if (intf != null) {
//                log("interface[0] != null");
                for (int i = 0; i < intf.getEndpointCount(); i++) {
                    UsbEndpoint ep = intf.getEndpoint(i);
//                    log("ep[" + i + "] type = " + ep.getType());
                    if (ep.getType() == endPointType) {
//                        log("ep[" + i + "] direction = " + ep.getDirection() + "(out=" + UsbConstants.USB_DIR_OUT + ")");
                        if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                            epOut = ep;
                        } else {
                            epIn = ep;
                        }
                    }
                }
                if (epIn != null && epOut != null) {
//                    log("both ep are found");
                    connection = manager.openDevice(device);
//                    log("connection=" + connection);
                    if (connection != null) {
                        boolean claimResult = connection.claimInterface(intf, false);
//                        log("Вызов claimInterface(intf, false), результат = " + claimResult);
                        if (!claimResult) {
                            claimResult = connection.claimInterface(intf, true);
//                            log("Вызов claimInterface(intf, true), результат = " + claimResult);
                        }
                        if (claimResult) {
                            readRequest.initialize(connection, epIn);
                            writeRequest.initialize(connection, epOut);
                            readBuffer = ByteBuffer.allocate(epIn.getMaxPacketSize());
                            connected = true;
                        }
                    }
                }
            } else {
                log("interface[0] == null");
            }
        }
        log("Результат вызова метода connect() равен " + connected);
        return connected;
    }

    @Override
    public void writeData(byte[] bytes) throws EChannelException {
        if (connected) {
            ByteBuffer writeBuffer = ByteBuffer.allocate(epOut.getMaxPacketSize());
            writeBuffer.put(bytes);
            for (int i = bytes.length; i < epOut.getMaxPacketSize(); i++) {
                writeBuffer.put((byte) 0);
            }
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < writeBuffer.capacity(); i++) {
                sb.append(writeBuffer.get(i) + ", ");
            }
//            log("Запись в устройство байтов: " + sb);
            writeRequest.queue(writeBuffer, epOut.getMaxPacketSize());
        } else {
            log("Запись в устройство невозможно, т.к. не выполнено подключение");
        }
    }

    @Override
    public void addChannelListener(IChannelListener listener) {
        listeners.add(listener);
    }

    @Override
    public void deleteChannelListener(IChannelListener listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    private void onData(byte[] data) {
        for (IChannelListener listener : listeners) {
            listener.onData(data);
        }
    }

    @Override
    public void run() {
        log("Запуск нити слушателя USB");

        do {
            try {
                if (connected) {
                    readRequest.queue(readBuffer, epIn.getMaxPacketSize());
                    //log("Выход из readRequest.queue()");
                    do {
                    } while (connection != null && connection.requestWait() != readRequest);

                    if (connection != null) {
                        byte[] buf = new byte[epIn.getMaxPacketSize()];
                        int pos = 0;
                        for (int i = 0; i < readBuffer.array().length; i++) {
                            byte b = readBuffer.get(i);
                            if (b != 0) {
                                buf[pos++] = b;
                            }
                        }
                        //log("Прочитаны данные из readBuffer, размер = " + readBuffer.array().length);
                        if (pos > 0) {
                            onData(Arrays.copyOfRange(buf, 0, pos));
                        }
                    }
                } else {
                    Thread.sleep(1000);
                }
            } catch (Throwable e) {
                log("Произошло исключение в нити обмена данными с USB: " + Utils.getStackTrace(e));
            }
        } while (!isInterrupted());
        log("Нить слушателя USB завершена");
    }
}
