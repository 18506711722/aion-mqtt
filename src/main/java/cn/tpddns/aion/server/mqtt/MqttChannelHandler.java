package cn.tpddns.aion.server.mqtt;

import cn.tpddns.aion.server.SocketChannel;
import cn.tpddns.aion.server.common.channel.Channel;
import cn.tpddns.aion.server.http.HttpChannel;
import cn.tpddns.aion.server.http.HttpChannelHandler;
import cn.tpddns.aion.server.http.HttpHeader;
import cn.tpddns.aion.server.http.HttpRequest;
import cn.tpddns.aion.server.websocket.WebSocketChannel;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class MqttChannelHandler extends HttpChannelHandler {
    private Class<? extends MqttChannel> channelClazz;

    public MqttChannelHandler() {

    }

    public MqttChannelHandler(Class<? extends MqttChannel> channelClazz) {
        this.channelClazz = channelClazz;
    }

    @Override
    public Channel handle(Channel channel) {
        if (channel instanceof WebSocketChannel) {
            HttpChannel httpChannel = (HttpChannel) ((WebSocketChannel) channel).getChannel();
            HttpRequest httpRequest = httpChannel.getRequest();
            Map<String, String> requestHeader = httpRequest.getRequestHeader();
            String secWebsocketProtocol = requestHeader.get(HttpHeader.SEC_WEBSOCKET_PROTOCOL);
            if ("mqtt".equals(secWebsocketProtocol)) {
                if (this.channelClazz != null) {
                    try {
                        return channelClazz.getDeclaredConstructor(Channel.class).newInstance(channel);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                             NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    return new MqttWebSocketChannel(channel);
                }
            } else {
                throw new RuntimeException("websocket channel not mqtt protocol");
            }
        } else if (channel instanceof SocketChannel) {
            if (this.channelClazz != null) {
                try {
                    return channelClazz.getDeclaredConstructor(Channel.class).newInstance(channel);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return new MqttSocketChannel(channel);
            }
        } else {
            throw new RuntimeException("not support channel " + channel.getClass());
        }
    }
}
