package top.aion0573.mqtt.websocket.message;

public interface WebSocketMessage<T> {

    T getPayload();

    int getPayloadLength();

    boolean isLast();

    byte[] asBytes();
}
