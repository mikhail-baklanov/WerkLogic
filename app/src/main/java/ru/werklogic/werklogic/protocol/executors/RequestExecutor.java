package ru.werklogic.werklogic.protocol.executors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import ru.werklogic.werklogic.protocol.channel.EChannelException;
import ru.werklogic.werklogic.protocol.channel.IChannel;
import ru.werklogic.werklogic.protocol.channel.IChannelListener;
import ru.werklogic.werklogic.protocol.data.IResponseData;
import ru.werklogic.werklogic.protocol.messages.IRequestMessage;
import ru.werklogic.werklogic.protocol.messages.IResponseMessage;
import ru.werklogic.werklogic.protocol.utils.Utils;

public class RequestExecutor implements IChannelListener {

    private IChannel channel;

    private ReentrantLock lock = new ReentrantLock();
    private Semaphore sem = new Semaphore(0);

    private IResponseData responseData;
    private Exception responseException;
    private IResponseMessage responseMessage;

    private ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

    public RequestExecutor(IChannel channel) {
        this.channel = channel;
    }

    public IResponseData executeRequest(IRequestMessage requestMessage) throws EChannelException,
            IOException {
        lock.lock();
        try {
            responseData = null;
            responseException = null;
            byteStream.reset();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            requestMessage.writeTo(out);
            responseMessage = requestMessage.getResponseMessage();

            channel.addChannelListener(this);
            byte[] bytes = out.toByteArray();
            ru.werklogic.werklogic.utils.Utils.log("Запись байтов в канал: " + ru.werklogic.werklogic.utils.Utils.bytes2str(bytes));
            channel.writeData(bytes);
            if (sem.tryAcquire(20, TimeUnit.SECONDS)) {
                if (responseData != null) {
                    return responseData;
                } else if (responseException != null) {
                    throw responseException;
                } else {
                    throw new EChannelException("Неизвестная причина отсутствия данных");
                }
            } else {
                throw new IOException("Таумаут при ожидании данных из канала");
            }
        } catch (Exception e) {
            throw new IOException("Ошибка получения данных", e);
        } finally {
            channel.deleteChannelListener(this);
            lock.unlock();
        }
    }

    @Override
    public void onConnect() {
        if (sem.hasQueuedThreads()) {
            responseException = new IOException("Обнаружено установление нового соединения");
            sem.release();
        }
    }

    @Override
    public void onDisconnect() {
        if (sem.hasQueuedThreads()) {
            responseException = new IOException("Обнаружен разрыв соединения");
            sem.release();
        }
    }

    @Override
    public void onData(byte[] data) {
        ru.werklogic.werklogic.utils.Utils.log("Из устройства получен блок данных: " + ru.werklogic.werklogic.utils.Utils.bytes2str(data));
        if (responseData == null && responseException == null && responseMessage != null) {
            try {
                ru.werklogic.werklogic.utils.Utils.log("Данные записаны в поток-накопитель");
                byteStream.write(data);
                responseData = responseMessage.readFrom(byteStream.toByteArray());
                ru.werklogic.werklogic.utils.Utils.log("Данные интерпретированы в " + responseData);
                if (responseData != null) {
                    sem.release();
                } else {
                    if (Utils.findCR(byteStream.toByteArray())) {
                        byteStream.reset();
                    }
                }
            } catch (IOException e) {
                ru.werklogic.werklogic.utils.Utils.log("Произошло исключение при записи данных в поток-накопитель");
                responseException = e;
                sem.release();
            }
        }
    }

    public void done() {
        if (sem.hasQueuedThreads()) {
            responseException = new IOException("Принудительное завершение");
            sem.release();
        }
    }

}
