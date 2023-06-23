package cn.tpddns.aion.server.websocket.frame;

public class WebSocketFrameOpcode {

    public static byte CONTINUOUS = 0x00;
    public static byte TEXT = 0x01;
    public static byte BINARY = 0x02;
    public static byte DISCONNECT = 0x08;
    public static byte PING = 0x09;
    public static byte PONG= 0x0A;
}
