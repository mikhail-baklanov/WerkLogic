package ru.werklogic.werklogic.protocol.channel;

public interface IChannelListener {

    /**
     * По каналу получены данные.
     *
     * @param data
     *          данные
     */
    public void onData(byte[] data);

    /**
     * Появилась связь по каналу.
     */
    public void onConnect(); // не понятно нужен ли этот метод

    /**
     * Связь по каналу потеряна.
     */
    public void onDisconnect(); // не понятно нужен ли этот метод

}
