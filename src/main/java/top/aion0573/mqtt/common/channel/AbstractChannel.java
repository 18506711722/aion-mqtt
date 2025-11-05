package top.aion0573.mqtt.common.channel;

import top.aion0573.mqtt.common.buffer.AionByteBuffer;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Date;

/**
 * 抽象的通信通道
 */
public abstract class AbstractChannel implements Channel {
    /**
     * 最后读取时间
     */
    @Setter
    @Getter
    private Date lastReadDate;
    /**
     * 最后写入时间
     */
    @Setter
    @Getter
    private Date lastWriteDate;

    @Setter
    @Getter
    private Date openTime;

    /**
     * 通道是否关闭
     */
    @Setter
    @Getter
    private boolean close;

    /**
     * 读取缓存
     */
    private AionByteBuffer readBuff;

    public AbstractChannel() {
        readBuff = AionByteBuffer.allocate(8 * 1024);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.setLastWriteDate(new Date());
        this.getOutputStream().write(b);
    }

    @Override
    public void onRead0(byte[] b) {
        if (readBuff.capacity() - readBuff.limit() >= b.length) {
            this.setLastReadDate(new Date());
            readBuff.put(b);
            this.onRead(readBuff);
        } else {
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
    public long getIdleTime() {
        if (this.getLastReadDate() == null || this.getLastWriteDate() == null) {
            return System.currentTimeMillis() - this.getOpenTime().getTime();
        }
        return System.currentTimeMillis() - Math.max(this.getLastReadDate().getTime(), this.getLastWriteDate().getTime());
    }
}
