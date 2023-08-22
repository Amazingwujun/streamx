package com.jun.streamx.broker;


import com.jun.streamx.commons.exception.StreamxException;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CompletableFuture;

/**
 * 流数据采集和分发
 *
 * @author Jun
 * @since 1.0.0
 */
@Slf4j
public class FrameGrabberAndRecorder implements Runnable {

    static {
        avutil.av_log_set_level(avutil.AV_LOG_ERROR);
        FFmpegLogCallback.set();
    }

    //@formatter:off

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

    /** 当流为空时自动关比，单位 */
    private final int autoCloseAfter;

    //@formatter:on

    public FrameGrabberAndRecorder(String streamUrl) {
        this(streamUrl, 10_000);
    }

    /**
     * 构建推流拉流对象
     *
     * @param streamUrl      流地址
     * @param autoCloseAfter 流关闭时间，当待推送 channel 为空
     */
    public FrameGrabberAndRecorder(String streamUrl, int autoCloseAfter) {
        Assert.hasText(streamUrl, "streamUrl 不能为空");

        this.streamUrl = streamUrl;
        this.autoCloseAfter = autoCloseAfter;
        this.channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    public void start() {
        if (!this.runningStatus) {
            this.runningStatus = true;
            this.future = new CompletableFuture<>();
            new Thread(this).start();
        }
    }

    public void stop() {
        this.runningStatus = false;
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
            throw new RuntimeException("grabber 缓存清理失败");
        }

        // flv header + video tag1
        if (flvHeaders == null) {
            flvHeaders = bos.toByteArray();
        }
        future.complete(flvHeaders);
        bos.reset();

        // 开始
        long startAt = 0;
        long channelGroupEmptyAt = Long.MAX_VALUE;
        while (runningStatus) {
            try {
                var frame = grabber.grab();
                if (frame != null) {
                    if (startAt == 0) {
                        startAt = System.currentTimeMillis();
                    }
                    var videoTS = 1000 * (System.currentTimeMillis() - startAt);
                    if (videoTS > recorder.getTimestamp()) {
                        recorder.setTimestamp(videoTS);
                    }
                    recorder.record(frame);
                }

                if (bos.size() > 0) {
                    var data = bos.toByteArray();
                    bos.reset();

                    if (channelGroup.isEmpty()) {
                        var pre = channelGroupEmptyAt;
                        if (channelGroupEmptyAt == Long.MAX_VALUE) {
                            channelGroupEmptyAt = System.currentTimeMillis();
                        }

                        if (autoCloseAfter > 0 && System.currentTimeMillis() - pre > autoCloseAfter) {
                            runningStatus = false;
                            log.warn("[{}] 待推流 channels 为空，自动关闭当前推流拉流任务", streamUrl);
                        }
                    } else {
                        channelGroupEmptyAt = Long.MAX_VALUE;

                        // 开始分发
                        channelGroup.writeAndFlush(Unpooled.wrappedBuffer(data));
                    }
                }
            } catch (Exception e) {
                log.error(String.format("[%s] 拉流推流任务异常: %s", streamUrl, e.getMessage()), e);
                break;
            }
        }

        // 运行状态统统改为 false
        runningStatus = false;

        // 关闭相关资源
        try {
            grabber.close();
            recorder.close();
        } catch (FrameGrabber.Exception | FrameRecorder.Exception e) {
            log.error(e.getMessage(), e);
        }
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
        if (streamUrl.startsWith("rtsp")) {
            // 设置打开协议 tcp
            grabber.setOption("rtsp_transport", "tcp");
            //设置超时时间
            grabber.setOption("stimeout", "3000000");
        }

        try {
            grabber.start();
        } catch (FFmpegFrameGrabber.Exception e) {
            log.error("grabber start failed: " + e.getMessage(), e);
            future.completeExceptionally(e);
            this.runningStatus = false;
            throw new StreamxException(e);
        }
    }

    private void buildRecorder() {
        this.recorder = new FFmpegFrameRecorder(bos,
                grabber.getImageWidth(),
                grabber.getImageHeight(),
                grabber.getAudioChannels());
        recorder.setFormat("flv");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);

        recorder.setInterleaved(false);
        recorder.setVideoOption("tune", "zerolatency");
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setVideoOption("crf", "26");
        recorder.setVideoOption("threads", "1");
        recorder.setFrameRate(25);// 设置帧率
        recorder.setGopSize(25);// 设置gop,与帧率相同，相当于间隔1秒chan's一个关键帧
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setOption("keyint_min", "25");    //gop最小间隔
        recorder.setTrellis(1);
        recorder.setMaxDelay(0);// 设置延迟

        try {
            recorder.start();
        } catch (FFmpegFrameRecorder.Exception e) {
            log.error("recorder start failed: " + e.getMessage(), e);
            future.completeExceptionally(e);
            this.runningStatus = false;
            throw new StreamxException(e);
        }
    }
}
