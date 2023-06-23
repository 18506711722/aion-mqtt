package cn.tpddns.aion.server.common.channel;

import cn.tpddns.aion.server.common.buffer.AionByteBuffer;

import java.io.IOException;
import java.util.Date;

public abstract class AbstractChannel implements Channel {
    private Date lastReadDate;
    private Date lastWriteDate;

    private boolean close;

    private AionByteBuffer readBuff;

    public Date getLastReadDate() {
        return lastReadDate;
    }

    public void setLastReadDate(Date lastReadDate) {
        this.lastReadDate = lastReadDate;
    }

    public Date getLastWriteDate() {
        return lastWriteDate;
    }

    public void setLastWriteDate(Date lastWriteDate) {
        this.lastWriteDate = lastWriteDate;
    }

    public AbstractChannel() {
        readBuff =  AionByteBuffer.allocate(8 * 1024);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.setLastWriteDate(new Date());
        this.getOutputStream().write(b);
    }

    @Override
    public void onRead0(byte[] b) {
        if(readBuff.capacity() - readBuff.limit()>=b.length){
            this.setLastReadDate(new Date());
            readBuff.put(b);
            // 加个 事件 通知通道read 消息
            this.onRead(readBuff);
        }else{
            throw new RuntimeException("读取缓冲区溢出");
        }
    }

    public void onRead(AionByteBuffer buffer) {

    }

    @Override
    public AionByteBuffer getReadCache() {
        return readBuff;
    }

    @Override
    public void setReadCache(AionByteBuffer readCacheBuffer) {
        this.readBuff = readCacheBuffer;
    }


    @Override
    public void onClose() {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean isClose() {
        return close;
    }

    @Override
    public void setClose(boolean b) {
        this.close = b;
    }
}
