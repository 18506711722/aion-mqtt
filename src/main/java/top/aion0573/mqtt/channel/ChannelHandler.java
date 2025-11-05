package top.aion0573.mqtt.channel;

import top.aion0573.mqtt.common.channel.Channel;

public interface ChannelHandler {
    /**
     * 通道 处理程序
     *
     * @param channel
     * @return
     */
    Channel handle(Channel channel) throws Exception;
}
