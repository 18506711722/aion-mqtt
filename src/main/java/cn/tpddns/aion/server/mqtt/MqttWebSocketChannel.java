package cn.tpddns.aion.server.mqtt;

import cn.tpddns.aion.server.common.channel.Channel;
import cn.tpddns.aion.server.mqtt.exception.AionMqttUnknownTypeMessageException;
import cn.tpddns.aion.server.mqtt.message.*;
import cn.tpddns.aion.server.websocket.WebSocketChannel;
import cn.tpddns.aion.server.common.buffer.AionByteBuffer;
import cn.tpddns.aion.server.websocket.message.WebSocketBinaryMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class MqttWebSocketChannel extends WebSocketChannel implements MqttChannel {

    public MqttServerContext getContext() {
        return MqttServerContext.getInstance();
    }

    private final AionByteBuffer mqttCacheBuffer;

    private final MessageManager<Short> messageManager;

    /**
     * mqtt 是否已经连接
     */
    private boolean connect;

    /**
     * mqtt 协议版本
     */
    private int version;

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
        return this.messageManager;
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

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public AionByteBuffer getCacheBuffer() {
        return this.mqttCacheBuffer;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    @Override
    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    @Override
    public void setVersion(byte version) {
        this.version = version;
    }

    public boolean isWill() {
        return will;
    }

    @Override
    public void setWill(boolean will) {
        this.will = will;
    }

    public int getWillQos() {
        return willQos;
    }

    @Override
    public boolean getWillRetain() {
        return this.willRetain;
    }

    @Override
    public void setWillQos(int willQos) {
        this.willQos = willQos;
    }

    public byte[] getWillPayload() {
        return willPayload;
    }

    @Override
    public void setWillPayload(byte[] willPayload) {
        this.willPayload = willPayload;
    }

    public String getWillTopic() {
        return willTopic;
    }

    @Override
    public void setWillTopic(String willTopic) {
        this.willTopic = willTopic;
    }

    public boolean isWillRetain() {
        return willRetain;
    }

    @Override
    public void setWillRetain(boolean willRetain) {
        this.willRetain = willRetain;
    }

    public MqttWebSocketChannel(Channel channel) {
        super(channel);
        this.mqttCacheBuffer = AionByteBuffer.allocate(8 * 1024);
        this.messageManager = new MemoryChannelMessageManager();
    }

    @Override
    public void onBinaryMessage(WebSocketBinaryMessage message) {
        this.onRead(message.getPayload());
    }

    @Override
    public boolean onConnect(AionMqttConnectMessage message) {
        return true;
    }

    @Override
    public boolean sendMessage(AionMqttMessage message) {
        log.debug("mqtt publish message");
        try {
            this.sendMessage(new WebSocketBinaryMessage(message.asByte()));
            return true;
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return getChannel().getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return getChannel().getOutputStream();
    }

    @Override
    public void close() {
        getChannel().close();
    }
}
