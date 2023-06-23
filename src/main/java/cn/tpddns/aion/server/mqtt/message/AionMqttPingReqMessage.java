package cn.tpddns.aion.server.mqtt.message;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class AionMqttPingReqMessage extends AionMqttMessage {
    public AionMqttPingReqMessage(byte[] bytes) {
        super(bytes);
        this.setType(MqttMessageType.PINGREQ);
    }

    public static AionMqttPingReqMessage parse(byte[] bytes) {
        return new AionMqttPingReqMessage(bytes);
    }
}
