package cn.tpddns.aion.server.websocket.message;

public interface WebSocketMessage<T> {

    T getPayload();

    int getPayloadLength();

    boolean isLast();

    byte[] asBytes();
}
