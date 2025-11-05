package top.aion0573.mqtt.message;

public class AionMqttPingReqMessage extends AionMqttMessage {
    public AionMqttPingReqMessage(byte[] bytes) {
        super(bytes);
        this.setType(MqttMessageType.PINGREQ);
    }

    public static AionMqttPingReqMessage parse(byte[] bytes) {
        return new AionMqttPingReqMessage(bytes);
    }
}
