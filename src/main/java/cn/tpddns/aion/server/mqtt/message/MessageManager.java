package cn.tpddns.aion.server.mqtt.message;

public interface MessageManager<T> {

    boolean saveMessage(T key,AionMqttPublishMessage message);

    AionMqttPublishMessage getMessage(T key);

    boolean delMessage(T key);


}
