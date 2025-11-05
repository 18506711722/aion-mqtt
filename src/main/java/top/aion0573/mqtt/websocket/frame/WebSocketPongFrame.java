package top.aion0573.mqtt.websocket.frame;

public class WebSocketPongFrame extends WebSocketFrame{

    public WebSocketPongFrame() {
        this.setOpcode(WebSocketFrameOpcode.PONG);
    }
}
