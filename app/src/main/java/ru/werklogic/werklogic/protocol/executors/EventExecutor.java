package ru.werklogic.werklogic.protocol.executors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ru.werklogic.werklogic.protocol.channel.IChannel;
import ru.werklogic.werklogic.protocol.channel.IChannelListener;
import ru.werklogic.werklogic.protocol.data.IResponseData;
import ru.werklogic.werklogic.protocol.facade.IDataListener;
import ru.werklogic.werklogic.protocol.messages.IResponseMessage;
import ru.werklogic.werklogic.protocol.messages.SensorEvent;
import ru.werklogic.werklogic.protocol.utils.Utils;

public class EventExecutor implements IChannelListener {

  private IChannel channel;
  private IDataListener listener;
  private ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

  public EventExecutor(IChannel channel, IDataListener listener) {
    this.channel = channel;
    this.listener = listener;
    channel.addChannelListener(this);
  }

  @Override
  public void onData(byte[] data) {
    try {
      byteStream.write(data);
      
      IResponseMessage response = new SensorEvent();
      IResponseData responseData = response.readFrom(byteStream.toByteArray());
      if (responseData != null) {
        byteStream.reset();
        listener.onData(responseData);
      }
      
      if (Utils.findCR(byteStream.toByteArray())) {
        byteStream.reset();
      }
    }
    catch (IOException e) {
    }
  }

  @Override
  public void onConnect() {
    byteStream.reset();
  }

  @Override
  public void onDisconnect() {
    byteStream.reset();
  }

  public void done() {
    channel.deleteChannelListener(this);
  }

}
