package top.aion0573.mqtt.message;

import lombok.NonNull;
import top.aion0573.mqtt.common.buffer.AionByteBuffer;

import java.nio.charset.StandardCharsets;
import java.util.List;

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
        this.setIdentifier((short) (Byte.toUnsignedInt(identifierM) << 8 | Byte.toUnsignedInt(identifierL)));

        byte topicFilterLenM = buffer.get();
        byte topicFilterLenL = buffer.get();
        int topicFilterLen = Short.toUnsignedInt((short) (topicFilterLenM << 8 | topicFilterLenL));

        byte[] topicFilterB = new byte[topicFilterLen];
        buffer.get(topicFilterB);

        String topicFilter = new String(topicFilterB, StandardCharsets.UTF_8);
        this.setTopicFilter(topicFilter);
    }

    public static AionMqttUnSubscribeMessage create(short identifier, @NonNull List<String> topicFilters) {
        byte controlPacketType = (byte) (MqttMessageType.UNSUBSCRIBE.getType() << 4);
        int remainingLength = 0;
        // 标识符字节位
        remainingLength += 2;
        for (String topicFilter : topicFilters) {
            remainingLength += 2;
            remainingLength += (byte) topicFilter.getBytes(StandardCharsets.UTF_8).length;
        }

        if (remainingLength + 2 > 255) {
            throw new RuntimeException(String.format("mqtt message length too large,packetLength=%s", remainingLength));
        }

        AionByteBuffer buffer = AionByteBuffer.allocate(remainingLength + 2);
        buffer.put(controlPacketType);
        buffer.put((byte) remainingLength);
        buffer.put(new byte[]{
                (byte) (identifier >> 8),
                (byte) identifier
        });
        for (String topicFilter : topicFilters) {
            byte[] topicFilterB = topicFilter.getBytes(StandardCharsets.UTF_8);
            buffer.put(new byte[]{
                    (byte) (topicFilterB.length >> 8),
                    (byte) topicFilterB.length
            });
            buffer.put(topicFilterB);
        }
        return new AionMqttUnSubscribeMessage(buffer.getEffectiveBytes());
    }

    public static AionMqttUnSubscribeMessage parse(byte[] bytes) {
        return new AionMqttUnSubscribeMessage(bytes);
    }
}
