package com.sohu.sns.monitor.netty;

import io.netty.channel.Channel;
import io.netty.util.concurrent.EventExecutor;

/**
 * Will handle all the I/O-Operations for a {@link Channel} once it was registered.
 *
 * One {@link EventLoop} instance will usually handle more then one {@link Channel} but this may depend on
 * implementation details and internals.
 *
 */
public interface EventLoop extends EventExecutor, EventLoopGroup {
    @Override
    EventLoopGroup parent();
}
