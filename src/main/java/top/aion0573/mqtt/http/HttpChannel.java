package top.aion0573.mqtt.http;

import top.aion0573.mqtt.channel.SocketChannel;
import top.aion0573.mqtt.common.channel.Channel;
import top.aion0573.mqtt.common.buffer.AionByteBuffer;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpChannel extends SocketChannel {
    //    private final static int MAX_HEADER_SIZE = 8 * 1024;
    @Setter
    @Getter
    private Channel channel;

    @Setter
    @Getter
    private HttpRequest request;

    public HttpChannel(Socket socket){
        super(socket);
        this.setChannel(new SocketChannel(socket));
    }

    public HttpChannel(SocketChannel channel) {
        super(channel.getSocket());
        this.setServer(channel.getServer());
        this.setChannel(channel);
        this.setReadCache(channel.getReadCache());
        if (getReadCache().limit() > 0) {
            this.onRead(getReadCache());
        }
    }

    public HttpRequest parseRequest() throws IOException {
        HttpRequest request = new HttpRequest();
        AionByteBuffer readCache = getReadCache();
        byte[] methodB = new byte[readCache.limit()];
//        readCache.flip();
        readCache.get(methodB);
        readCache.compact();
        String httpMethodName = new String(methodB, StandardCharsets.UTF_8);

        BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStream()));

        String firstLine = httpMethodName + reader.readLine();
        String[] firstLineArr = firstLine.split(" ");
        httpMethodName = firstLineArr[0];

        request.setHttpMethodType(HttpMethodType.valueOf(httpMethodName));

        String url = firstLineArr[1];
        request.setUrl(url);

        String httpVersion = firstLineArr[2];

        request.setHttpVersion(httpVersion);

        String headerLine;
        do {
            headerLine = reader.readLine();
            if (headerLine != null && !headerLine.isBlank()) {
                String[] headerArr = headerLine.split(":");
                request.getRequestHeader().put(headerArr[0].trim(), headerArr[1].trim());
            }
        } while (headerLine != null && !headerLine.isBlank());
        String contentLengthStr = request.getRequestHeader().get(HttpHeader.CONTENT_LENGTH);

        if (contentLengthStr != null) {
            int contentLength = Integer.parseInt(contentLengthStr);
            byte[] body = new byte[contentLength];
            getInputStream().read(body);
            request.setBody(body);
        }
        return request;
    }

    @Override
    public void onRead(AionByteBuffer buffer) {
        try {
            HttpRequest httpRequest = this.parseRequest();
            this.setRequest(httpRequest);
            this.onRequest(httpRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onRequest(HttpRequest request) {
        Map<String, String> requestHeader = request.getRequestHeader();
        String connection = requestHeader.get(HttpHeader.CONNECTION);
        if (connection == null) {
            // TODO  返回响应

            String a = """
                    HTTP/1.1 404 Not Found
                    Server: nginx/1.22.1
                    Date: Wed, 17 May 2023 01:54:28 GMT
                    Content-Type: text/html
                    Content-Length: 153
                    Connection: keep-alive

                    <html>
                    <head><title>404 Not Found</title></head>
                    <body>
                    <center><h1>404 Not Found</h1></center>
                    <hr><center>nginx/1.22.1</center>
                    </body>
                    </html>
                    """;
            try {
                this.write(a.getBytes(StandardCharsets.UTF_8));
                this.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
//        SKIP 请求给后续 handle 升级 通道
    }

//    @Override
//    public InputStream getInputStream() throws IOException {
//        return this.channel.getInputStream();
//    }
//
//    @Override
//    public OutputStream getOutputStream() throws IOException {
//        return this.channel.getOutputStream();
//    }
//
//    @Override
//    public void close() {
//        this.getChannel().close();
//    }


}
