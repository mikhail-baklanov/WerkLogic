package ru.werklogic.werklogic.protocol.facade;

import ru.werklogic.werklogic.protocol.data.IResponseData;

public interface IDataListener {

    /**
     * От сервера получены данные.
     * @return
     */
    public void onData(IResponseData data);

    /**
     * Появилась связь с сервером.
     */
    public void onConnect();

    /**
     * Связь с сервером потеряна.
     */
    public void onDisconnect();


}
