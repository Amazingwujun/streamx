package com.jun.streamx.broker.javacv;


import com.jun.streamx.broker.constants.ProtocolEnum;
import com.jun.streamx.broker.exception.GrabberOrRecorderBuildException;
import com.jun.streamx.broker.server.ProtocolDispatchHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流数据采集和分发
 *
 * @author Jun
 * @since 1.0.0
 */
@Slf4j
public class FrameGrabAndRecordManager implements Runnable {

    static {
        avutil.av_log_set_level(avutil.AV_LOG_ERROR);
        FFmpegLogCallback.set();
    }

    public static final Map<String, FrameGrabAndRecordManager> CACHE = new ConcurrentHashMap<>();

    //@formatter:off

    private static final int MAX_EMPTY_PACKET_SIZE = 5;

    private FFmpegFrameGrabber grabber;
    private FFmpegFrameRecorder recorder;

    /** 含第一个 video 关键帧 */
    private byte[] flvHeaders;
    /** 流地址 */
    private final String streamUrl;
    private final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private CompletableFuture<byte[]> future;
    private final ChannelGroup channelGroup;
    private volatile boolean runningStatus;
    private final long keepAliveAfterGroupEmpty;

    //@formatter:on

    public FrameGrabAndRecordManager(String streamUrl) {
        this(streamUrl, Duration.ofMinutes(1).toMillis());
    }

    /**
     * 构建推流拉流对象
     *
     * @param streamUrl                流地址
     * @param keepAliveAfterGroupEmpty 当 channelGroup 为空时，保持拉流状态的时间.
     *                                 单位毫秒. 当该参数 < 0 时，拉流状态将一直保持。
     */
    public FrameGrabAndRecordManager(String streamUrl, long keepAliveAfterGroupEmpty) {
        Assert.hasText(streamUrl, "streamUrl 不能为空");

        this.streamUrl = streamUrl;
        this.keepAliveAfterGroupEmpty = keepAliveAfterGroupEmpty;
        this.channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    public CompletableFuture<byte[]> start() {
        if (!this.runningStatus) {
            this.runningStatus = true;
            this.future = new CompletableFuture<>();
            new Thread(this).start();
        }
        return future;
    }

    public void stop() {
        this.runningStatus = false;
    }

    private void stopAndRelease() {
        // 推流结束, 关闭全部客户端
        channelGroup.close();

        // 运行状态统统改为 false
        runningStatus = false;

        // 关闭相关资源
        try {
            grabber.close();
        } catch (FrameGrabber.Exception e) {
            log.error("资源关闭失败: " + e.getMessage(), e);
            log.error("grabber 关闭失败: " + e.getMessage(), e);
        }
        try {
            recorder.close();
        } catch (FrameRecorder.Exception e) {
            log.error("recorder 关闭失败: " + e.getMessage(), e);
        }
    }

    public void addReceiver(Channel channel) {
        channelGroup.add(channel);
    }

    public CompletableFuture<byte[]> future() {
        return future;
    }

    @Override
    public void run() {
        buildGrabber();
        buildRecorder();

        try {
            // 清空缓存
            grabber.flush();
        } catch (FrameGrabber.Exception e) {
            log.error("grabber flush failed: " + e.getMessage(), e);

            // 停止现成并释放资源
            stopAndRelease();

            return;
        }

        // flv header + video tag1
        if (flvHeaders == null) {
            flvHeaders = bos.toByteArray();
        }
        future.complete(flvHeaders);
        bos.reset();

        // 音视频的 stream_index
        var videoStream = grabber.getVideoStream();

        // 开始
        long channelGroupEmptyAt = Long.MAX_VALUE;
        long emptyPacketCount = 0;
        while (runningStatus) {
            try {
                var pkt = grabber.grabPacket();
                if (pkt == null || pkt.size() <= 0 || pkt.data() == null) {
                    emptyPacketCount++;
                    if (emptyPacketCount > MAX_EMPTY_PACKET_SIZE) {
                        log.error("流[{}]出现连续空包次数超过最大次数[{}]", streamUrl, MAX_EMPTY_PACKET_SIZE);
                        break;
                    }
                    continue;
                }
                emptyPacketCount = 0;

                // 跳过音频
                if (pkt.stream_index() != videoStream) {
                    continue;
                }

                // 转封装
                recorder.recordPacket(pkt);
                if (bos.size() > 0) {
                    var data = bos.toByteArray();
                    bos.reset();

                    if (channelGroup.isEmpty()) {
                        var pre = channelGroupEmptyAt;
                        if (channelGroupEmptyAt == Long.MAX_VALUE) {
                            channelGroupEmptyAt = System.currentTimeMillis();
                        }

                        if (keepAliveAfterGroupEmpty > 0 && System.currentTimeMillis() - pre > keepAliveAfterGroupEmpty) {
                            log.warn("[{}] 待推流 channels 为空，自动关闭当前推流拉流任务", streamUrl);
                            break;
                        }
                    } else {
                        channelGroupEmptyAt = Long.MAX_VALUE;
                    }

                    // 开始分发
                    channelGroup.forEach(channel -> {
                        switch (protocol(channel)) {
                            case ws, wss ->
                                    channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(data)));
                            case http, https -> channel.writeAndFlush(Unpooled.wrappedBuffer(data));
                        }
                    });
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                break;
            }
        }

        stopAndRelease();
    }


    private void buildGrabber() {
        this.grabber = new FFmpegFrameGrabber(streamUrl);

        grabber.setOption("timeout", String.valueOf(5 * 1_000_000));
        grabber.setOption("stimoout", "15000000");
        grabber.setOption("threads", "1");
        grabber.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);

        // 设置缓存大小，提高画质、减少卡顿花屏
        grabber.setOption("buffer_size", "1024000");

        // rtsp 流处理
        // 设置打开协议 tcp
        grabber.setOption("rtsp_transport", "tcp");

        try {
            grabber.start();
        } catch (FFmpegFrameGrabber.Exception e) {
            log.error("grabber start failed: " + e.getMessage(), e);
            future.completeExceptionally(e);
            this.runningStatus = false;
            throw new GrabberOrRecorderBuildException();
        }
    }

    private void buildRecorder() {
        this.recorder = new FFmpegFrameRecorder(bos,
                grabber.getImageWidth(),
                grabber.getImageHeight(),
                grabber.getAudioChannels());
        recorder.setFormat("flv");
        /*
            没有这个配置，会导致 flv.js 播放延迟 10 秒左右。
            具体原因我没找到，github 上有人提到过这个 issue: https://github.com/bytedeco/javacv/issues/718
         */
        recorder.setInterleaved(false);

        try {
            recorder.start(grabber.getFormatContext());
        } catch (FFmpegFrameRecorder.Exception e) {
            log.error("recorder start failed: " + e.getMessage(), e);
            future.completeExceptionally(e);
            this.runningStatus = false;
            throw new GrabberOrRecorderBuildException();
        }
    }

    /**
     * 获取当前 channel 的 protocol
     *
     * @param channel {@link Channel}
     * @return ProtocolEnum
     */
    private ProtocolEnum protocol(Channel channel) {
        return (ProtocolEnum) channel.attr(AttributeKey.valueOf(ProtocolDispatchHandler.PROTOCOL)).get();
    }
}
