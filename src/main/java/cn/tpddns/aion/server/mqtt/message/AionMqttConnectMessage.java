package cn.tpddns.aion.server.mqtt.message;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class AionMqttConnectMessage extends AionMqttMessage {

    private byte version;
    private boolean haveUsername;

    private boolean havePassword;

    private boolean willRetain;

    private int willQos;

    private boolean will;

    private boolean cleanSession;

    private int keepAlive;

    private String clientId;

    private String username;

    private String password;

    private String willTopic;

    private byte[] willPayload;

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public boolean isHaveUsername() {
        return haveUsername;
    }

    public void setHaveUsername(boolean haveUsername) {
        this.haveUsername = haveUsername;
    }

    public boolean isHavePassword() {
        return havePassword;
    }

    public void setHavePassword(boolean havePassword) {
        this.havePassword = havePassword;
    }

    public boolean isWillRetain() {
        return willRetain;
    }

    public boolean isWill() {
        return will;
    }

    public void setWill(boolean will) {
        this.will = will;
    }

    public void setWillRetain(boolean willRetain) {
        this.willRetain = willRetain;
    }



    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }



    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getWillQos() {
        return willQos;
    }

    public void setWillQos(int willQos) {
        this.willQos = willQos;
    }

    public String getWillTopic() {
        return willTopic;
    }

    public void setWillTopic(String willTopic) {
        this.willTopic = willTopic;
    }

    public byte[] getWillPayload() {
        return willPayload;
    }

    public void setWillPayload(byte[] willPayload) {
        this.willPayload = willPayload;
    }



    public AionMqttConnectMessage(byte[] bytes) {
        super(bytes);
        this.setType(MqttMessageType.CONNECT);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.position(2);
        int protocolLen = buffer.getShort();
        byte[] protocolB = new byte[protocolLen];
        buffer.get(protocolB);
        this.setProtocol(new String(protocolB));
        byte versionB = buffer.get();
        this.setVersion(versionB);
        byte connectFlagB = buffer.get();
        int connectFlagInt = Byte.toUnsignedInt(connectFlagB);
        this.setHaveUsername((connectFlagInt >> 7 & 0x01) > 0);
        this.setHavePassword((connectFlagInt >> 6 & 0x01) > 0);
        this.setWillRetain((connectFlagInt >> 5 & 0x01) > 0);

        this.willQos = Byte.toUnsignedInt((byte) (connectFlagInt >> 3 & 0x03));
        this.will = Byte.toUnsignedInt((byte) (connectFlagInt >> 2 & 0x01)) > 0;
        this.setCleanSession((connectFlagInt >> 1 & 0x01) > 0);
        this.setKeepAlive(buffer.getShort());

        int clientIdLen = buffer.getShort();
        byte[] clientIdB = new byte[clientIdLen];
        buffer.get(clientIdB);
        this.setClientId(new String(clientIdB, StandardCharsets.UTF_8));

        if(this.isWill()){
            int willTopicLen = buffer.getShort();
            byte[] willTopicB = new byte[willTopicLen];
            buffer.get(willTopicB);
            this.willTopic = new String(willTopicB,StandardCharsets.UTF_8);

            int willPayloadLen = buffer.getShort();
            byte[] willPayloadB = new byte[willPayloadLen];
            buffer.get(willPayloadB);
            this.willPayload = willPayloadB;
        }

        if (this.isHaveUsername()) {
            int usernameLen = buffer.getShort();
            byte[] usernameB = new byte[usernameLen];
            buffer.get(usernameB);
            this.setUsername(new String(usernameB,StandardCharsets.UTF_8));
        }
        if(this.isHavePassword()){
            int passwordLen = buffer.getShort();
            byte[] passwordB = new byte[passwordLen];
            buffer.get(passwordB);
            this.setPassword(new String(passwordB,StandardCharsets.UTF_8));
        }


    }

    public static AionMqttConnectMessage parse(byte[] bytes) {
        return new AionMqttConnectMessage(bytes);
    }
}
