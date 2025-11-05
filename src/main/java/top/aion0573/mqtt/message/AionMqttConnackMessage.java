package top.aion0573.mqtt.message;

public class AionMqttConnackMessage extends AionMqttMessage {

    public AionMqttConnackMessage(byte[] bytes) {
        super(bytes);
        this.setType(MqttMessageType.CONNACK);
    }

    public static AionMqttConnackMessage parse(byte[] bytes) {
        return new AionMqttConnackMessage(bytes);
    }

    public static AionMqttConnackMessage create() {
        byte[] bytes = new byte[]{
                0x20, 0x02, 0x00, 0x00
        };
        return new AionMqttConnackMessage(bytes);
    }

}
