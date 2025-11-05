package top.aion0573.mqtt.websocket.frame;

import top.aion0573.mqtt.websocket.message.WebSocketMessage;
import top.aion0573.mqtt.websocket.message.WebSocketTextMessage;

public class WebSocketFrameEncoder {

    public static WebSocketFrame encoder(WebSocketMessage<?> message)   {
        byte opcode = message instanceof WebSocketTextMessage ? WebSocketFrameOpcode.TEXT:WebSocketFrameOpcode.BINARY;
        byte[] payload =  message.asBytes();
        WebSocketFrame frame = new WebSocketFrame();
        frame.setLast(true);
        frame.setRsv1(0);
        frame.setRsv2(0);
        frame.setRsv3(0);
        frame.setOpcode(opcode);
        frame.setPayloadLen(payload.length);
        frame.setPayloadB(payload);
        return frame;
    }
}
