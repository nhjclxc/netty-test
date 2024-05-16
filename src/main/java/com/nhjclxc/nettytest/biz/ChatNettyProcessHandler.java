package com.nhjclxc.nettytest.biz;

import com.alibaba.fastjson2.JSONObject;
import com.nhjclxc.nettytest.config.ChatChannelHandlerPool;
import com.nhjclxc.nettytest.config.NettyResult;
import com.nhjclxc.nettytest.utils.CustomThreadPoolExecutor;
import com.nhjclxc.nettytest.utils.MessageType;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * netty聊天融入业务
 */
@Slf4j
@Component
public class ChatNettyProcessHandler {

    /**
     * 向指定信道发送消息
     *
     * @param channel 目标用户的信道
     * @param type    发送的类型 {@link com.nhjclxc.nettytest.utils.MessageType}
     * @param data    发送的消息 , json格式
     * @author 罗贤超
     */
    public void send(Channel channel, MessageType type, Object data, Long userId, Long toUserId) {
        if (channel == null)
            throw new RuntimeException("目标信道不能为空！！！");

        try {
            NettyResult<Object> result = NettyResult.builder().type(type).data(data).userId(userId).toUserId(toUserId).build();
            ChannelFuture channelFuture = channel.writeAndFlush(new TextWebSocketFrame(JSONObject.from(result).toJSONString()));
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    boolean success = channelFuture.isSuccess();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            log.error("netty聊天未知异常，{}", e.getMessage());
        }
    }

    /**
     * 向指定用户发送消息
     */
    public void send(MessageType type, Object data, Long userId, Long toUserId) {
        if (toUserId == null) throw new RuntimeException("目标用户不能为空");

        Channel channel = ChatChannelHandlerPool.getChannel(toUserId);
        send(channel, type, data, userId, toUserId);
    }

    /**
     * 指定群聊发送数据
     */
    public void send(Long groupId, Object data) {
        CustomThreadPoolExecutor.execute(() -> {
            List<Channel> groupAllChannel = ChatChannelHandlerPool.getGroupAllChannel(groupId);
            for (Channel channel : groupAllChannel) {
                send(channel, null, data, null, null);
            }
        });
    }

    /**
     * 指定信道发送数据
     */
    public void send(Channel channel, Object data) {
        send(channel, null, data, null, null);
    }

    /**
     * 向所有在线用户发送消息
     */
    public void send(MessageType type, Object data) {
        List<Channel> allChannel = ChatChannelHandlerPool.getAllChannel();
        for (Channel channel : allChannel) {
            send(channel, type, data, null, null);
        }
    }

}
