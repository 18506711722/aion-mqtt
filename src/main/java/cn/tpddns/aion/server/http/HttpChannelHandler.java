package cn.tpddns.aion.server.http;

import cn.tpddns.aion.server.common.channel.Channel;
import cn.tpddns.aion.server.ChannelHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HttpChannelHandler implements ChannelHandler {
    private final static List<String> HTTP_METHOD_TYPES = new ArrayList<>() {
        {
            add(HttpMethodType.GET.name());
            add(HttpMethodType.POST.name());
            add(HttpMethodType.PUT.name());
            add(HttpMethodType.DELETE.name());
            add(HttpMethodType.HEAD.name());
            add(HttpMethodType.CONNECT.name());
            add(HttpMethodType.OPTIONS.name());
        }
    };

    private final String RESPONSE_400 = """
            HTTP/1.1 400 Bad Request
            Server: nginx
            Date: Tue, 16 May 2023 07:23:57 GMT
            Content-Type: text/html
            Content-Length: 150
            Connection: close
                        
            <html>
            <head><title>400 Bad Request</title></head>
            <body>
            <center><h1>400 Bad Request</h1></center>
            <hr><center>nginx</center>
            </body>
            </html>
            """;

    /**
     * 判断 是不是 以 HTTP MRTHOD 开头
     *
     * @param channel
     * @return
     * @throws IOException
     */
    private boolean isHttpMethodStart(Channel channel) throws IOException {
        List<String> matchMethod = new ArrayList<>(HTTP_METHOD_TYPES);
        InputStream in = channel.getInputStream();

        boolean isMatch = false;
        int index = 0;
        do {
            int letter = in.read();
            channel.onRead0(new byte[]{(byte) letter});
            List<String> deleteList = new ArrayList<>();
            for (String methodName : matchMethod) {
                if (methodName.charAt(index) == letter) {
                    if (methodName.length() - 1 == index) {
                        isMatch = true;
                        break;
                    }
                } else {
                    deleteList.add(methodName);
                }
            }
            if (!deleteList.isEmpty()) {
                matchMethod.removeAll(deleteList);
            }
            index++;
        } while (!isMatch && !matchMethod.isEmpty());
        return isMatch;
    }

    @Override
    public Channel handle(Channel channel) throws Exception {
        boolean isHttp = isHttpMethodStart(channel);
        if (isHttp) {
            return new HttpChannel(channel);
        } else {
            channel.write(RESPONSE_400.getBytes(StandardCharsets.UTF_8));
            channel.close();
            throw new RuntimeException("非HTTP请求");
        }
    }

}
