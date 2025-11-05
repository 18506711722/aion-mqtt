package top.aion0573.mqtt.client;

import lombok.Getter;

public class MqttClientTopic {
    @Getter
    private String topicFilter;
    @Getter
    private short qos;

    public MqttClientTopic(String topicFilter) {
        this.topicFilter = topicFilter;
    }

    public MqttClientTopic(String topicFilter, short qos) {
        this.topicFilter = topicFilter;
        this.qos = qos;
    }

    @Override
    public String toString() {
        return "MqttClientTopic{" +
                "topicFilter='" + topicFilter + '\'' +
                ", qos=" + qos +
                '}';
    }
}
