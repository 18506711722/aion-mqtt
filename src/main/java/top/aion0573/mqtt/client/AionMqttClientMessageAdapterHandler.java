package top.aion0573.mqtt.client;

import lombok.Getter;
import lombok.Setter;
import top.aion0573.mqtt.message.AionMqttMessage;
import top.aion0573.mqtt.message.AionMqttSubackMessage;

public class AionMqttClientMessageAdapterHandler implements AionMqttClientMessageHandler {
    @Getter
    @Setter
    private AionMqttClient client;


    @Override
    public void onSubAck(AionMqttSubackMessage message) {
    }

    @Override
    public void handle(AionMqttMessage message) {

    }
}
