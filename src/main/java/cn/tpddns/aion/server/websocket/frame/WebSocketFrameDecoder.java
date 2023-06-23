package cn.tpddns.aion.server.websocket.frame;

import cn.tpddns.aion.server.common.buffer.AionByteBuffer;
import cn.tpddns.aion.server.common.buffer.exception.BufferReadOverflowException;

public class WebSocketFrameDecoder {

    public static WebSocketFrame decode(AionByteBuffer buffer)  {
        try {
            buffer.mark();
            byte f = buffer.get();
            boolean isLast = (f >> 7 & 0x01) > 0;
            int rsv1 = f >> 6 & 0x01;
            int rsv2 = f >> 5 & 0x01;
            int rsv3 = f >> 4 & 0x01;
            byte opcode = (byte) (f & 0x0f);  //00 -02 是数据帧 01好像是文本 02好像是二进制

            byte t = buffer.get();
            boolean isMask = (t >> 7 & 0x01) > 0;
            int payloadLen = Byte.toUnsignedInt((byte) (t & 0x7f));

            if (payloadLen > 0 && payloadLen < 126) {
                // 正常的数据包长度
            } else {
                if (payloadLen == 126) {   // 后续两个字节代表长度
                    payloadLen = Short.toUnsignedInt((short) (buffer.get() << 8 | buffer.get()));
                } else if (payloadLen == 127) {  //后续8个字节
                    payloadLen = (int) Integer.toUnsignedLong(buffer.get() << 56 | buffer.get() << 48 | buffer.get() << 40 | buffer.get() << 32 | buffer.get() << 24 | buffer.get() << 16 | buffer.get() << 8 | buffer.get());
                }
            }

            byte[] maskingKeyB = null;
            if (isMask) {
                maskingKeyB = new byte[4];
                buffer.get(maskingKeyB);
            }

            byte[] payloadB = new byte[payloadLen];
            buffer.get(payloadB);

            if (isMask) {
                for (int i = 0; i < payloadLen; i++) {
                    payloadB[i] = (byte) (payloadB[i] ^ maskingKeyB[i % 4]);
                }
            }

            buffer.compact();

            WebSocketFrame frame = new WebSocketFrame();
            frame.setLast(isLast);
            frame.setRsv1(rsv1);
            frame.setRsv2(rsv2);
            frame.setRsv3(rsv3);
            frame.setOpcode(opcode);
            frame.setMask(isMask);
            frame.setPayloadLen(payloadLen);
            frame.setMaskingKeyB(maskingKeyB);
            frame.setPayloadB(payloadB);

            return frame;
        }catch (BufferReadOverflowException e){
            buffer.reset();
        }
        return null;
    }
}
