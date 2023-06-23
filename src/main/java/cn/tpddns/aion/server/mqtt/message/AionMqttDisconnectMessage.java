package cn.tpddns.aion.server.mqtt.message;

public class AionMqttDisconnectMessage extends AionMqttMessage {

    public AionMqttDisconnectMessage(byte[] bytes) {
        super(bytes);
        this.setType(MqttMessageType.DISCONNECT);
    }

    public static AionMqttDisconnectMessage parse(byte[] bytes) {
        return new AionMqttDisconnectMessage(bytes);
    }
}
