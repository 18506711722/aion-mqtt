package top.aion0573.mqtt.channel;

import lombok.Setter;
import top.aion0573.mqtt.common.channel.AbstractChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import top.aion0573.mqtt.server.AionServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

@Slf4j
public class SocketChannel extends AbstractChannel {
    @Getter
    final private Socket socket;

    private AionServer server;

    @Override
    public AionServer getServer() {
        return server;
    }

    @Override
    public void setServer(AionServer server) {
        this.server = server;
    }

    public SocketChannel(Socket socket){
        this.socket = socket;
    }

    public SocketChannel( AionServer server,Socket socket) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    @Override
    public void close() {
        try {
            this.getSocket().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            if(!this.isClose()){
                this.setClose(true);
                this.onClose();
            }
        }
    }

    @Override
    public void onClose() {
        log.debug("socket channel on close");
        getServer().removeChannel(this);
    }

//    public void sendMessage(AionMqttMessage message) throws IOException {
//        this.write(message.asByte());
//    }
//
//    @Override
//    public void onRead(ByteBuffer buffer) {
//        buffer.flip();
//        byte flagB = buffer.get();
//        byte packetLengthB = buffer.get();
//        byte[] packetPayload = new byte[packetLengthB + 2];
//        packetPayload[0] = flagB;
//        packetPayload[1] = packetLengthB;
//        buffer.get(packetPayload, 2, packetLengthB);
//        buffer.compact();
//        AionMqttMessage message = AionMqttMessage.parse(packetPayload);
//        this.onMessage(message);
//    }

//    public void onMessage(AionMqttMessage message) {
//        try {
//            switch (message.getType()) {
//                case CONNECT -> {
//                    this.onConnect((AionMqttConnectMessage) message);
//                }
//                case SUBSCRIBE -> {
//                    this.onSubscribe((AionMqttSubscribeMessage) message);
//                }
//                case PUBLISH -> {
//                    this.onPublish((AionMqttPublishMessage) message);
//                }
//                case DISCONNECT -> this.onDisConnect((AionMqttDisconnectMessage) message);
//            }
//        } catch (Exception e) {
//            this.onError(e);
//        }
//    }
//
//    public void onError(Throwable e) {
//        e.printStackTrace();
//    }

//    public void onConnect(AionMqttConnectMessage message) throws IOException {
//        System.out.println("客户端请求接入");
//        AionMqttMessage mqttConnackMessage = AionMqttConnackMessage.create();
//        this.sendMessage(mqttConnackMessage);
//    }

//    public void onSubscribe(AionMqttSubscribeMessage message) throws IOException {
//        System.out.println("客户端发起订阅");
//        String topicFilter = message.getTopicFilter();
//        subscribe.add(topicFilter);
//        AionMqttMessage mqttSubackMessage = AionMqttSubackMessage.create(message.getIdentifier());
//        this.sendMessage(mqttSubackMessage);
//    }

//    public void onPublish(AionMqttPublishMessage message) throws IOException {
//        System.out.println("客户端发送消息");
//        AionMqttMessage mqttSubackMessage = AionMqttPubackMessage.create(message.getIdentifier());
//        this.sendMessage(mqttSubackMessage);
//    }

//    public void onDisConnect(AionMqttDisconnectMessage message) {
//        System.out.println("客户端主动断开");
//    }
}
