package cn.tpddns.aion.server.websocket.message;

public class WebSocketBinaryMessage extends AbstractWebSocketMessage<byte[]>{

    public WebSocketBinaryMessage(byte[] payload) {
        super(payload);
    }

    public WebSocketBinaryMessage(byte[] payload, boolean isLast) {
        super(payload, isLast);
    }

    @Override
    public int getPayloadLength() {
        return this.getPayload().length;
    }

    @Override
    public byte[] asBytes() {
        return this.getPayload();
    }
}
