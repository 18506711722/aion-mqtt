package cn.tpddns.aion.server.mqtt.message;

public class AionMqttUnSubackMessage extends AionMqttMessage {

    public AionMqttUnSubackMessage(byte[] bytes) {
        super(bytes);
        this.setType(MqttMessageType.UNSUBACK);
    }

    public static AionMqttUnSubackMessage create(short identifier) {
        byte[] bytes = new byte[]{
                (byte) (MqttMessageType.UNSUBACK.getType()<<4), 0x02,(byte) (identifier>>8), (byte) identifier
        };
        return new AionMqttUnSubackMessage(bytes);
    }
}
