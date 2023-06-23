package cn.tpddns.aion.server.mqtt.message;

import cn.tpddns.aion.server.common.buffer.AionByteBuffer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class AionMqttSubscribeMessage extends AionMqttMessage {
    private short identifier;

    private String topicFilter;

    private int requestedQoS;

    public short getIdentifier() {
        return identifier;
    }

    public void setIdentifier(short identifier) {
        this.identifier = identifier;
    }

    public String getTopicFilter() {
        return topicFilter;
    }

    public void setTopicFilter(String topicFilter) {
        this.topicFilter = topicFilter;
    }

    public int getRequestedQoS() {
        return requestedQoS;
    }

    public void setRequestedQoS(int requestedQoS) {
        this.requestedQoS = requestedQoS;
    }

    public AionMqttSubscribeMessage(byte[] bytes) {
        super(bytes);
        this.setType(MqttMessageType.SUBSCRIBE);
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


        //TODO mqtt5 这里解析应该是由变动的

        byte topicFilterLenM = buffer.get();
        byte topicFilterLenL = buffer.get();
        int topicFilterLen = Short.toUnsignedInt((short) (topicFilterLenM << 8 | topicFilterLenL));

        byte[] topicFilterB = new byte[topicFilterLen];
        buffer.get(topicFilterB);

        String topicFilter = new String(topicFilterB, StandardCharsets.UTF_8);
        this.setTopicFilter(topicFilter);
        byte requestedQoS = buffer.get();
        this.setRequestedQoS(Byte.toUnsignedInt(requestedQoS));
    }

    public static AionMqttSubscribeMessage parse(byte[] bytes) {
        return new AionMqttSubscribeMessage(bytes);
    }

    public static void main(String[] args) {
        byte[] b = new byte[]{
                (byte) 0x82, 0x0A, 0x00, 0x01, 0x00, 0x05, 0x74, 0x6f, 0x70, 0x69, 0x63, 0x00
        };
        AionMqttSubscribeMessage.parse(b);
    }
}
