package top.aion0573.mqtt.message;

public enum MqttMessageType {
    DATA(-1),
    CONNECT(1),  // 客户端连接报文
    CONNACK(2),  // 客户端连接响应报文
    PUBLISH(3),  // 客户端发布报文
    PUBACK(4),  // 正常的PUBLISH报文响应
    PUBREC(5),   // qos=2 报文的响应
    PUBREL(6),
    PUBCOMP(7),  //PUBREL 报文的响应
    SUBSCRIBE(8),// 客户端订阅的报文
    SUBACK(9),   // 订阅响应报文
    UNSUBSCRIBE(10), //取消订阅
    UNSUBACK(11), // 取消订阅响应报文
    PINGREQ(12), // 客户端PING 报文
    PINGRESP(13), // 服务端 PONG 报文
    DISCONNECT(14); // 客户端断开连接报文


    private int type;

    MqttMessageType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

   public static MqttMessageType ofType(int type) {
        switch (type) {
            case 1:
                return CONNECT;
            case 2:
                return CONNACK;
            case 3:
                return PUBLISH;
            case 4:
                return PUBACK;
            case 5:
                return PUBREC;
            case 6:
                return PUBREL;
            case 7:
                return PUBCOMP;
            case 8:
                return SUBSCRIBE;
            case 9:
                return SUBACK;
            case 10:
                return UNSUBSCRIBE;
            case 11:
                return UNSUBACK;
            case 12:
                return PINGREQ;
            case 13:
                return PINGRESP;
            case 14:
                return DISCONNECT;
            default:
                throw new RuntimeException("MQTT package type Reserved");
        }
    }
}

