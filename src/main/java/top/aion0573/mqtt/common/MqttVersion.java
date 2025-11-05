package top.aion0573.mqtt.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MqttVersion {
    MQTT500("5.0", 5), MQTT311("3.1.1", 4), MQTT310("3.1", 3);

    private final String stringVersion;
    private final int intVersion;


    public static MqttVersion getMqttVersion(int intVersion) {
        return switch (intVersion) {
            case 5 -> MQTT500;
            case 4 -> MQTT311;
            case 3 -> MQTT310;
            default -> throw new IllegalArgumentException("Unknown MQTTVersion: " + intVersion);
        };
    }
}
