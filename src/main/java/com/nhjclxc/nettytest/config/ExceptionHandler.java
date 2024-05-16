package com.nhjclxc.nettytest.config;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;


/**
 * Netty服务异常处理机制
 */
@Sharable
public class ExceptionHandler extends ChannelDuplexHandler {

    private static ExceptionHandler INSTANCE;

    /**
     * 因为是new出来的handler,没有托给spring容器,所以一定要先初始化,否则autowired失效
     * <a href="https://blog.csdn.net/zisuu/article/details/105922371">...</a>
     */
    @PostConstruct
    public void init() {
        INSTANCE = this;
    }

    public static ExceptionHandler getInstance() {
        return INSTANCE;
    }


    private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof RuntimeException) {
            log.info("netty服务异常");
            ByteBuf byteBuf = ctx.alloc().buffer();
            JSONObject data = new JSONObject();
            data.put("type", 500);
            data.put("msg", "netty服务异常");
            byteBuf.writeBytes(data.toJSONString().getBytes());
            ctx.channel().writeAndFlush(new TextWebSocketFrame(byteBuf));
        }
    }
}

