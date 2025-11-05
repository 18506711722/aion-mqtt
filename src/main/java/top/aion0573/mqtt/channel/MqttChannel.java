package top.aion0573.mqtt.channel;

import top.aion0573.mqtt.decoder.MqttFrameDecoder;
import top.aion0573.mqtt.server.AionMqttServerContext;
import top.aion0573.mqtt.topic.MqttTopic;
import top.aion0573.mqtt.common.buffer.AionByteBuffer;
import top.aion0573.mqtt.common.channel.Channel;
import top.aion0573.mqtt.exception.AionMqttUnknownTypeMessageException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import top.aion0573.mqtt.message.*;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public abstract class MqttChannel extends SocketChannel {

    @Setter
    @Getter
    private MessageManager<Short> messageManager;
    @Setter
    @Getter
    private Channel channel;

    /**
     * mqtt 是否已经连接
     */
    @Setter
    @Getter
    private boolean connect;

    /**
     * mqtt 协议版本
     */
    @Setter
    @Getter
    private byte version;
    @Setter
    @Getter
    private boolean cleanSession;
    @Setter
    @Getter
    private String username;
    @Setter
    @Getter
    private String clientId;
    @Setter
    @Getter
    private boolean will;
    @Setter
    @Getter
    private int willQos;
    @Setter
    @Getter
    private byte[] willPayload;
    @Setter
    @Getter
    private String willTopic;
    @Setter
    @Getter
    private boolean willRetain;
    @Setter
    @Getter
    private int keepAlive;
    @Setter
    @Getter
    private Date lastPingTime;
    @Setter
    @Getter
    private Date connectedTime;


    public MqttChannel(Socket socket) {
        super(socket);
    }


    public AionMqttServerContext getContext() {
        return AionMqttServerContext.getInstance();
    }

    /**
     * 连接回调 可以做用户验证等等操作
     *
     * @param message
     * @return
     */
    boolean onConnect(AionMqttConnectMessage message) {
        return true;
    }

    /**
     * 连接成功后的操作
     *
     * @param message
     */
    void onConnectSuccess(AionMqttConnectMessage message) {
        log.debug("mqtt on connect success username:{} clintId:{}", message.getUsername(), message.getClientId());
        AionMqttMessage mqttConnackMessage = AionMqttConnackMessage.create();
        this.sendMessage(mqttConnackMessage);
        this.setConnect(true);
        this.setConnectedTime(new Date());
        this.setUsername(message.getUsername());
        this.setClientId(message.getClientId());
        this.setVersion(message.getVersion());
        this.setCleanSession(message.isCleanSession());
        this.setWill(message.isWill());
        this.setWillQos(message.getWillQos());
        this.setWillPayload(message.getWillPayload());
        this.setWillTopic(message.getWillTopic());
        this.setWillRetain(message.isWillRetain());
        this.setKeepAlive(message.getKeepAlive());

        // 相同客户端ID连接 关闭之前的连接
        getContext().closeChannelByClientId(message.getClientId());
        getContext().addMqttChannel(this);

    }

    /**
     * 断开连接
     *
     * @param message
     */
    void onDisconnect(AionMqttDisconnectMessage message) {
        log.debug("mqtt on disconnect clientId:{}", this.getClientId());
        this.close();
        this.setConnect(false);
    }

    /**
     * PING
     *
     * @param message
     */
    void onPing(AionMqttPingReqMessage message) {
        log.debug("mqtt on ping clientId:{}", this.getClientId());
        AionMqttPingRespMessage respMessage = AionMqttPingRespMessage.create();
        log.debug("mqtt send pong message clientId:{}", this.getClientId());
        this.sendMessage(respMessage);
        this.setLastPingTime(new Date());


        System.out.println("mqtt send pong message clientId:" + this.getClientId());
    }

    /**
     * 发布
     *
     * @param message
     */
    void onPublish(AionMqttPublishMessage message) {
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

    void onPubRel(AionMqttPubRelMessage message) {
        log.debug("mqtt on PUBREL clientId:{} identifier:{}", this.getClientId(), message.getIdentifier());
        AionMqttPublishMessage publishMessage = getMessageManager().getMessage(message.getIdentifier());
        if (publishMessage != null) {
            this.publish(publishMessage);
        }
        AionMqttPubCompMessage pubCompMessage = AionMqttPubCompMessage.create(message.getIdentifier());
        this.sendMessage(pubCompMessage);
    }

    void onPubComp(AionMqttPubCompMessage message) {
        log.debug("mqtt on PUBCOMP clientId:{} identifier:{}", this.getClientId(), message.getIdentifier());
        this.getMessageManager().delMessage(message.getIdentifier());
    }

    void onPubRec(AionMqttPubRecMessage message) {
        this.sendMessage(AionMqttPubRelMessage.create(message.getIdentifier()));
    }

    public abstract boolean sendMessage(AionMqttMessage message);

    boolean publish(AionMqttPublishMessage message) {
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
     *
     * @param message
     * @return
     */
    boolean onSubscribe(AionMqttSubscribeMessage message) {
        log.debug("mqtt on subscribe topic:{} qos:{} identifier:{}", message.getTopicFilters(), message.getRequestedQoS(), message.getIdentifier());
        List<String> topicFilters = message.getTopicFilters();
        Lock lock = null;
        try {
            lock = getContext().getTopicsReentrantReadWriteLock().writeLock();
            lock.lock();
            List<MqttTopic> topics = getContext().getTopics();
            for (String topicFilter : topicFilters) {
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
    boolean onUnSubscribe(AionMqttUnSubscribeMessage message) {
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

    @Override
    public void onRead(AionByteBuffer buffer) {
        AionMqttMessage mqttMessage;
        do {
            log.debug("mqtt start decode mqtt frame clientId:{}", this.getClientId());
            long startTime = System.currentTimeMillis();
            try {
                mqttMessage = MqttFrameDecoder.decode(buffer);
            } catch (AionMqttUnknownTypeMessageException e) {
                log.error("mqtt decode error on unknown type message,close channel");
                this.close();
                return;
            }
            log.trace("mqtt decode frame time consuming {}", System.currentTimeMillis() - startTime);
            if (mqttMessage != null) {
                log.info("mqtt channel receive mqtt {} message:{}", mqttMessage.getType().name(), mqttMessage);
                switch (mqttMessage.getType()) {
                    case CONNECT -> {
                        if (!this.isConnect()) {
                            boolean canConnect = this.onConnect((AionMqttConnectMessage) mqttMessage);
                            if (canConnect) {
                                this.onConnectSuccess((AionMqttConnectMessage) mqttMessage);
                            }
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

//    @Override
//    public InputStream getInputStream() throws IOException {
//        return super.getInputStream();
//    }
//
//    @Override
//    public OutputStream getOutputStream() throws IOException {
//        return super.getOutputStream();
//    }

    @Override
    public void onClose() {
        super.onClose();
        log.debug("mqtt channel on close");
        if (this.isWill()) {
            AionMqttPublishMessage willMessage = AionMqttPublishMessage.create(this.getWillTopic(), this.getWillPayload(), this.getWillQos(), this.isWillRetain());
        }

        List<MqttTopic> topics = getContext().getTopics();
        for (MqttTopic topic : topics) {
            topic.removeSubscribeChannel(this);
        }
        getContext().removeMqttChannel(this);
    }

    @Override
    public String toString() {
        return "MqttChannel{" +
                "username='" + username + '\'' +
                ", clientId='" + clientId + '\'' +
                ", cleanSession=" + cleanSession +
                ", version=" + version +
                ", connect=" + connect +
                ", will=" + will +
                ", willQos=" + willQos +
                ", willRetain=" + willRetain +
                ", keepAlive=" + keepAlive +
                '}';


    }
}
