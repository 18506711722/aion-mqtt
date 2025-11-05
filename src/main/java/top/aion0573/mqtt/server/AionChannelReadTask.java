package top.aion0573.mqtt.server;

import top.aion0573.mqtt.channel.ChannelHandler;
import top.aion0573.mqtt.common.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class AionChannelReadTask implements Runnable {
    final private AionServer server;

    private Channel channel;

    final private List<ChannelHandler> channelHandlers;


    public AionChannelReadTask(AionServer server, Channel channel) {
        this.server = server;
        this.channel = channel;
        this.channelHandlers = server.getChannelHandlers();
    }

    @Override
    public void run() {
        log.debug("channel read task running");
        try {
            for (ChannelHandler channelHandler : channelHandlers) {
                this.channel = channelHandler.handle(channel);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return;
        }

        if (channel != null) {
            server.addChannel(this.channel);
            log.debug("server channel size={}", server.getChannels().size());
            try {
                InputStream in = this.channel.getInputStream();
                byte[] buffer = new byte[1024];
                int readLen;
                while (true) {
                    readLen = in.read(buffer);
                    if (readLen > 0) {
                        channel.onRead0(Arrays.copyOf(buffer, readLen));
                    } else {
                       Thread.sleep(1);  // 睡眠1ms 避免CPU占用过高
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                channel.close();
            }
        }
    }
}
