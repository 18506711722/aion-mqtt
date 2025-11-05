package top.aion0573.mqtt.http;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class HttpRequest {
    private HttpMethodType httpMethodType;

    private String url;

    private String httpVersion;

    private Map<String, String> requestHeader = new HashMap<>();

    private byte[] body;
}
