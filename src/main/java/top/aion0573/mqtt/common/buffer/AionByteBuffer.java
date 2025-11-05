package top.aion0573.mqtt.common.buffer;

import top.aion0573.mqtt.common.buffer.exception.BufferReadOverflowException;
import top.aion0573.mqtt.common.buffer.exception.BufferWriteOverflowException;

import java.util.Arrays;

public class AionByteBuffer {
    private byte[] bytes;

    /**
     * 读取下标
     */
    private int readIndex;
    /**
     * 写入下标
     */
    private int writeIndex;
    /**
     * 估计是有效长度
     */
    private int limit;
    /**
     * 容量
     */
    private int capacity;
    /**
     * 临时标记
     */
    private int mark;

    public AionByteBuffer(int capacity) {
        this.capacity = capacity;
        this.bytes = new byte[capacity];
    }

    public int capacity() {
        return this.capacity;
    }

    public int limit() {
        return this.limit;
    }

    public AionByteBuffer put(byte b) {
        return this.put(new byte[]{b});
    }

    public AionByteBuffer put(byte[] b) {
//        if (this.capacity - this.limit < b.length) {
//            throw new BufferWriteOverflowException(String.format("超出缓存最大可写 limit=%d capacity=%d  writeSize=%d", this.limit, this.capacity, b.length));
//        }
//        for (byte value : b) {
//            bytes[writeIndex++] = value;
//        }
//        this.limit += b.length;
//        return this;
        return this.put(b, 0, b.length);
    }

    public AionByteBuffer put(byte[] b, int offset, int length) {
        if (this.capacity - this.limit < length - offset) {
            throw new BufferWriteOverflowException(String.format("超出缓存最大可写 limit=%d capacity=%d  writeSize=%d", this.limit, this.capacity, b.length));
        }
        for(int i = 0; i < length; i++){
            bytes[writeIndex++] = b[offset+i];
        }
        this.limit += length;
        return this;
    }

    public byte get() {
        if (this.readIndex >= this.limit) {
            throw new BufferReadOverflowException(String.format("超出缓存最大可读 limit=%d readIndex=%d  readSize=%d", this.limit, this.readIndex, 1));
        }
        return bytes[this.readIndex++];
    }

    public AionByteBuffer get(byte[] b) {
        if ((this.readIndex + b.length) > this.limit) {
            throw new BufferReadOverflowException(String.format("超出缓存最大可读 limit=%d readIndex=%d  readSize=%d", this.limit, this.readIndex, b.length));
        }
        for (int i = 0; i < b.length; i++) {
            b[i] = bytes[this.readIndex++];
        }
        return this;
    }

    public byte[] getEffectiveBytes() {
        return Arrays.copyOfRange(this.bytes, 0, this.limit);
    }

    public AionByteBuffer compact() {
        int remaining = this.limit - this.readIndex;
        for (int i = 0; i < remaining; i++) {
            bytes[i] = bytes[this.readIndex + i];
        }
        this.limit = remaining;
        this.readIndex = 0;
        this.writeIndex = remaining;
        return this;
    }

    public AionByteBuffer mark() {
        this.mark = this.readIndex;
        return this;
    }

    public AionByteBuffer reset() {
        this.readIndex = this.mark;
        this.mark = -1;
        return this;
    }

    public int getReadableSize() {
        return this.limit - this.readIndex;
    }


    public static AionByteBuffer allocate(int capacity) {
        return new AionByteBuffer(capacity);
    }

    public static AionByteBuffer wrap(byte[] array) {
        return AionByteBuffer.wrap(array, 0, array.length);
    }

    public static AionByteBuffer wrap(byte[] array,
                                      int offset, int length) {
        AionByteBuffer buffer = new AionByteBuffer(length);
        for (int i = 0; i < length; i++) {
            buffer.bytes[i] = array[offset + i];
        }
        buffer.limit = length;
        return buffer;
    }
}
