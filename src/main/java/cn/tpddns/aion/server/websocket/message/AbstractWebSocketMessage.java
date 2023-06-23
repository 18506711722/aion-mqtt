package cn.tpddns.aion.server.websocket.message;

public abstract class AbstractWebSocketMessage<T> implements WebSocketMessage<T>{
    private T payload;

    private boolean isLast;

    public AbstractWebSocketMessage(T payload) {
        this.payload = payload;
        this.isLast = true;
    }

    public AbstractWebSocketMessage(T payload, boolean isLast) {
        this.payload = payload;
        this.isLast = isLast;
    }

    @Override
    public T getPayload() {
        return payload;
    }

    @Override
    public boolean isLast() {
        return isLast;
    }
}
