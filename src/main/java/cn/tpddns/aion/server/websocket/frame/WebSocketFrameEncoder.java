package cn.tpddns.aion.server.websocket.frame;

import cn.tpddns.aion.server.websocket.exception.WebSocketNotMaskException;
import cn.tpddns.aion.server.websocket.message.WebSocketMessage;
import cn.tpddns.aion.server.websocket.message.WebSocketTextMessage;

import java.nio.ByteBuffer;

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
