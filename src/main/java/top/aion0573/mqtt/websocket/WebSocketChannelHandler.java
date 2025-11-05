package top.aion0573.mqtt.websocket;

import top.aion0573.mqtt.common.channel.Channel;
import top.aion0573.mqtt.http.HttpChannel;
import top.aion0573.mqtt.http.HttpChannelHandler;
import top.aion0573.mqtt.http.HttpHeader;
import top.aion0573.mqtt.http.HttpRequest;
import top.aion0573.mqtt.utils.ShaUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class WebSocketChannelHandler extends HttpChannelHandler {

    final private List<String> allowSecWebsocketProtocols;

    public WebSocketChannelHandler() {
        this(null);
    }

    public WebSocketChannelHandler(List<String> allowSecWebsocketProtocols) {
        this.allowSecWebsocketProtocols = allowSecWebsocketProtocols;
    }

    @Override
    public Channel handle(Channel channel) {
        if (channel instanceof HttpChannel httpChannel) {
            HttpRequest request = httpChannel.getRequest();
            Map<String, String> requestHeader = request.getRequestHeader();
            String upgrade = requestHeader.get(HttpHeader.UPGRADE);
            if (upgrade != null && upgrade.equals("websocket")) {
                String secWebSocketKey = requestHeader.get(HttpHeader.SEC_WEB_SOCKET_KEY);
                if (secWebSocketKey == null) {
                    //TODO 返回错误的响应 并且把socket 关闭
                    channel.close();
                    return null;
                }
                String secWebSocketAccept = "";
                try {
                    secWebSocketAccept = Base64.getEncoder().encodeToString(ShaUtils.shaEncode(secWebSocketKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"));
                } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }

                String secWebsocketProtocols = requestHeader.get(HttpHeader.SEC_WEBSOCKET_PROTOCOL);
                if (secWebsocketProtocols != null) {
                    if (allowSecWebsocketProtocols == null || !allowSecWebsocketProtocols.contains(secWebsocketProtocols)) {
                        System.out.println("不受支持的websocket 升级子协议 Protocol=" + secWebsocketProtocols);
                        channel.close();
                        return null;
                    }
                }

                String a = ("HTTP/1.1 101 Switching Protocols\r\n" +
                            "Upgrade: websocket\r\n" +
                            "Connection: Upgrade\r\n" +
                            "Sec-WebSocket-Accept: %s\r\n" +
                            (
                                    secWebsocketProtocols != null ?
                                            HttpHeader.SEC_WEBSOCKET_PROTOCOL + ":" + secWebsocketProtocols + "\r\n"
                                            : ""
                            ) +
                            "\r\n").formatted(secWebSocketAccept);
                try {
                    channel.write(a.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return new WebSocketChannel(httpChannel);
            }
            return httpChannel;
        }
        return channel;
    }
}
