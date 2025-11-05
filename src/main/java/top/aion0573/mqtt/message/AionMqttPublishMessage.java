package top.aion0573.mqtt.message;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class AionMqttPublishMessage extends AionMqttMessage {
    private short identifier;
    private String topicName;

    private byte[] payload;


    private int requestedQoS;

    private boolean retain;

    private boolean dup;

    public byte[] getPayload() {
        return payload;
    }

    public short getIdentifier() {
        return identifier;
    }

    public void setIdentifier(short identifier) {
        this.identifier = identifier;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public int getRequestedQoS() {
        return requestedQoS;
    }

    public void setRequestedQoS(int requestedQoS) {
        this.requestedQoS = requestedQoS;
    }

    public boolean isRetain() {
        return retain;
    }

    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    public boolean isDup() {
        return dup;
    }

    public void setDup(boolean dup) {
        this.dup = dup;
    }

    public AionMqttPublishMessage(String topicName, byte[] willPayload, int qos, boolean retain) {
        this.topicName = topicName;
        this.payload = willPayload;
        this.setRequestedQoS(qos);
        this.retain = retain;
        this.dup = false;

        byte f = (byte) (((byte) (MqttMessageType.PUBLISH.getType()) << 4));
        f |= (isDup() ? 0x01 : 0x00) << 3;
        f |= this.retain ? 0x01 : 0x00;

        int topicNameLen = this.topicName.length();
        byte[] topicNameB = this.topicName.getBytes(StandardCharsets.UTF_8);


//        byte[] b = new byte[]{
//                f,0x00, (byte) topicNameLen,...topicNameB,
////
//        };
//       super();
    }

    public AionMqttPublishMessage(byte[] bytes) {
        super(bytes);
        this.setType(MqttMessageType.PUBLISH);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        byte f = buffer.get();

        boolean retain = (f & 0x01) > 0;
        this.setRetain(retain);

        int qos = Byte.toUnsignedInt((byte) (f >> 1 & 0x03));
        this.setRequestedQoS(qos);

        boolean dup = (f >> 3 & 0x01) > 0;
        this.setDup(dup);

        byte packetLenB = buffer.get();
        int packetLen = Byte.toUnsignedInt(packetLenB);

        byte topicNameLenM = buffer.get();
        byte topicNameLenL = buffer.get();
        int topicNameLen = Short.toUnsignedInt((short) (topicNameLenM << 8 | topicNameLenL));
        byte[] topicNameB = new byte[topicNameLen];
        buffer.get(topicNameB);
        this.setTopicName(new String(topicNameB, StandardCharsets.UTF_8));

        byte identifierM = buffer.get();
        byte identifierL = buffer.get();
        short identifier = (short) (identifierM << 8 | identifierL);
        this.setIdentifier(identifier);

        int payloadLen = packetLen - topicNameLen - 4;
        byte[] payload = new byte[payloadLen];
        buffer.get(payload);
        this.payload = payload;
    }

    public static AionMqttPublishMessage parse(byte[] bytes) {
        return new AionMqttPublishMessage(bytes);
    }

    public static AionMqttPublishMessage create(byte[] bytes) {
        return new AionMqttPublishMessage(bytes);
    }

    public static AionMqttPublishMessage create(String topic, byte[] willPayload, int qos, boolean retain) {
        AionMqttPublishMessage message = new AionMqttPublishMessage(topic, willPayload, qos, retain);
        return message;
    }


}
