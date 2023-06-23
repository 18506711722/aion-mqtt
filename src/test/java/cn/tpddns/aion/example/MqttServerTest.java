package cn.tpddns.aion.example;

import cn.tpddns.aion.server.AionServer;
import cn.tpddns.aion.server.http.HttpChannelHandler;
import cn.tpddns.aion.server.mqtt.MqttChannelHandler;
import cn.tpddns.aion.server.websocket.WebSocketChannelHandler;

import java.io.IOException;
import java.util.List;

public class MqttServerTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        AionServer server = new AionServer()
                .addChannelHandler(new HttpChannelHandler())
                .addChannelHandler(new WebSocketChannelHandler(List.of("mqtt")))
                .addChannelHandler(new MqttChannelHandler())
                .port(8083)
                .start();
        AionServer tcpMqttServer = new AionServer()
                .addChannelHandler(new MqttChannelHandler())
                .port(1883)
                .start();
        server.await();
    }
}
