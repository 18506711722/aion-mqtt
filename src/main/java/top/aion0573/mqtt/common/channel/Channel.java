package top.aion0573.mqtt.common.channel;

import top.aion0573.mqtt.common.buffer.AionByteBuffer;
import top.aion0573.mqtt.server.AionServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * 通信通道
 */
public interface Channel {

    void write(byte[] b) throws IOException;

    void onRead0(byte[] b);

    InputStream getInputStream() throws IOException;

    OutputStream getOutputStream() throws IOException;

    AionByteBuffer getReadCache();

    void setReadCache(AionByteBuffer readCacheBuffer);

    void close();

    void onClose();

    boolean isClose();

    void setClose(boolean b);

    long getIdleTime();

    AionServer getServer();

    void setServer(AionServer server);

}
