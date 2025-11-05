package top.aion0573.mqtt.common.mqtt;

public class MqttFrameType {
    public final static int CONNECT = 1;
    public final static int CONNACK = 2;
    public final static int PUBLISH = 3;
    public final static int PUBACK = 4;
    public final static int PUBREC = 5;
    public final static int PUBREL = 6;
    public final static int PUBCOMP = 7;
    public final static int SUBSCRIBE = 8;
    public final static int SUBACK = 9;
    public final static int UNSUBSCRIBE = 10;
    public final static int UNSUBACK = 11;
    public final static int PINGREQ = 12;
    public final static int PINGRESP = 13;
    public final static int DISCONNECT = 14;
}
