package cn.tpddns.aion.server.mqtt.message;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MemoryChannelMessageManager implements MessageManager<Short> {
    private Map<Short, AionMqttPublishMessage> store;

    public MemoryChannelMessageManager() {
        this.store = Collections.synchronizedMap(new HashMap<>());
    }

    @Override
    public boolean saveMessage(Short identifier,AionMqttPublishMessage message) {
        if(!store.containsKey(identifier)){
            store.put(message.getIdentifier(), message);
        }
        return true;
    }

    @Override
    public AionMqttPublishMessage getMessage(Short identifier) {
        return store.get(identifier);
    }

    @Override
    public boolean delMessage(Short identifier) {
        store.remove(identifier);
        return true;
    }

}
