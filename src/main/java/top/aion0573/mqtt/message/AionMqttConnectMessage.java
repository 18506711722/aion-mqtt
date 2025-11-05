package top.aion0573.mqtt.message;

import com.oracle.svm.core.annotate.Alias;
import top.aion0573.mqtt.common.buffer.AionByteBuffer;

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

        if (this.version == 5) {  //MQTT 5.0 增加的属性解析
            int propertyLen = buffer.get();
            byte[] propertyB = new byte[propertyLen];
            buffer.get(propertyB);
            ByteBuffer propertyByteBuffer = ByteBuffer.wrap(propertyB);
            do {
                byte identifier = propertyByteBuffer.get();
                switch (identifier) {
                    case 0x11:
                        int sessionExpiryInterval = propertyByteBuffer.getInt();
                        System.out.println("sessionExpiryInterval: " + sessionExpiryInterval);
                        break;
                    case 0x21:
                        short receiveMaximum = propertyByteBuffer.getShort();
                        System.out.println("receiveMaximum: " + receiveMaximum);
                        break;
                    case 0x27:
                        short maximumPacketSize = propertyByteBuffer.getShort();
                        System.out.println("maximumPacketSize: " + maximumPacketSize);
                        break;
                    case 0x22:
                        short topicAliasMaximum = propertyByteBuffer.getShort();
                        System.out.println("topicAliasMaximum: " + topicAliasMaximum);
                        break;
                    case 0x19:
                        boolean requestResponseInformation = Byte.toUnsignedInt(propertyByteBuffer.get()) == 1;
                        System.out.println("requestResponseInformation: " + requestResponseInformation);
                        break;
                    case 0x17:
                        boolean requestProblemInformation = Byte.toUnsignedInt(propertyByteBuffer.get()) == 1;
                        System.out.println("requestProblemInformation: " + requestProblemInformation);
                        break;
                    case 0x26:
                        System.out.println("用户属性");
                        short keyLen = propertyByteBuffer.getShort();
                        byte[] keyB = new byte[keyLen];
                        propertyByteBuffer.get(keyB);
                        short valueLen = propertyByteBuffer.getShort();
                        byte[] valueB = new byte[valueLen];
                        propertyByteBuffer.get(valueB);
                        break;
                    case 0x15:  // 验证方法
                        short s = propertyByteBuffer.getShort();
                        byte[] sB = new byte[s];
                        propertyByteBuffer.get(sB);
                        System.out.println(new String(sB, StandardCharsets.UTF_8));
                        break;
                    case 0x16:
                        short ss = propertyByteBuffer.getShort();
                        byte[] ssB = new byte[ss];
                        propertyByteBuffer.get(ssB);
                        System.out.println(new String(ssB, StandardCharsets.UTF_8));
                        break;
                }
            } while (propertyByteBuffer.hasRemaining());
        }
        int clientIdLen = buffer.getShort();
        byte[] clientIdB = new byte[clientIdLen];
        buffer.get(clientIdB);
        this.setClientId(new String(clientIdB, StandardCharsets.UTF_8));

        if (this.isWill()) {
            int willTopicLen = buffer.getShort();
            byte[] willTopicB = new byte[willTopicLen];
            buffer.get(willTopicB);
            this.willTopic = new String(willTopicB, StandardCharsets.UTF_8);

            int willPayloadLen = buffer.getShort();
            byte[] willPayloadB = new byte[willPayloadLen];
            buffer.get(willPayloadB);
            this.willPayload = willPayloadB;
        }

        if (this.isHaveUsername()) {
            int usernameLen = buffer.getShort();
            byte[] usernameB = new byte[usernameLen];
            buffer.get(usernameB);
            this.setUsername(new String(usernameB, StandardCharsets.UTF_8));
        }
        if (this.isHavePassword()) {
            int passwordLen = buffer.getShort();
            byte[] passwordB = new byte[passwordLen];
            buffer.get(passwordB);
            this.setPassword(new String(passwordB, StandardCharsets.UTF_8));
        }
    }

    public static AionMqttConnectMessage create(String clientId, String username, String password, boolean willRetain, int willQoS, boolean willFlag, boolean cleanSession, int keepAlive) {
        byte controlPacketType = 0x10;
        /**
         * 帧长度
         */
        byte remainingLength = 0;

        /**
         * 协议名称
         */
        String protocolName = "MQTT";
        byte[] protocolNameB = protocolName.getBytes(StandardCharsets.UTF_8);
        /**
         * 协议名称长度
         */
        int protocolNameLength = (byte) protocolNameB.length;
        // 增加协议长度的两个字节
        remainingLength += 2;
        // 增加协议字符串的字节
        remainingLength += protocolNameLength;
        /**
         * 协议界别 3.1  固定0x04
         */
        byte protocolLevel = 0x04;
        remainingLength += 1;

        byte connectFlag = 0x00;
        if (username != null) {
            connectFlag &= (byte) 0x80;
        }
        if (password != null) {
            connectFlag &= (byte) 0x40;
        }
        if (willRetain) {
            connectFlag &= (byte) 0x20;
        }
        switch (willQoS) {
            case 0:
                // 默认两位0不做修改
                break;
            case 1:
                connectFlag &= (byte) 0x08;
                break;
            case 2:
                connectFlag &= (byte) 0x10;
                connectFlag &= (byte) 0x08;
                break;
        }
        if (willFlag) {
            connectFlag &= (byte) 0x04;
        }
        if (cleanSession) {
            connectFlag &= (byte) 0x02;
        }
        //增加连接标志的一个字节
        remainingLength += 1;
        //增加表示keepAlive的两个字节
        remainingLength += 2;
        if (clientId != null) {
            remainingLength += 2;
            remainingLength += (byte) clientId.getBytes(StandardCharsets.UTF_8).length;
        }
        if (username != null) {
            remainingLength += 2;
            remainingLength += (byte) username.getBytes(StandardCharsets.UTF_8).length;
        }
        if (password != null) {
            remainingLength += 2;
            remainingLength += (byte) password.getBytes(StandardCharsets.UTF_8).length;
        }

        AionByteBuffer bytes = new AionByteBuffer(remainingLength + 2);
        bytes.put(controlPacketType);
        bytes.put(remainingLength);
        bytes.put(new byte[]{
                (byte) (protocolNameLength >> 8),
                (byte) protocolNameLength
        });
        bytes.put(protocolNameB);
        bytes.put(protocolLevel);
        bytes.put(connectFlag);
        bytes.put(new byte[]{
                (byte) (keepAlive >> 8),
                (byte) keepAlive
        });
        if (clientId != null) {
            byte[] clientIdB = clientId.getBytes(StandardCharsets.UTF_8);
            int clientIdByteLen = clientIdB.length;
            bytes.put(new byte[]{
                    (byte) (clientIdByteLen >> 8),
                    (byte) clientIdByteLen
            });
            bytes.put(clientId.getBytes(StandardCharsets.UTF_8));
        }
        if (username != null) {
            byte[] usernameB = username.getBytes(StandardCharsets.UTF_8);
            int usernameByteLen = usernameB.length;
            bytes.put(new byte[]{
                    (byte) (usernameByteLen >> 8),
                    (byte) usernameByteLen
            });
            bytes.put(username.getBytes(StandardCharsets.UTF_8));
        }
        if (password != null) {
            byte[] passwordB = password.getBytes(StandardCharsets.UTF_8);
            int passwordByteLen = passwordB.length;
            bytes.put(new byte[]{
                    (byte) (passwordByteLen >> 8),
                    (byte) passwordByteLen
            });
            bytes.put(password.getBytes(StandardCharsets.UTF_8));
        }
        return new AionMqttConnectMessage(bytes.getEffectiveBytes());
    }

    public static AionMqttConnectMessage parse(byte[] bytes) {
        return new AionMqttConnectMessage(bytes);
    }
}
