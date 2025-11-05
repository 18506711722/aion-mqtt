package top.aion0573.mqtt.message;

import lombok.Getter;
import lombok.Setter;
import top.aion0573.mqtt.common.buffer.AionByteBuffer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AionMqttSubscribeMessage extends AionMqttMessage {
    @Getter
    @Setter
    private short identifier;
    @Getter
    @Setter
    private List<String> topicFilters;
    @Getter
    @Setter
    private int requestedQoS;

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
        this.setIdentifier((short) (Byte.toUnsignedInt(identifierM) << 8 | Byte.toUnsignedInt(identifierL)));

        //TODO mqtt5 这里解析应该是由变动的
        List<String> topicFilters = new ArrayList<>();
        while (buffer.getReadableSize() > 1) {
            byte topicFilterLenM = buffer.get();
            byte topicFilterLenL = buffer.get();
            int topicFilterLen = Short.toUnsignedInt((short) (topicFilterLenM << 8 | topicFilterLenL));
            byte[] topicFilterB = new byte[topicFilterLen];
            buffer.get(topicFilterB);
            String topicFilter = new String(topicFilterB, StandardCharsets.UTF_8);
            topicFilters.add(topicFilter);
        }
        this.setTopicFilters(topicFilters);
        byte requestedQoS = buffer.get();
        this.setRequestedQoS(Byte.toUnsignedInt(requestedQoS));
    }

    public static AionMqttSubscribeMessage parse(byte[] bytes) {
        return new AionMqttSubscribeMessage(bytes);
    }

    public static AionMqttSubscribeMessage create(short identifier, List<String> topicFilters, short requestedQoS) {
        int remainingLength = 0;
        byte controlPacketType = (byte) (MqttMessageType.SUBSCRIBE.getType() << 4);
        // Identifier length
        remainingLength += 2;
        for (String topicFilter : topicFilters) {
            remainingLength += 2;
            remainingLength += topicFilter.getBytes().length;
        }
        // requested qos bit length
        remainingLength += 1;
        if (remainingLength + 2 > 255) {
            throw new RuntimeException(String.format("mqtt message length too large,packetLength=%s", remainingLength));
        }
        AionByteBuffer bytes = AionByteBuffer.allocate(remainingLength + 2);
        bytes.put(controlPacketType);
        bytes.put((byte) remainingLength);
        bytes.put(new byte[]{
                (byte) (identifier >> 8),
                (byte) identifier,
        });
        for (String topicFilter : topicFilters) {
            byte[] topicFilterB = topicFilter.getBytes(StandardCharsets.UTF_8);
            bytes.put(new byte[]{
                    (byte) (topicFilterB.length >> 8),
                    (byte) topicFilterB.length
            });
            bytes.put(topicFilterB);
        }
        bytes.put((byte) requestedQoS);
        return new AionMqttSubscribeMessage(bytes.getEffectiveBytes());
    }


//    public static void main(String[] args) {
//        byte[] b = new byte[]{
//                (byte) 0x82, 0x0A, 0x00, 0x01, 0x00, 0x05, 0x74, 0x6f, 0x70, 0x69, 0x63, 0x00
//        };
//        AionMqttSubscribeMessage.parse(b);
//    }
}
