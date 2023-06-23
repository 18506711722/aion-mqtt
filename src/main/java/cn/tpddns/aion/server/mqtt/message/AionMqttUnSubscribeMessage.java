package cn.tpddns.aion.server.mqtt.message;

import cn.tpddns.aion.server.common.buffer.AionByteBuffer;

import java.nio.charset.StandardCharsets;

public class AionMqttUnSubscribeMessage extends AionMqttMessage {

    private short identifier;

    private String topicFilter;

    public short getIdentifier() {
        return identifier;
    }

    public String getTopicFilter() {
        return topicFilter;
    }

    public void setTopicFilter(String topicFilter) {
        this.topicFilter = topicFilter;
    }

    public void setIdentifier(short identifier) {
        this.identifier = identifier;
    }

    public AionMqttUnSubscribeMessage(byte[] bytes) {
        super(bytes);
        this.setType(MqttMessageType.UNSUBSCRIBE);
        AionByteBuffer buffer = AionByteBuffer.wrap(bytes);
        byte packetTypeB = buffer.get();
        int packetLen = 0;
        int frameLenBytesLen = 0;
        for (int i = 0; i < 4; i++) {
            byte frameLenB = buffer.get();
            boolean hasNextLenB = (frameLenB >> 7 & 0x01) > 0;
            if (i == 0) {
                packetLen += Byte.toUnsignedInt((byte) (frameLenB & 0x7f));
            } else {
                packetLen += Math.pow(128, i);
            }
            frameLenBytesLen++;
            if (!hasNextLenB) {
                break;
            }
        }

        // 可变报文标识符
        byte identifierM = buffer.get();
        byte identifierL = buffer.get();
        this.setIdentifier((short) (identifierM << 8 | identifierL));

        byte topicFilterLenM = buffer.get();
        byte topicFilterLenL = buffer.get();
        int topicFilterLen = Short.toUnsignedInt((short) (topicFilterLenM << 8 | topicFilterLenL));

        byte[] topicFilterB = new byte[topicFilterLen];
        buffer.get(topicFilterB);

        String topicFilter = new String(topicFilterB, StandardCharsets.UTF_8);
        this.setTopicFilter(topicFilter);
    }

//    public static AionMqttUnSubscribeMessage create() {
//        byte[] bytes = new byte[]{
//                (byte) (MqttMessageType.UNSUBSCRIBE.getType() >> 4), 0x00
//        };
//        return new AionMqttUnSubscribeMessage(bytes);
//    }

    public static AionMqttUnSubscribeMessage parse(byte[] bytes) {
        return new AionMqttUnSubscribeMessage(bytes);
    }
}
