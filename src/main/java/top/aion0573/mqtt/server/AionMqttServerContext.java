package top.aion0573.mqtt.server;

import lombok.extern.slf4j.Slf4j;
import top.aion0573.mqtt.channel.MqttChannel;
import top.aion0573.mqtt.message.MemoryTopicRetainMessageManager;
import top.aion0573.mqtt.message.MessageManager;
import top.aion0573.mqtt.topic.MqttTopic;
import top.aion0573.mqtt.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class AionMqttServerContext {
    static {
        INSTANCE = new AionMqttServerContext();
    }

    private final static AionMqttServerContext INSTANCE;

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

    public static AionMqttServerContext getInstance() {
        return INSTANCE;
    }

    private AionMqttServerContext() {
        this.topics = new ArrayList<>();
        this.mqttChannels = new ArrayList<>();
        this.mqttChannelReentrantReadWriteLock = new ReentrantReadWriteLock();
        this.topicsReentrantReadWriteLock = new ReentrantReadWriteLock();
        this.topicRetainMessageManager = new MemoryTopicRetainMessageManager();

        Thread.ofVirtual().name("mqtt-server-scan-not-keep-alive-thread").start(() -> {
            while (true) {
                try {
//                    log.debug("mqtt server start scan not keep alive mqtt channels");
                    long startTime = System.currentTimeMillis();
                    List<MqttChannel> needCloseMqttChannels = new ArrayList<>();
                    ReentrantReadWriteLock.ReadLock channelReadLock = this.mqttChannelReentrantReadWriteLock.readLock();
                    channelReadLock.lock();
                    this.getMqttChannels().forEach(mqttChannel -> {
                        int keepAlive = mqttChannel.getKeepAlive();
                        long lastAliveTime = mqttChannel.getLastPingTime() != null ? mqttChannel.getLastPingTime().getTime() : mqttChannel.getConnectedTime().getTime();
                        long currentTime = System.currentTimeMillis();
                        boolean isAlive = (currentTime - lastAliveTime) * 1.0 / 1000 < keepAlive * 1.5;
                        if (!isAlive) {
                            log.debug("mqtt channel clientId:{} keep alive timeout", mqttChannel.getClientId());
                            needCloseMqttChannels.add(mqttChannel);
                        }
                    });
                    channelReadLock.unlock();
                    //读锁中获取了写锁会导致死锁  现在放到锁外面试试
                    for(MqttChannel channel : needCloseMqttChannels) {
                        channel.close();
                    }
//                    log.debug("mqtt server end scan not keep alive mqtt channels time consumed {}ms", System.currentTimeMillis() - startTime);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
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
