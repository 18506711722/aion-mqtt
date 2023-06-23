package cn.tpddns.aion.server.mqtt.message;

public class AionMqttSubackMessage extends AionMqttMessage {

    public AionMqttSubackMessage(byte[] bytes) {
        super(bytes);
        this.setType(MqttMessageType.SUBACK);
    }

    public static AionMqttSubackMessage create(short identifier) {
        byte[] bytes = new byte[]{
                (byte) 0x90, 0x02, (byte) (identifier>>8), (byte) identifier
        };
        return new AionMqttSubackMessage(bytes);
    }
}
