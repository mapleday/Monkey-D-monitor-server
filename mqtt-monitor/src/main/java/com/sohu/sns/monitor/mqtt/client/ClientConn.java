package com.sohu.sns.monitor.mqtt.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jinyingshi on 2016/6/3.
 */
public class ClientConn {
    private static final ClientConn conn = new ClientConn();
    private Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>();
    private static final Random random = new Random();

    private ClientConn() {
    }

    public static ClientConn getInstance() {
        return conn;
    }

    public ChannelFuture writeAndFlush(String channelNodeId, Object message) {
        Channel channel = channels.get(channelNodeId);
        if (channel != null) {
            return channel.writeAndFlush(message);
        }
        return null;
    }

    public ChannelFuture writeAndFlushRandom(Object message) {
        int size = channels.size();
        int i = Math.abs(random.nextInt(size));
        Object[] objects = channels.keySet().toArray();
        Object object = objects[i];
        return writeAndFlush(object.toString(), message);
    }

    public void add(Channel channel) {
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        InetAddress address = socketAddress.getAddress();
        String hostAddress = address.getHostAddress();
        int port = socketAddress.getPort();
        channels.put(toAddressString(hostAddress, port), channel);
    }

    private String toAddressString(String ip, int port) {
        return ip + ":" + port;
    }

}
