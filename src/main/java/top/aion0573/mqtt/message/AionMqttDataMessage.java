package top.aion0573.mqtt.message;

public class AionMqttDataMessage {
    private String topicName;

    private byte[] payload;

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public AionMqttDataMessage(String topicName, byte[] payload) {
        this.topicName = topicName;
        this.payload = payload;
    }

}
