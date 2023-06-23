package cn.tpddns.aion.server.mqtt;

import cn.tpddns.aion.server.mqtt.message.MemoryTopicRetainMessageManager;
import cn.tpddns.aion.server.mqtt.message.MessageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MqttServerContext {
    static {
        INSTANCE = new MqttServerContext();
    }

    private final static MqttServerContext INSTANCE;

    private List<MqttTopic> topics;

    private List<MqttChannel> mqttChannels;

    private ReentrantReadWriteLock mqttChannelReentrantReadWriteLock;

    private ReentrantReadWriteLock topicsReentrantReadWriteLock;

    private MessageManager<String> topicRetainMessageManager;

    public ReentrantReadWriteLock getTopicsReentrantReadWriteLock() {
        return topicsReentrantReadWriteLock;
    }

    public ReentrantReadWriteLock getMqttChannelReentrantReadWriteLock() {
        return mqttChannelReentrantReadWriteLock;
    }

    public MessageManager<String> getTopicRetainMessageManager() {
        return topicRetainMessageManager;
    }

    public void setTopicRetainMessageManager(MessageManager<String> topicRetainMessageManager) {
        this.topicRetainMessageManager = topicRetainMessageManager;
    }

    public static MqttServerContext getInstance() {
        return INSTANCE;
    }

    private MqttServerContext() {
        this.topics = Collections.synchronizedList(new ArrayList<>());
        this.mqttChannels = Collections.synchronizedList(new ArrayList<>());
        this.mqttChannelReentrantReadWriteLock = new ReentrantReadWriteLock();
        this.topicsReentrantReadWriteLock = new ReentrantReadWriteLock();
        this.topicRetainMessageManager = new MemoryTopicRetainMessageManager();
    }

    public List<MqttTopic> getTopics() {
        return topics;
    }

    public List<MqttChannel> getMqttChannels() {
        return mqttChannels;
    }

    public void addMqttChannel(MqttChannel channel) {
        ReentrantReadWriteLock.WriteLock writeLock = null;
        try {
            writeLock = this.mqttChannelReentrantReadWriteLock.writeLock();
            writeLock.lock();
            this.mqttChannels.add(channel);
        } finally {
            if (writeLock != null) {
                writeLock.unlock();
            }
        }
    }


    public void removeMqttChannel(MqttChannel channel) {
        ReentrantReadWriteLock.WriteLock writeLock = null;
        try {
            writeLock = this.mqttChannelReentrantReadWriteLock.writeLock();
            writeLock.lock();
            this.mqttChannels.remove(channel);
        } finally {
            if (writeLock != null) {
                writeLock.unlock();
            }
        }
    }

    public void closeChannelByClientId(String clientId) {
        ReentrantReadWriteLock.WriteLock writeLock = null;
        try {
            writeLock = this.getMqttChannelReentrantReadWriteLock().writeLock();
            writeLock.lock();
            List<MqttChannel> mqttChannels = this.getMqttChannels();
            List<MqttChannel> matchChannel = mqttChannels.stream().filter(channel -> clientId.equals(channel.getClientId())).toList();
            for (MqttChannel channel : matchChannel) {
                channel.close();
            }
        } finally {
            if (writeLock != null) {
                writeLock.unlock();
            }
        }
    }

}
