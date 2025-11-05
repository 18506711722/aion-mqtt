package top.aion0573.mqtt.message;

import lombok.Getter;

public class AionMqttSubackMessage extends AionMqttMessage {
    @Getter
    final private short identifier;

    public AionMqttSubackMessage(byte[] bytes) {
        super(bytes);
        this.setType(MqttMessageType.SUBACK);
        this.identifier = (short) (bytes[2] << 8 | bytes[3]);
    }

    public static AionMqttSubackMessage parse(byte[] bytes) {
        return new AionMqttSubackMessage(bytes);
    }

    public static AionMqttSubackMessage create(short identifier) {
        byte[] bytes = new byte[]{
                (byte) 0x90, 0x02, (byte) (identifier >> 8), (byte) identifier
        };
        return new AionMqttSubackMessage(bytes);
    }
}
