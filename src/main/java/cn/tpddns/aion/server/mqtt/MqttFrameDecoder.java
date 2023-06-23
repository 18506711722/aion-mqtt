package cn.tpddns.aion.server.mqtt;

import cn.tpddns.aion.server.common.buffer.AionByteBuffer;
import cn.tpddns.aion.server.common.buffer.exception.BufferReadOverflowException;
import cn.tpddns.aion.server.common.mqtt.MqttFrameType;
import cn.tpddns.aion.server.mqtt.exception.AionMqttUnknownTypeMessageException;
import cn.tpddns.aion.server.mqtt.message.AionMqttConnackMessage;
import cn.tpddns.aion.server.mqtt.message.AionMqttConnectMessage;
import cn.tpddns.aion.server.mqtt.message.AionMqttMessage;
import cn.tpddns.aion.server.mqtt.message.MqttMessageType;
import cn.tpddns.aion.server.websocket.message.WebSocketBinaryMessage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
