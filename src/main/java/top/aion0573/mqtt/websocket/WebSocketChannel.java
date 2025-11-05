package top.aion0573.mqtt.websocket;

import lombok.Getter;
import lombok.Setter;
import top.aion0573.mqtt.common.channel.Channel;
import top.aion0573.mqtt.common.buffer.AionByteBuffer;
import top.aion0573.mqtt.http.HttpChannel;
import top.aion0573.mqtt.utils.StringUtils;
import top.aion0573.mqtt.websocket.frame.*;
import top.aion0573.mqtt.websocket.message.WebSocketBinaryMessage;
import top.aion0573.mqtt.websocket.message.WebSocketMessage;
import top.aion0573.mqtt.websocket.message.WebSocketTextMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Slf4j
public class WebSocketChannel extends HttpChannel {
    @Setter
    @Getter
    private Channel channel;

    public WebSocketChannel(Socket socket) {
        super(socket);
        this.setChannel(new HttpChannel(socket));
    }

    public WebSocketChannel(HttpChannel channel) {
        super(channel.getSocket());
        this.setServer(channel.getServer());
        this.setChannel(channel);
        this.setReadCache(channel.getReadCache());
        if (this.getReadCache().getReadableSize() > 0) {
            this.onRead(this.getReadCache());
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.channel.write(b);
    }

    @Override
    public void onRead(AionByteBuffer buffer) {
        WebSocketFrame frame;
        do {
            frame = WebSocketFrameDecoder.decode(buffer);
            if (frame != null) {
                if (!frame.isMask()) {
                    this.close();
                    return;
                }
                if (WebSocketFrameOpcode.CONTINUOUS == frame.getOpcode()) {
                    //TODO websocket 分片帧未处理 网络不好的时候大概率会发生
                    System.out.println("分片数据");
                    System.out.println(StringUtils.byteArrayToHexString(frame.getPayloadB()," "));
//                    throw new RuntimeException("尚未支持的数据帧");
                } else if (WebSocketFrameOpcode.TEXT == frame.getOpcode()) {
                    this.onTextMessage(new WebSocketTextMessage(new String(frame.getPayloadB(), StandardCharsets.UTF_8), frame.isLast()));
                } else if (WebSocketFrameOpcode.BINARY == frame.getOpcode()) {
                    this.onBinaryMessage(new WebSocketBinaryMessage(frame.getPayloadB(), frame.isLast()));
                } else if (WebSocketFrameOpcode.DISCONNECT == frame.getOpcode()) {
                    this.close();
                } else if (WebSocketFrameOpcode.PING == frame.getOpcode()) {
                    this.onPing();
                } else {
                    throw new RuntimeException("不受支持的websocket 帧  opcode=" + frame.getOpcode());
                }
            }
        } while (frame != null);
    }

    public void onTextMessage(WebSocketTextMessage message) {

    }

    public void onBinaryMessage(WebSocketBinaryMessage message) {

    }

    public void onPing() {
        WebSocketFrame frame = new WebSocketPongFrame();
        try {
            this.sendFrame(frame);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(WebSocketMessage<?> message) throws IOException {
        WebSocketFrame frame = WebSocketFrameEncoder.encoder(message);
        this.sendFrame(frame);
    }

    public void sendFrame(WebSocketFrame frame) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        byte f = (byte) ((frame.isLast() ? 1 : 0) << 7 | frame.getRsv1() << 6 | frame.getRsv2() << 5 | frame.getRsv3() << 4 | frame.getOpcode());
        buffer.put(f);
        byte t = (byte) ((frame.isMask() ? 1 : 0) << 7);
        int payloadLen = frame.getPayloadLen();
        if (payloadLen >= 0 && payloadLen < 126) {
            t |= payloadLen;
            buffer.put(t);
        } else if (payloadLen > 126 && payloadLen <= 65535) {
            t |= 126;
            buffer.put(t);
            buffer.put((byte) (payloadLen >> 8));
            buffer.put((byte) payloadLen);
        } else if (payloadLen > 65535) {
            throw new RuntimeException("发送的数据帧太大了，以后再处理把");
        }

        byte[] payloadB = frame.getPayloadB();
        if (payloadLen > 0) {
            if (frame.isMask()) {
                byte[] maskingKey = new byte[4];
                new Random().nextBytes(maskingKey);
                buffer.put(maskingKey);

                for (int i = 0; i < payloadLen; i++) {
                    payloadB[i] = (byte) (payloadB[i] ^ maskingKey[i % 4]);
                }
            }
            buffer.put(payloadB);
        }

        byte[] sendB = new byte[buffer.position()];
        buffer.flip();
        buffer.get(sendB);

        this.write(sendB);
    }
}
