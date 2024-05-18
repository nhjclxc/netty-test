package com.nhjclxc.nettytest.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


/**
 * 空闲连接检测   如果规定时间没有数据传输则关闭连接    不能实现为单例模式  每个连接都有各自的状态
 * <p>
 * 客户端超时未发送心跳检测，将与客户端断开连接
 */
public class IMIdleStateHandler extends IdleStateHandler {

    /**
     * 超时响应时间
     */
    private static final Integer OVERTIME = 300;

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

    public IMIdleStateHandler() {
        /**
         * 第一个参数表示读空闲时间
         * 第二个参数表示写空闲时间
         * 第三个参数表示读写空闲时间
         * 第四个参数表示时间单位
         */
        super(OVERTIME, 0, 0, TimeUnit.SECONDS);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        log.error("客户端：{}，{}秒内发送心跳检测超时，关闭当前信道", ctx.channel().id(), OVERTIME);
        // 在信道池里面移除该信道
        ChatChannelHandlerPool.removeChannel(ctx.channel());
        ctx.channel().close();
    }
}
