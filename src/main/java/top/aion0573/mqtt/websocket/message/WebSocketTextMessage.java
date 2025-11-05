package top.aion0573.mqtt.websocket.message;

import java.nio.charset.StandardCharsets;

public class WebSocketTextMessage extends AbstractWebSocketMessage<String>{

    public WebSocketTextMessage(String payload) {
        super(payload);
    }

    public WebSocketTextMessage(String payload, boolean isLast) {
        super(payload, isLast);
    }

    @Override
    public int getPayloadLength() {
        return this.getPayload().length();
    }

    @Override
    public byte[] asBytes() {
        return this.getPayload().getBytes(StandardCharsets.UTF_8);
    }

}
