package top.aion0573.mqtt.message;

public interface MessageManager<T> {

    boolean saveMessage(T key,AionMqttPublishMessage message);

    AionMqttPublishMessage getMessage(T key);

    boolean delMessage(T key);


}
