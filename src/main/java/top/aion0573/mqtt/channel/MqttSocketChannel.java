package top.aion0573.mqtt.channel;

import top.aion0573.mqtt.common.channel.Channel;
import top.aion0573.mqtt.common.buffer.AionByteBuffer;
import top.aion0573.mqtt.message.AionMqttConnectMessage;
import top.aion0573.mqtt.message.AionMqttMessage;
import top.aion0573.mqtt.message.MemoryChannelMessageManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class MqttSocketChannel extends MqttChannel {

    public MqttSocketChannel(Channel channel) {
        super(((SocketChannel)channel).getSocket());
        this.setChannel(channel);
        this.setServer(channel.getServer());
        this.setMessageManager(new MemoryChannelMessageManager());
    }

    @Override
    public boolean sendMessage(AionMqttMessage message) {
        log.info("mqtt socket channel send {} message: {}",message.getType().name(), message);
        try {
            this.write(message.asByte());
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

    @Override
    public OutputStream getOutputStream() throws IOException {
        return this.getChannel().getOutputStream();
    }

}
