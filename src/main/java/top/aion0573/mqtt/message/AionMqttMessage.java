package top.aion0573.mqtt.message;

import top.aion0573.mqtt.common.message.Message;
import top.aion0573.mqtt.exception.AionMqttUnknownTypeMessageException;
import top.aion0573.mqtt.utils.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Date;

public abstract class AionMqttMessage implements Message {
    @Getter
    @Setter
    private Date createDate;
    @Getter
    @Setter
    private byte[] bytes;

    @Override
    public byte[] asByte() {
        return bytes;
    }

    /**
     * MQTT message type
     */
    @Setter
    @Getter
    private MqttMessageType type;
    @Setter
    @Getter
    private String protocol;

    public AionMqttMessage() {

    }

    public AionMqttMessage(byte[] bytes) {
        this.bytes = bytes;
        this.createDate = new Date();
    }


    public static AionMqttMessage parse(byte[] bytes) throws AionMqttUnknownTypeMessageException {
        byte flag = bytes[0];
        int intType = Byte.toUnsignedInt(flag) >> 4;
        MqttMessageType type = MqttMessageType.ofType(intType);
        AionMqttMessage message;
        switch (type) {
            case CONNECT -> {
                message = AionMqttConnectMessage.parse(bytes);
            }
            case CONNACK -> {
                message = AionMqttConnackMessage.parse(bytes);
            }
            case SUBSCRIBE -> {
                message = AionMqttSubscribeMessage.parse(bytes);
            }
            case SUBACK -> {
                message = AionMqttSubackMessage.parse(bytes);
            }
            case UNSUBSCRIBE -> {
                message = AionMqttUnSubscribeMessage.parse(bytes);
            }
            case UNSUBACK -> {
                message = AionMqttUnSubackMessage.parse(bytes);
            }
            case PUBLISH -> {
                message = AionMqttPublishMessage.parse(bytes);
            }
            case PINGREQ -> {
                message = AionMqttPingReqMessage.parse(bytes);
            }
            case DISCONNECT -> {
                message = AionMqttDisconnectMessage.parse(bytes);
            }
            case PUBACK -> {
                message = AionMqttPubackMessage.parse(bytes);
            }
            case PUBREC -> {
                message = AionMqttPubRecMessage.parse(bytes);
            }
            case PUBREL -> {
                message = AionMqttPubRelMessage.parse(bytes);
            }
            case PUBCOMP -> {
                message = AionMqttPubCompMessage.parse(bytes);
            }
            default -> throw new AionMqttUnknownTypeMessageException("不受支持的消息类型");
        }
        message.setCreateDate(new Date());
        return message;
    }

    @Override
    public String toString() {
        return "AionMqttMessage{" +
                "createDate=" + DateUtils.format(createDate, "yyyy-MM-dd HH:mm:ss") +
                ", bytes=" + Arrays.toString(bytes) +
                ", type=" + type +
                ", protocol='" + protocol + '\'' +
                '}';
    }
}
