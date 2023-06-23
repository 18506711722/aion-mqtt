package cn.tpddns.aion.server.mqtt.message;

import java.nio.ByteBuffer;

public class AionMqttPubRecMessage extends AionMqttMessage {
    private short identifier;

    public short getIdentifier() {
        return identifier;
    }

    public void setIdentifier(short identifier) {
        this.identifier = identifier;
    }

    public AionMqttPubRecMessage(byte[] bytes) {
        super(bytes);
        this.setType(MqttMessageType.PUBREC);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        byte packetTypeB =  buffer.get();
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
    }

    public static AionMqttPubRecMessage parse(byte[] bytes) {
        return new AionMqttPubRecMessage(bytes);
    }

    public static AionMqttPubRecMessage create(short identifier) {
        byte[] bytes = new byte[]{
                (byte) (MqttMessageType.PUBREC.getType()<<4|0x08), 0x02,(byte) (identifier>>8), (byte) identifier
        };
        return new AionMqttPubRecMessage(bytes);
    }
}
