package com.jun.streamx.net;

/**
 * 服务器配置
 *
 * @author Jun
 * @since 1.0.0
 */
public class ServerProperties {
    //@formatter:Off

    /** 服务端监听 host */
    private String host = "0.0.0.0";
    /** 服务端监听端口 */
    private int port;
    /** tcp 握手队列 */
    private int soBacklog = 128;

    //@formatter:on

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getSoBacklog() {
        return soBacklog;
    }

    public void setSoBacklog(int soBacklog) {
        this.soBacklog = soBacklog;
    }

    @Override
    public String toString() {
        return "ServerProperties{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", soBacklog=" + soBacklog +
                '}';
    }
}
