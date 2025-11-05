package top.aion0573.mqtt.client;

import lombok.Setter;
import top.aion0573.mqtt.common.buffer.AionByteBuffer;
import top.aion0573.mqtt.decoder.MqttFrameDecoder;
import top.aion0573.mqtt.exception.AionMqttUnknownTypeMessageException;
import top.aion0573.mqtt.message.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import top.aion0573.mqtt.topic.MqttTopic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class AionMqttClient {
    @Getter
    final private String host;
    @Getter
    final private int port;
    @Getter
    final private String clientId;
    @Getter
    final private String username;
    @Getter
    final private String password;
    @Getter
    final private int connectTimeout;
    @Getter
    final private int keepAlive;
    @Getter
    final private boolean clearSession;
    @Getter
    final private boolean autoReconnect;
    @Getter
    private Socket socket;
    @Getter
    private InputStream in;
    @Getter
    private OutputStream out;

    private Thread receiveThread;

    private short identifier;

    //    @Getter
//    @Setter
//    private boolean open;
//    @Getter
//    @Setter
//    private boolean connected;
    private AionMqttClientStatus status;


    @Getter
    @Setter
    private AionMqttClientMessageHandler messageHandler;

    private Map<Short, Object> identifierMap = new HashMap<>();

    @Getter
    private List<MqttClientTopic> subscribed = new ArrayList<>();

    public AionMqttClient(String host, int port, String clientId, String username, String password, int connectTimeout, int keepAlive, boolean clearSession, boolean autoReconnect, AionMqttClientMessageHandler messageHandler) {
        this.host = host;
        this.port = port;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
        this.connectTimeout = connectTimeout;
        this.keepAlive = keepAlive;
        this.clearSession = clearSession;
        this.autoReconnect = autoReconnect;
        if (messageHandler != null) {
            this.messageHandler = messageHandler;
            this.messageHandler.setClient(this);
        }
        this.status = AionMqttClientStatus.DISCONNECTED;
    }

    public boolean connectBlock() throws IOException {
//        socket = new Socket(host, port);
//        System.out.println("Connecting to " + host + ":" + port);
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            try {
                this.connect();
                while (this.status != AionMqttClientStatus.CONNECTED) {
                    Thread.sleep(1);
                }
                return true;
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            return false;
        });
        try {
            return future.get(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("客户端连接中断");
        } catch (ExecutionException e) {
           log.error("客户端连接执行异常");
        } catch (TimeoutException e) {
           log.error("客户端连接超时");
        }
        return false;
    }

    public void connect() throws IOException {
        this.socket = new Socket(host, port);
        this.status = AionMqttClientStatus.OPEN;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
        this.receiveThread = Thread.ofVirtual().start(() -> {
            AionByteBuffer cacheBuffer = new AionByteBuffer(1024);
            byte[] buffer = new byte[1024];
            while (true) {
                try {
                    int readSize = in.read(buffer);
                    if (readSize > 0) {
                        cacheBuffer.put(buffer, 0, readSize);
                        AionMqttMessage mqttMessage = null;
                        do {
                            log.debug("mqtt start decode mqtt frame clientId:{}", this.getClientId());
                            long startTime = System.currentTimeMillis();
                            try {
                                mqttMessage = MqttFrameDecoder.decode(cacheBuffer);
                                if (mqttMessage != null) {
                                    log.info("mqtt client clientId:{} receive message:{}", this.getClientId(), mqttMessage);
                                    if (messageHandler != null) {
                                        messageHandler.handle(mqttMessage);
                                    }
                                    switch (mqttMessage.getType()) {
                                        case CONNACK -> this.onConnected(mqttMessage);
                                        case SUBACK -> this.onSubAck((AionMqttSubackMessage) mqttMessage);
                                        case UNSUBACK -> this.onUnSubAck((AionMqttUnSubackMessage) mqttMessage);
                                        default ->
                                                throw new AionMqttUnknownTypeMessageException("Unknown message type");
                                    }
                                }
                            } catch (AionMqttUnknownTypeMessageException e) {
                                log.error("mqtt decode error on unknown type message,close channel");
                            }
                            log.trace("mqtt decode frame time consuming {}", System.currentTimeMillis() - startTime);
                        } while (mqttMessage != null);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        AionMqttConnectMessage mqttConnectMessage = AionMqttConnectMessage.create("test", "test", "test", false, 0, false, true, 60);
        this.sendMessage(mqttConnectMessage);
        this.status = AionMqttClientStatus.CONNECTING;
    }

    public void sendMessage(AionMqttMessage message) throws IOException {
        log.info("mqtt client clientId:{} send message:{}", this.getClientId(), message);
        this.out.write(message.getBytes());
    }

    /**
     * 已连接回调
     */
    public void onConnected(AionMqttMessage message) {
        this.status = AionMqttClientStatus.CONNECTED;
        log.debug("mqtt client connected");
    }

    public void onSubAck(AionMqttSubackMessage message) {
        List<MqttClientTopic> clientTopics = (List<MqttClientTopic>) this.identifierMap.remove(message.getIdentifier());
        if (clientTopics != null) {
            this.subscribed.addAll(clientTopics);
            if (messageHandler != null) {
                messageHandler.onSubAck(message);
            }
        }
    }

    public void onUnSubAck(AionMqttUnSubackMessage message) {
        System.out.println(message);
    }

    public void subscribe(String topicFilter, short requestedQoS) throws IOException {
        this.subscribe(List.of(topicFilter), requestedQoS);
    }

    public void subscribe(List<String> topicFilters, short requestedQoS) throws IOException {
        short newIdentifier = this.generateIdentifier();
        List<MqttClientTopic> mqttClientTopicList = new ArrayList<>();
        for (String topicFilter : topicFilters) {
            mqttClientTopicList.add(new MqttClientTopic(topicFilter, requestedQoS));
        }
        this.sendMessage(AionMqttSubscribeMessage.create(newIdentifier, topicFilters, requestedQoS));
        this.identifierMap.put(newIdentifier, mqttClientTopicList);
    }

    public void unsubscribe(List<String> topic) throws IOException {
        short newIdentifier = this.generateIdentifier();
        this.sendMessage(AionMqttUnSubscribeMessage.create(newIdentifier, topic));
        this.identifierMap.put(newIdentifier, topic);
    }

    /**
     * 生成 标识符
     *
     * @return
     */
    public short generateIdentifier() {
        return ++this.identifier;
    }
}
