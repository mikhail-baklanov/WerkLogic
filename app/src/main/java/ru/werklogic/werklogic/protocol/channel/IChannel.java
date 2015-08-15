package ru.werklogic.werklogic.protocol.channel;

/**
 * Канал должен поддерживать состояние связи по каналу.
 *
 * @author bmw
 */
public interface IChannel {

    /**
     * Записать данные в канал
     *
     * @throws EChannelException
     *           ошибка, возникшая при передачи данных в канал. Переданы ли данные - не известно
     */
    public void writeData(byte[] data) throws EChannelException;

    /**
     * Добавить слушателя событий по каналу
     *
     * @param listener
     *          слушатель
     */
    public void addChannelListener(IChannelListener listener);

    public void deleteChannelListener(IChannelListener listener);

    /**
     * Определяет наличие связи по каналу.
     *
     * @return true - есть связь, false - нет связи
     */
    public boolean isConnected();

}
