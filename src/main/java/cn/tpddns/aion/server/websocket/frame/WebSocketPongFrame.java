package cn.tpddns.aion.server.websocket.frame;

public class WebSocketPongFrame extends WebSocketFrame{

    public WebSocketPongFrame() {
        this.setOpcode(WebSocketFrameOpcode.PONG);
    }
}
