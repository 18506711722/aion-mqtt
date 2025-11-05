package top.aion0573.mqtt.message;

public class AionMqttPingRespMessage extends AionMqttMessage {
    public AionMqttPingRespMessage(byte[] bytes) {
        super(bytes);
        this.setType(MqttMessageType.PINGRESP);
    }

    public static AionMqttPingRespMessage create() {
        byte type = (byte) (MqttMessageType.PINGRESP.getType() << 4);
        AionMqttPingRespMessage message = new AionMqttPingRespMessage(new byte[]{type, 0x00});
        return message;
    }

    public static AionMqttPingRespMessage parse(byte[] bytes) {
        return new AionMqttPingRespMessage(bytes);
    }
}
