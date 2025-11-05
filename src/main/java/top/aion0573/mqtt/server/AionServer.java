package top.aion0573.mqtt.server;

import top.aion0573.mqtt.channel.ChannelHandler;
import top.aion0573.mqtt.channel.SocketChannel;
import top.aion0573.mqtt.common.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;


@Slf4j
public class AionServer {
    @Getter
    @Setter
    private Integer port;

    private ExecutorService executorService;

    private Thread channelTimeoutThread;

    private Thread acceptThread;

    /**
     * 通道处理器  主要仿造netty的设计理念
     */
    @Getter
    final private List<ChannelHandler> channelHandlers;

    public AionServer port(int port) {
        this.port = port;
        return this;
    }

    /**
     * 服务通道数 包括所有的TCP连接 比如说未连接的MQTT连接
     */
    @Getter
    final private List<Channel> channels = new ArrayList<>();  // 后面改成线程线程安全或者加个锁
    @Getter
    final private ReentrantReadWriteLock channelsReentrantReadWriteLock;

    public AionServer() {
        this.channelsReentrantReadWriteLock = new ReentrantReadWriteLock();
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
        if (this.getPort() == null) {
            throw new RuntimeException("aion server port is null");
        }
        this.executorService = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("aion-server-channel-read-thread").factory());  // 不知道虚拟线程怎么设置线程名称
        ServerSocket serverSocket = new ServerSocket(this.getPort());
        this.channelTimeoutThread = Thread.ofVirtual().name("aion-server-channel-timeout-thread").start(() -> {
            log.info("aion-server-channel-timeout-thread start");
            while (true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                log.debug("aion server start check timeout channels");
                long startTime = System.currentTimeMillis();
                List<Channel> needCloseChannels = new ArrayList<>();
                ReentrantReadWriteLock.ReadLock channelsReadLock = this.channelsReentrantReadWriteLock.readLock();
                channelsReadLock.lock();
                for (Channel channel : AionServer.this.getChannels()) {
                    if (channel.getClass().equals(SocketChannel.class)) {
                        long idleTime = channel.getIdleTime();
                        if (idleTime > TimeUnit.SECONDS.toMillis(30)) {
                            log.info("socket channel idle time:{} > 30s", idleTime);
                            needCloseChannels.add(channel);
                        }
                    }
                }
                channelsReadLock.unlock();
                for(Channel channel:needCloseChannels){
                    channel.close();
                }
                log.debug("aion server end check timeout channels ,consume time {} ms ", System.currentTimeMillis() - startTime);
            }
        });
        this.acceptThread = Thread.ofPlatform().name("aion-server-accept-thread").daemon().start(() -> {
            log.info("aion mqtt server start listening port:{}", AionServer.this.getPort());
            while (true) {
                try {
                    final Socket socket = serverSocket.accept();
                    log.debug("new socket connection");
                    SocketChannel socketChannel = new SocketChannel(AionServer.this, socket);
                    socketChannel.setOpenTime(new Date());
                    AionServer.this.executorService.submit(new AionChannelReadTask(AionServer.this, socketChannel));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return this;
    }

    public void addChannel(Channel channel) {
        ReentrantReadWriteLock.WriteLock channelWriteLock = channelsReentrantReadWriteLock.writeLock();
        channelWriteLock.lock();
        this.channels.add(channel);
        channelWriteLock.unlock();
    }

    public void removeChannel(Channel channel) {
        ReentrantReadWriteLock.WriteLock channelWriteLock = channelsReentrantReadWriteLock.writeLock();
        channelWriteLock.lock();
        this.channels.remove(channel);
        channelWriteLock.unlock();
    }

    public void await() throws InterruptedException {
        this.acceptThread.join();
    }
}
