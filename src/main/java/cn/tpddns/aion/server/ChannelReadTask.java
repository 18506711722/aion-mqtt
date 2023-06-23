package cn.tpddns.aion.server;

import cn.tpddns.aion.server.common.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class ChannelReadTask implements Runnable {
    private AionServer server;

    private Channel channel;

    private List<ChannelHandler> channelHandlers;


    public ChannelReadTask(AionServer server, Channel channel) {
        this.server = server;
        this.channel = channel;
        channelHandlers = server.getChannelHandlers();
    }

    @Override
    public void run() {
        log.info("channel read task running");
        try {
            for (ChannelHandler channelHandler : channelHandlers) {
                this.channel = channelHandler.handle(channel);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return;
        }

        if (channel != null) {
            server.getChannels().add(this.channel);
            log.info("server channel size={}", server.getChannels().size());
            System.out.println();
            try {
                InputStream in = this.channel.getInputStream();
                byte[] buffer = new byte[1024];
                int readLen;
                while (true) {
                    readLen = in.read(buffer);
                    if (readLen > 0) {
                        channel.onRead0(Arrays.copyOf(buffer, readLen));
                    } else {
                       Thread.sleep(1);  // 睡眠1S 避免CPU占用过高
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
                channel.onClose();
                server.getChannels().remove(channel);
            }
        }
    }
}
