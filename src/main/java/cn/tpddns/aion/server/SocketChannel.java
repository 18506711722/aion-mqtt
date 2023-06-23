package cn.tpddns.aion.server;

import cn.tpddns.aion.server.common.channel.AbstractChannel;
import cn.tpddns.aion.server.mqtt.message.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@Slf4j
public class SocketChannel extends AbstractChannel {

    private Socket socket;

    public Socket getSocket() {
        return socket;
    }

    public SocketChannel(Socket socket) throws IOException {
        this.socket = socket;
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
            getSocket().close();
            this.setClose(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClose() {
        log.debug("socket channel on close");
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
