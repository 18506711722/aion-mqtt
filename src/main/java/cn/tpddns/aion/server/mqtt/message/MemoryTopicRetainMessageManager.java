package cn.tpddns.aion.server.mqtt.message;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MemoryTopicRetainMessageManager implements MessageManager<String> {
    private Map<String, AionMqttPublishMessage> store;

    public MemoryTopicRetainMessageManager() {
        this.store = Collections.synchronizedMap(new HashMap<>());
    }

    @Override
    public boolean saveMessage(String topicName,AionMqttPublishMessage message) {
        if(!store.containsKey(topicName)){
            store.put(topicName, message);
        }
        return true;
    }

    @Override
    public AionMqttPublishMessage getMessage(String topicName) {
        return store.get(topicName);
    }

    @Override
    public boolean delMessage(String topicName) {
        store.remove(topicName);
        return true;
    }

}
