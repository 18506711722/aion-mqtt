package top.aion0573.example;

import top.aion0573.mqtt.http.HttpChannelHandler;
import top.aion0573.mqtt.server.AionServer;
import top.aion0573.mqtt.channel.MqttChannelHandler;
import top.aion0573.mqtt.server.AionMqttServerContext;
import top.aion0573.mqtt.websocket.WebSocketChannelHandler;

import java.io.IOException;
import java.util.List;

public class MqttServerTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        MqttChannelHandler mqttChannelHandler = new MqttChannelHandler();
        AionServer server = new AionServer()
                .addChannelHandler(new HttpChannelHandler())
                .addChannelHandler(new WebSocketChannelHandler(List.of("mqtt")))
                .addChannelHandler(mqttChannelHandler)
                .port(8083)
                .start();
        AionServer tcpMqttServer = new AionServer()
                .addChannelHandler(mqttChannelHandler)
                .port(1883)
                .start();
//        tcpMqttServer.await();


        while (true) {
            System.out.println("当前服务器连接数:" + tcpMqttServer.getChannels().size());
            System.out.println("当前MQTT连接数:" + AionMqttServerContext.getInstance().getMqttChannels().size());
            System.out.println("当前MQTT连接客户端：" + AionMqttServerContext.getInstance().getMqttChannels());
            System.out.println("当前订阅主题:" + AionMqttServerContext.getInstance().getTopics());
            Thread.sleep(1000);
        }
    }
}
