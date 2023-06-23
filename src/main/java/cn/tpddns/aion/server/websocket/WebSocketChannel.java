package cn.tpddns.aion.server.websocket;

import cn.tpddns.aion.server.common.channel.AbstractChannel;
import cn.tpddns.aion.server.common.channel.Channel;
import cn.tpddns.aion.server.common.buffer.AionByteBuffer;
import cn.tpddns.aion.server.http.HttpChannel;
import cn.tpddns.aion.server.mqtt.message.AionMqttMessage;
import cn.tpddns.aion.server.websocket.frame.*;
import cn.tpddns.aion.server.websocket.message.WebSocketBinaryMessage;
import cn.tpddns.aion.server.websocket.message.WebSocketMessage;
import cn.tpddns.aion.server.websocket.message.WebSocketTextMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Slf4j
public class WebSocketChannel extends AbstractChannel {
    private Channel channel;

    public Channel getChannel() {
        return channel;
    }

    public WebSocketChannel(Channel channel) {
        this.channel = channel;
        log.debug("websocket new connect");
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.channel.write(b);
    }

    @Override
    public void onRead0(byte[] b) {
        super.onRead0(b);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.channel.getInputStream();
    }


    @Override
    public OutputStream getOutputStream() throws IOException {
        return this.channel.getOutputStream();
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
                    throw new RuntimeException("尚未支持的数据帧");
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

    public void onClose() {
    }

    @Override
    public void close() {
        this.getChannel().close();
    }

}
