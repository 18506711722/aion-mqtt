package top.aion0573.mqtt.decoder;

import top.aion0573.mqtt.common.buffer.AionByteBuffer;
import top.aion0573.mqtt.common.buffer.exception.BufferReadOverflowException;
import top.aion0573.mqtt.exception.AionMqttUnknownTypeMessageException;
import top.aion0573.mqtt.message.AionMqttMessage;

public class MqttFrameDecoder {

    public static AionMqttMessage decode(AionByteBuffer buffer) throws AionMqttUnknownTypeMessageException {
        try {
            buffer.mark();
            byte f = buffer.get();
            int type = Byte.toUnsignedInt((byte) (f >> 4));
            int frameLen = 0;
            int frameLenBytesLen = 0;
            for (int i = 0; i < 4; i++) {
                byte frameLenB = buffer.get();
                boolean hasNextLenB = (frameLenB >> 7 & 0x01) > 0;
                if (i == 0) {
                    frameLen += Byte.toUnsignedInt((byte) (frameLenB & 0x7f));
                } else {
                    frameLen += Math.pow(128, i);
                }
                frameLenBytesLen++;
                if (!hasNextLenB) {
                    break;
                }
            }
            buffer.reset();
            buffer.mark();
            byte[] frameBytes = new byte[1 + frameLenBytesLen + frameLen];
            buffer.get(frameBytes);
            buffer.compact();
            return AionMqttMessage.parse(frameBytes);
        } catch (BufferReadOverflowException e) {
            buffer.reset();
        }
        return null;
    }
}
