package cn.tpddns.aion.server.mqtt;

import cn.tpddns.aion.server.mqtt.message.AionMqttPubCompMessage;
import cn.tpddns.aion.server.mqtt.message.AionMqttPublishMessage;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MqttTopic {

    private String name;

    public String getName() {
        return name;
    }

    //    private int qos;

    private Set<MqttChannel> subscribeChannels;

    private ReentrantReadWriteLock subscribeChannelsReentrantReadWriteLock;

    public Set<MqttChannel> getSubscribeChannels() {
        return subscribeChannels;
    }

    public ReentrantReadWriteLock getSubscribeChannelsReentrantReadWriteLock() {
        return subscribeChannelsReentrantReadWriteLock;
    }

    public MqttTopic(String name) {
        this.name = name;
        this.subscribeChannels = new HashSet<>();
        this.subscribeChannelsReentrantReadWriteLock = new ReentrantReadWriteLock();
    }

    // TODO Retain  消息实现   topic 别名的实现等等等


    @Override
    public boolean equals(Object obj) {
        return this.name.equals(((MqttTopic) obj).name);
    }

    /**
     * 是否匹配
     *
     * @param topicFilter
     * @return
     */
    public boolean match(String topicFilter) {
        String[] topicNameArr = this.name.split("/");
        String[] topicFilterArr = topicFilter.split("/");
        boolean isMatch = false;
        int minLen = Math.min(topicNameArr.length, topicFilterArr.length);
        int maxLen = Math.max(topicNameArr.length, topicFilterArr.length);
        for (int i = 0; i < minLen; i++) {
            String topicNameLayer = topicNameArr[i];
            String topicFilterLayer = topicFilterArr[i];
            if (topicNameLayer.equals("#") || topicFilterLayer.equals("#")) {
                isMatch = true;
                break;
            }
            if (!topicNameLayer.equals("+") && !topicFilterLayer.equals("+") && !topicFilterLayer.equals(topicNameLayer)) {
                isMatch = false;
                break;
            }
            if (i == minLen - 1) {
                if (maxLen - minLen == 1) {
                    String[] target = topicFilterArr.length > topicNameArr.length ? topicFilterArr : topicNameArr;
                    String nextLayer = target[i + 1];
                    if (
                            (nextLayer.equals("+") && target.length == i + 2)
                            || nextLayer.equals("#")
                    ) {
                        isMatch = true;
                    }
                } else {
                    isMatch = true;
                }
            }
        }
        return isMatch;
    }

    public void addSubscribeChannel(MqttChannel channel) {
        ReentrantReadWriteLock.WriteLock writeLock = null;
        try {
            writeLock = this.getSubscribeChannelsReentrantReadWriteLock().writeLock();
            writeLock.lock();
            this.subscribeChannels.add(channel);
        } finally {
            if (writeLock != null) {
                writeLock.unlock();
            }
        }
    }


    public void removeSubscribeChannel(MqttChannel channel) {
        ReentrantReadWriteLock.WriteLock writeLock = null;
        try {
            writeLock = this.getSubscribeChannelsReentrantReadWriteLock().writeLock();
            writeLock.lock();
            this.getSubscribeChannels().remove(channel);
        } finally {
            if (writeLock != null) {
                writeLock.unlock();
            }
        }
    }

    public void publish(AionMqttPublishMessage message) {
        ReentrantReadWriteLock.ReadLock readLock = null;
        try {
            readLock = this.getSubscribeChannelsReentrantReadWriteLock().readLock();
            readLock.lock();
            Set<MqttChannel> mqttSubscribeChannels = this.getSubscribeChannels();
            for (MqttChannel mqttChannel : mqttSubscribeChannels) {
                mqttChannel.sendMessage(message);
            }
        } finally {
            if (readLock != null) {
                readLock.unlock();
            }
        }
    }

    public boolean isSubscribe(MqttChannel channel) {
        ReentrantReadWriteLock.ReadLock readLock = null;
        try {
            readLock = this.getSubscribeChannelsReentrantReadWriteLock().readLock();
            readLock.lock();
            return this.getSubscribeChannels().contains(channel);
        } finally {
            if (readLock != null) {
                readLock.unlock();
            }
        }
    }

    public static MqttTopic create(String name) {
        return new MqttTopic(name);
    }

}
