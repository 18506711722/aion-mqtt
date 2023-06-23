package cn.tpddns.aion.server.mqtt.message;

import cn.tpddns.aion.server.common.message.Message;
import cn.tpddns.aion.server.mqtt.exception.AionMqttUnknownTypeMessageException;

import java.util.Date;

public abstract class AionMqttMessage implements Message {
    private Date createDate;
    private byte[] bytes;

    @Override
    public byte[] asByte() {
        return bytes;
    }

    /**
     * MQTT message type
     */
    private MqttMessageType type;

    private String protocol;

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setType(MqttMessageType type) {
        this.type = type;
    }

    public MqttMessageType getType() {
        return type;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public AionMqttMessage(){

    }

    public AionMqttMessage(byte[] bytes) {
        this.bytes = bytes;
    }


    public static AionMqttMessage parse( byte[] bytes) throws AionMqttUnknownTypeMessageException {
        byte flag = bytes[0];
        int intType = Byte.toUnsignedInt(flag)>>4;
        MqttMessageType type = MqttMessageType.ofType(intType);
        AionMqttMessage message;
        switch (type) {
            case CONNECT -> {
                message = AionMqttConnectMessage.parse(bytes);
            }
            case SUBSCRIBE -> {
                message = AionMqttSubscribeMessage.parse(bytes);
            }
            case UNSUBSCRIBE -> {
                message = AionMqttUnSubscribeMessage.parse(bytes);
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
}
