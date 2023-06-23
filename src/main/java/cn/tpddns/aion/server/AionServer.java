package cn.tpddns.aion.server;

import cn.tpddns.aion.server.common.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
public class AionServer {

    private int port;

    private ExecutorService executorService;

    private Thread acceptThread;

    private List<ChannelHandler> channelHandlers;


    public List<ChannelHandler> getChannelHandlers() {
        return channelHandlers;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public AionServer port(int port) {
        this.port = port;
        return this;
    }

    private final Map<String, List<Channel>> topic = new HashMap<>();

    public Map<String, List<Channel>> getTopic() {
        return topic;
    }

    private final List<Channel> channels = new ArrayList<>();  // 后面改成线程线程安全或者加个锁

    public List<Channel> getChannels() {
        return channels;
    }

    public AionServer() {
        this.setPort(1883);
        this.channelHandlers = new ArrayList<>();
    }

    public void stop() {
        log.info("start stop aion server");
        this.executorService.shutdown();
        this.acceptThread.interrupt();
        log.info("stop aion server success");
    }

    public AionServer addChannelHandler(ChannelHandler handler) {
        this.channelHandlers.add(handler);
        return this;
    }

    public AionServer start() throws IOException {
        this.executorService = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("aion-server-channel-read-thread").factory());  // 不知道虚拟线程怎么设置线程名称
        ServerSocket serverSocket = new ServerSocket(this.getPort());
        this.acceptThread = Thread.ofPlatform().name("aion-server-accept-thread").daemon().start(() -> {
            log.info("aion mqtt server start listening port:{}", AionServer.this.getPort());
            while (true) {
                try {
                    final Socket socket = serverSocket.accept();
                    log.info("new socket connection");
                    Channel channel = new SocketChannel(socket);
                    AionServer.this.executorService.submit(new ChannelReadTask(AionServer.this,channel));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return this;
    }

    public void await() throws InterruptedException {
        this.acceptThread.join();
    }
}
