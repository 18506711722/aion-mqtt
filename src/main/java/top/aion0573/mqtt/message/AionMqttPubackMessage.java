package top.aion0573.mqtt.message;

public class AionMqttPubackMessage extends AionMqttMessage {

    public AionMqttPubackMessage(byte[] bytes) {
        super(bytes);
        this.setType(MqttMessageType.PUBACK);
    }

    public static AionMqttPubackMessage create(short identifier) {
        byte[] bytes = new byte[]{
                (byte) (MqttMessageType.PUBACK.getType()<<4), 0x02, (byte) (identifier >> 8), (byte) identifier
        };
        return new AionMqttPubackMessage(bytes);
    }

    public static AionMqttPubackMessage parse(byte[] bytes) {
        return new AionMqttPubackMessage(bytes);
    }
}
