package top.aion0573.mqtt.message;

import lombok.Getter;

public class AionMqttUnSubackMessage extends AionMqttMessage {
    @Getter
    private short identifier;

    public AionMqttUnSubackMessage(byte[] bytes) {
        super(bytes);
        this.setType(MqttMessageType.UNSUBACK);
        this.identifier = (short) (bytes[2] << 8 | bytes[3]);
    }

    public static AionMqttUnSubackMessage parse(byte[] bytes) {
        return new AionMqttUnSubackMessage(bytes);
    }

    public static AionMqttUnSubackMessage create(short identifier) {
        byte[] bytes = new byte[]{
                (byte) (MqttMessageType.UNSUBACK.getType()<<4), 0x02,(byte) (identifier>>8), (byte) identifier
        };
        return new AionMqttUnSubackMessage(bytes);
    }
}
