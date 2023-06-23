package cn.tpddns.aion.server.mqtt;

import cn.tpddns.aion.server.common.channel.AbstractChannel;
import cn.tpddns.aion.server.common.channel.Channel;
import cn.tpddns.aion.server.common.buffer.AionByteBuffer;
import cn.tpddns.aion.server.mqtt.exception.AionMqttUnknownTypeMessageException;
import cn.tpddns.aion.server.mqtt.message.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class MqttSocketChannel extends AbstractChannel implements MqttChannel {
    public MqttServerContext getContext() {
        return MqttServerContext.getInstance();
    }

    @Override
    public boolean isWill() {
        return false;
    }

    @Override
    public String getWillTopic() {
        return null;
    }

    @Override
    public byte[] getWillPayload() {
        return new byte[0];
    }

    @Override
    public int getWillQos() {
        return 0;
    }

    @Override
    public boolean getWillRetain() {
        return false;
    }

    private final AionByteBuffer mqttCacheBuffer;

    private final MessageManager<Short> messageManager;

    private Channel channel;

    /**
     * mqtt 是否已经连接
     */
    private boolean connect;

    /**
     * mqtt 协议版本
     */
    private byte version;

    private boolean cleanSession;

    private String username;

    private String clientId;

    private boolean will;

    private int willQos;

    private byte[] willPayload;

    private String willTopic;

    private boolean willRetain;

    public boolean isConnect() {
        return connect;
    }

    public void setConnect(boolean connect) {
        this.connect = connect;
    }

    @Override
    public MessageManager<Short> getMessageManager() {
        return null;
    }

    public int getVersion() {
        return version;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public MqttSocketChannel(Channel channel) {
        this.channel = channel;
        this.mqttCacheBuffer = AionByteBuffer.allocate(8 * 1024);
        this.messageManager = new MemoryChannelMessageManager();
    }

    @Override
    public void onRead(AionByteBuffer buffer) {
        int limit = buffer.limit();
        byte[] payload = new byte[limit];
        buffer.get(payload);
        buffer.compact();
        this.onRead(payload);
    }

    @Override
    public boolean onConnect(AionMqttConnectMessage message) {
        return true;
    }

    @Override
    public void setWillRetain(boolean willRetain) {
        this.willRetain = willRetain;
    }

    @Override
    public void setWillTopic(String willTopic) {
        this.willTopic = willTopic;
    }

    @Override
    public void setWillPayload(byte[] willPayload) {
        this.willPayload = willPayload;
    }

    @Override
    public void setWillQos(int willQos) {
        this.willQos = willQos;
    }

    @Override
    public void setWill(boolean will) {
        this.will = will;
    }

    @Override
    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    @Override
    public void setVersion(byte version) {
        this.version = version;
    }

    @Override
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }


    @Override
    public boolean sendMessage(AionMqttMessage message) {
        try {
            this.write(message.asByte());
            return true;
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public String getClientId() {
        return this.clientId;
    }

    @Override
    public AionByteBuffer getCacheBuffer() {
        return this.mqttCacheBuffer;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.channel.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return this.channel.getOutputStream();
    }

}
