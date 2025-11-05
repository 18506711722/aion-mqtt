package top.aion0573.mqtt.channel;

import top.aion0573.mqtt.common.buffer.AionByteBuffer;
import top.aion0573.mqtt.message.AionMqttMessage;
import top.aion0573.mqtt.message.MemoryChannelMessageManager;
import top.aion0573.mqtt.websocket.WebSocketChannel;
import top.aion0573.mqtt.websocket.message.WebSocketBinaryMessage;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class MqttWebSocketChannel extends MqttChannel {

    public MqttWebSocketChannel(WebSocketChannel channel) {
        super(channel.getSocket());
        this.setChannel(new WebSocketChannel(channel.getSocket()) {

            @Override
            public void onBinaryMessage(WebSocketBinaryMessage message) {
                MqttWebSocketChannel.this.getReadCache().put(message.getPayload());
                MqttWebSocketChannel.super.onRead(MqttWebSocketChannel.this.getReadCache());
            }
        });
        this.setMessageManager(new MemoryChannelMessageManager());
    }

    @Override
    public void onRead(AionByteBuffer buffer) {
        ((WebSocketChannel) this.getChannel()).onRead(buffer);
    }

    @Override
    public boolean sendMessage(AionMqttMessage message) {
        try {
            ((WebSocketChannel) this.getChannel()).sendMessage(new WebSocketBinaryMessage(message.asByte()));
            return true;
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.getChannel().getInputStream();
    }

}
