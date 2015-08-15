package ru.werklogic.werklogic.protocol.facade;

import ru.werklogic.werklogic.protocol.channel.IChannel;
import ru.werklogic.werklogic.protocol.data.HardwareSensorInfo;
import ru.werklogic.werklogic.protocol.data.IResponseData;
import ru.werklogic.werklogic.protocol.executors.EventExecutor;
import ru.werklogic.werklogic.protocol.executors.RequestExecutor;
import ru.werklogic.werklogic.protocol.messages.ClearCellRequest;
import ru.werklogic.werklogic.protocol.messages.IRequestMessage;
import ru.werklogic.werklogic.protocol.messages.SetupSensorRequest1;
import ru.werklogic.werklogic.protocol.messages.SetupSensorRequest2;
import ru.werklogic.werklogic.protocol.messages.SetupSensorRequest3;

public class Rec31Facade {

    public static final int CHECK_SUM_START_ADDRESS = 0;

    public interface ISetupDetectorListener {
        void onFirstClick();

        void onSecondClick();

        void onFailure(Exception e);

        void onSuccess(HardwareSensorInfo sensorInfo);
    }

    private IChannel channel;
    private RequestExecutor requestExecutor;
    private EventExecutor eventExecutor;

    public boolean isConnected() {
        return !disconnected();
    }

    public void init(IChannel channel, IDataListener dataListener) {
        this.channel = channel;
        this.requestExecutor = new RequestExecutor(channel);
        this.eventExecutor = new EventExecutor(channel, dataListener);
    }

    public void done() {
        requestExecutor.done();
        eventExecutor.done();
    }

    public void setupSensor(byte sensorNumber, ISetupDetectorListener setupDetectorListener) {
        if (disconnected()) {
            setupDetectorListener.onFailure(new EServerDisconnectException());
            return;
        }

        try {
            IRequestMessage request = new ClearCellRequest(sensorNumber);
            requestExecutor.executeRequest(request);

            request = new SetupSensorRequest1(sensorNumber);
            requestExecutor.executeRequest(request);
            setupDetectorListener.onFirstClick();

            request = new SetupSensorRequest2();
            requestExecutor.executeRequest(request);
            setupDetectorListener.onSecondClick();

            request = new SetupSensorRequest3(sensorNumber);
            IResponseData responseData = requestExecutor.executeRequest(request);
            setupDetectorListener.onSuccess((HardwareSensorInfo) responseData);
        } catch (Exception e) {
            setupDetectorListener.onFailure(e);
            return;
        }

    }

    public boolean deleteSensor(byte sensorNumber) {
        if (disconnected()) {
            return false;
        }

        try {
            IRequestMessage request = new ClearCellRequest(sensorNumber);
            requestExecutor.executeRequest(request);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public String readChecksum() {
        if (disconnected()) {
            return null;
        }

        String result = null;
        // TODO раскомментировать, когда будет работать получение контрольной суммы
//        int address = CHECK_SUM_START_ADDRESS;
//        try {
//            String checkSum = "";
//            for (int i = 0; i < 4; i++) {
//                IRequestMessage request = new ReadCheckSumRequest(address++);
//                IResponseData responseData = requestExecutor.executeRequest(request);
//                String s = ((ChecksumResponceData) responseData).getHexes();
//                if (s == null)
//                    return null;
//                checkSum += s;
//            }
//            result = checkSum;
//        } catch (Exception e) {
//            Utils.log("Ошибка чтения контрольной суммы: " + Utils.getStackTrace(e));
//        }
        return result;
    }

    private boolean disconnected() {
        return channel == null || !channel.isConnected();
    }
}
