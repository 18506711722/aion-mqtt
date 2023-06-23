package cn.tpddns.aion.server.mqtt;

import cn.tpddns.aion.server.common.buffer.AionByteBuffer;
import cn.tpddns.aion.server.common.channel.Channel;
import cn.tpddns.aion.server.mqtt.exception.AionMqttUnknownTypeMessageException;
import cn.tpddns.aion.server.mqtt.message.*;
import cn.tpddns.aion.server.websocket.message.WebSocketBinaryMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public interface MqttChannel extends Channel {
    Logger log = LoggerFactory.getLogger(MqttChannel.class);
    /**
     * 连接
     * @param message
     * @return
     */
    boolean onConnect(AionMqttConnectMessage message);

    default void onConnectSuccess(AionMqttConnectMessage message){
        log.debug("mqtt on connect success username:{} clintId:{}", message.getUsername(), message.getClientId());
        AionMqttMessage mqttConnackMessage = AionMqttConnackMessage.create();
        this.sendMessage(mqttConnackMessage);
        this.setConnect(true);
        this.setUsername(message.getUsername());
        this.setClientId(message.getClientId());
        this.setVersion(message.getVersion());
        this.setCleanSession( message.isCleanSession());
        this.setWill(message.isWill());
        this.setWillQos(message.getWillQos());
        this.setWillPayload(message.getWillPayload());
        this.setWillTopic(message.getWillTopic());
        this.setWillRetain(message.isWillRetain());
        getContext().closeChannelByClientId(message.getClientId());
        getContext().addMqttChannel(this);
    }

    void setWillRetain(boolean willRetain);

    void setWillTopic(String willTopic);

    void setWillPayload(byte[] willPayload);

    void setWillQos(int willQos);

    void setWill(boolean will);

    void setCleanSession(boolean cleanSession);

    void setVersion(byte version);

    void setClientId(String clientId);

    void setUsername(String username);

    /**
     * 断开连接
     * @param message
     */
    default void onDisconnect(AionMqttDisconnectMessage message){
        log.debug("mqtt on disconnect clientId:{}", this.getClientId());
        this.setConnect(false);
        this.close();
    }

    /**
     * PING
     * @param message
     */
    default void onPing(AionMqttPingReqMessage message){
        log.debug("mqtt on ping clientId:{}", this.getClientId());
        AionMqttPingRespMessage respMessage = AionMqttPingRespMessage.create();
        log.debug("mqtt send pong message clientId:{}", this.getClientId());
        this.sendMessage(respMessage);
    }

    /**
     * 发布
     * @param message
     */
   default void onPublish(AionMqttPublishMessage message){
       log.debug("mqtt on publish clientId:{}  topic:{} qos:{} identifier:{} udp:{} retain:{}",
               this.getClientId(),
               message.getTopicName(),
               message.getRequestedQoS(),
               message.getIdentifier(),
               message.isDup(),
               message.isRetain()
       );

       //TODO 检测客户端是否有publish的权限
       // 先响应publish ack 消息 如果是qos =2  则先保存消息
       String topicName = message.getTopicName();
       if (message.isRetain()) {
           ReentrantReadWriteLock.ReadLock readLock = null;
           try {
               readLock = getContext().getTopicsReentrantReadWriteLock().readLock();
               readLock.lock();
               List<MqttTopic> matchTopic = getContext().getTopics().stream().filter(mqttTopic -> mqttTopic.match(topicName)).toList();
               MessageManager<String> topicRetainMessageManager = getContext().getTopicRetainMessageManager();
               for (MqttTopic topic : matchTopic) {
                   topicRetainMessageManager.saveMessage(topic.getName(), message);
               }
           } finally {
               if (readLock != null) {
                   readLock.unlock();
               }
           }
       }
       int qos = message.getRequestedQoS();

       switch (qos) {
           case 0 -> this.publish(message);
           case 1 -> {
               getMessageManager().saveMessage(message.getIdentifier(), message);
               AionMqttPubackMessage pubackMessage = AionMqttPubackMessage.create(message.getIdentifier());
               log.debug("mqtt send PUBACK message");
               this.sendMessage(pubackMessage);
               this.publish(message);
           }
           case 2 -> {
               log.debug("mqtt save message");
               getMessageManager().saveMessage(message.getIdentifier(), message);
               AionMqttPubRecMessage pubrecMessage = AionMqttPubRecMessage.create(message.getIdentifier());
               log.debug("mqtt send PUBREC message identifier:{}", pubrecMessage.getIdentifier());
               this.sendMessage(pubrecMessage);
           }
       }
   }

    default void onPubRel(AionMqttPubRelMessage message) {
        log.debug("mqtt on PUBREL clientId:{} identifier:{}", this.getClientId(), message.getIdentifier());
        AionMqttPublishMessage publishMessage = getMessageManager().getMessage(message.getIdentifier());
        if (publishMessage != null) {
            this.publish(publishMessage);
        }
        AionMqttPubCompMessage pubCompMessage = AionMqttPubCompMessage.create(message.getIdentifier());
        this.sendMessage(pubCompMessage);
    }

    default void onPubComp(AionMqttPubCompMessage message){
        log.debug("mqtt on PUBCOMP clientId:{} identifier:{}", this.getClientId(), message.getIdentifier());
        this.getMessageManager().delMessage(message.getIdentifier());
    }

    default void onPubRec(AionMqttPubRecMessage message){
        this.sendMessage(AionMqttPubRelMessage.create(message.getIdentifier()));
    }

    boolean sendMessage(AionMqttMessage message);

    default boolean publish(AionMqttPublishMessage message){
        ReentrantReadWriteLock.ReadLock readLock = null;
        try {
            readLock = getContext().getTopicsReentrantReadWriteLock().readLock();
            readLock.lock();
            List<MqttTopic> matchTopic = getContext().getTopics().stream().filter(topic -> topic != null && topic.match(message.getTopicName())).toList();
            for (MqttTopic topic : matchTopic) {
                topic.publish(message);
            }
            return true;
        } finally {
            if (readLock != null) {
                readLock.unlock();
            }
        }
    }

    /**
     * 订阅
     * @param message
     * @return
     */
    default boolean onSubscribe(AionMqttSubscribeMessage message){
        log.debug("mqtt on subscribe topic:{} qos:{} identifier:{}", message.getTopicFilter(), message.getRequestedQoS(), message.getIdentifier());
        String topicFilter = message.getTopicFilter();
        Lock lock = null;
        try {
            lock = getContext().getTopicsReentrantReadWriteLock().writeLock();
            lock.lock();
            List<MqttTopic> topics = getContext().getTopics();
            Optional<MqttTopic> topicOptional = topics.stream().filter(topic -> topicFilter.equals(topic.getName())).findFirst();
            if (topicOptional.isPresent()) {
                MqttTopic matchTopic = topicOptional.get();
                matchTopic.addSubscribeChannel(this);

                AionMqttPublishMessage retainMessage = getContext().getTopicRetainMessageManager().getMessage(matchTopic.getName());
                if (retainMessage != null) {
                    log.debug("mqtt topic:{} has retain message,send retain message to client qos:{} identifier:{}", matchTopic.getName(), retainMessage.getRequestedQoS(), retainMessage.getIdentifier());
                    this.sendMessage(retainMessage);
                }
            } else {
                MqttTopic newTopic = MqttTopic.create(topicFilter);
                newTopic.addSubscribeChannel(this);
                topics.add(newTopic);
            }
            return true;
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    /**
     * 取消订阅
     *
     * @param message
     */
    default boolean onUnSubscribe(AionMqttUnSubscribeMessage message){
        log.debug("mqtt on unsubscribe topic:{} identifier:{}", message.getTopicFilter(), message.getIdentifier());
        Lock lock = null;
        try {
            lock = getContext().getTopicsReentrantReadWriteLock().writeLock();
            lock.lock();
            List<MqttTopic> matchTopic = getContext()
                    .getTopics()
                    .stream()
                    .filter(mqttTopic -> mqttTopic.match(message.getTopicFilter()))
                    .toList();
            for (MqttTopic topic : matchTopic) {
                topic.removeSubscribeChannel(this);
                if (topic.getSubscribeChannels().isEmpty()) {
                    getContext().getTopics().remove(topic);
                }
            }
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
        return true;
    }

    String getClientId();

    default void onRead(byte[] payload){
        this.getCacheBuffer().put(payload);
        AionMqttMessage mqttMessage;
        do {
            log.debug("mqtt start decode mqtt frame clientId:{}", this.getClientId());
            long startTime = System.currentTimeMillis();
            try {
                mqttMessage = MqttFrameDecoder.decode(getCacheBuffer());
            } catch (AionMqttUnknownTypeMessageException e) {
                log.error("mqtt decode error on unknown type message,close channel");
                this.close();
                return;
            }
            log.trace("mqtt decode frame time consuming {}", System.currentTimeMillis() - startTime);
            if (mqttMessage != null) {
                log.debug("mqtt frame type is {}", mqttMessage.getType().name());
                switch (mqttMessage.getType()) {
                    case CONNECT -> {
                        boolean canConnect = this.onConnect((AionMqttConnectMessage) mqttMessage);
                        if (canConnect) {
                            this.onConnectSuccess((AionMqttConnectMessage) mqttMessage);
                        }
                    }
                    case PUBLISH -> this.onPublish((AionMqttPublishMessage) mqttMessage);
                    case SUBSCRIBE -> {
                        boolean canSubscribe = this.onSubscribe((AionMqttSubscribeMessage) mqttMessage);
                        if (canSubscribe) {
                            AionMqttMessage mqttSubackMessage = AionMqttSubackMessage.create(((AionMqttSubscribeMessage) mqttMessage).getIdentifier());
                            log.debug("mqtt send suback message identifier {}", ((AionMqttSubscribeMessage) mqttMessage).getIdentifier());
                            this.sendMessage(mqttSubackMessage);
                        }
                    }
                    case UNSUBSCRIBE -> {
                        boolean canUnSubscribe = this.onUnSubscribe((AionMqttUnSubscribeMessage) mqttMessage);
                        if (canUnSubscribe) {
                            AionMqttUnSubackMessage unSubackMessage = AionMqttUnSubackMessage.create(((AionMqttUnSubscribeMessage) mqttMessage).getIdentifier());
                            log.debug("mqtt send unsuback message identifier {}", ((AionMqttUnSubscribeMessage) mqttMessage).getIdentifier());
                            this.sendMessage(unSubackMessage);
                        }
                    }
                    case PINGREQ -> this.onPing((AionMqttPingReqMessage) mqttMessage);
                    case DISCONNECT -> this.onDisconnect((AionMqttDisconnectMessage) mqttMessage);
                    case PUBREC -> this.onPubRec((AionMqttPubRecMessage) mqttMessage);
                    case PUBREL -> this.onPubRel((AionMqttPubRelMessage) mqttMessage);
                    case PUBCOMP -> this.onPubComp((AionMqttPubCompMessage) mqttMessage);
                    default -> {
                        log.error("不受支持的mqtt 协议帧");
                    }
                }
            }
            log.debug("mqtt end decode mqtt frame clientId:{}", this.getClientId());
        } while (mqttMessage != null);
    }

    AionByteBuffer getCacheBuffer();

    default void onClose(){
        log.debug("mqtt channel on close");
        if (this.isWill()) {
            AionMqttPublishMessage willMessage = AionMqttPublishMessage.create(this.getWillTopic(), this.getWillPayload(), this.getWillQos(), this.getWillRetain());
        }

        List<MqttTopic> topics = getContext().getTopics();
        for (MqttTopic topic : topics) {
            topic.removeSubscribeChannel(this);
        }
        getContext().removeMqttChannel(this);
    }

    MqttServerContext getContext();

    boolean isWill();

    String getWillTopic();

    byte[] getWillPayload();

    int getWillQos();

    boolean getWillRetain();

    void setConnect(boolean connect);

    MessageManager<Short> getMessageManager();
}
