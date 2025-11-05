package top.aion0573.mqtt.client;

import top.aion0573.mqtt.message.AionMqttMessage;
import top.aion0573.mqtt.message.AionMqttSubackMessage;

public interface AionMqttClientMessageHandler {

    void setClient(AionMqttClient client);

    abstract void onSubAck(AionMqttSubackMessage message);

    void handle(AionMqttMessage message);
}
