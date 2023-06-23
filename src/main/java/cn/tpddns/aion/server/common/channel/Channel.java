package cn.tpddns.aion.server.common.channel;

import cn.tpddns.aion.server.common.buffer.AionByteBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

}
