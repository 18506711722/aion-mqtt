package cn.tpddns.aion.server;

import cn.tpddns.aion.server.common.channel.Channel;

public interface ChannelHandler {
    /**
     * 通道 处理程序
     *
     * @param channel
     * @return
     */
    Channel handle(Channel channel) throws Exception;
}
