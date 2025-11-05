package top.aion0573.mqtt.channel;

import top.aion0573.mqtt.common.channel.Channel;
import top.aion0573.mqtt.http.HttpChannel;
import top.aion0573.mqtt.http.HttpChannelHandler;
import top.aion0573.mqtt.http.HttpHeader;
import top.aion0573.mqtt.http.HttpRequest;
import top.aion0573.mqtt.websocket.WebSocketChannel;

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
        if (channel instanceof WebSocketChannel webSocketChannel) {
            HttpChannel httpChannel = (HttpChannel) webSocketChannel.getChannel();
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
                    return new MqttWebSocketChannel(webSocketChannel);
                }
            } else {
                throw new RuntimeException("websocket channel is not mqtt protocol");
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
