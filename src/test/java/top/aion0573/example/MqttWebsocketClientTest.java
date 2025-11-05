package top.aion0573.example;

import top.aion0573.mqtt.client.AionMqttClient;
import top.aion0573.mqtt.client.AionMqttClientMessageAdapterHandler;
import top.aion0573.mqtt.message.AionMqttMessage;
import top.aion0573.mqtt.message.AionMqttSubackMessage;

import java.io.IOException;
import java.util.List;

public class MqttWebsocketClientTest {

    public static void main(String[] args) throws InterruptedException, IOException {
        AionMqttClient client = new AionMqttClient("localhost", 1883, "test", "test", "test", 30, 10, true, true, new AionMqttClientMessageAdapterHandler() {
//            @Override
//            public void handle(AionMqttMessage message) {
//                System.out.println(message);
//            }

            @Override
            public void onSubAck(AionMqttSubackMessage message) {
                System.out.println(this.getClient().getSubscribed());
                try {
                    this.getClient().unsubscribe(List.of("/test/#"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        client.connectBlock();
//        client.subscribe("/test/#", (short) 0);
        client.subscribe(List.of("/test/#","/test1/#"), (short) 0);
//        OutputStream outputStream =  client.getSocket().getOutputStream();
//        outputStream.write(new byte[]{
//                -94, 8, 0, 2, 0, 4, 116, 101, 115, 116
//        });
//        byte[] bb = new byte[1024];
//       client.getSocket().getInputStream().read(bb);
//        outputStream.write();
        Thread.sleep(300000);
//        client.getSocket().getOutputStream().write(1);
//        Thread.sleep(3000);
        System.out.println(1111111);
    }
}
