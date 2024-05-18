package com.nhjclxc.nettytest.biz;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.nhjclxc.nettytest.netty.ChatChannelHandlerPool;
import com.nhjclxc.nettytest.utils.NettyResult;
import com.nhjclxc.nettytest.utils.CustomThreadPoolExecutor;
import com.nhjclxc.nettytest.utils.MessageType;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * netty聊天融入业务
 */
@Slf4j
@Component
public class ChatNettyProcessHandler {

    /**
     * 向指定信道发送消息
     *
     * @param currentChannel 当前用户的信道
     * @param destChannel    目标用户的信道
     * @author 罗贤超
     */
    public void send(Channel currentChannel, Channel destChannel, NettyResult result) {
        if (destChannel == null){
            callback(currentChannel, "用户不在线无法接收消息");
//            throw new RuntimeException("目标信道不能为空！！！");
            return;
        }

        try {
            String finalMsg = JSONObject.from(result).toJSONString();
            ChannelFuture channelFuture = destChannel.writeAndFlush(new TextWebSocketFrame(finalMsg));
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    boolean success = channelFuture.isSuccess();
                    log.info("webSocket消息发送结果：{}, {}", finalMsg, success);

                    // 反馈是否发送成功
                    if (currentChannel != null) {
                        currentChannel.writeAndFlush(new TextWebSocketFrame(JSONObject.from(NettyResult.builder().uuid(result.getUuid()).messageType(MessageType.SEND_RESULT).context(success + "").build()).toJSONString()));
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            log.error("netty聊天未知异常，{}", e.getMessage());
        }
    }


    public void send(Channel currentChannel, String context) {
        NettyResult nettyResult = null;
        try {
            nettyResult = JSONObject.parseObject(context, NettyResult.class);
        } catch (JSONException ignored) {}
        if (nettyResult == null){
            callback(currentChannel, "无法解析消息格式");
            return;
        }

        Long userId = getCurrentChannelUserId(currentChannel);
        nettyResult.setUserId(userId);

        Long groupId = nettyResult.getGroupId();
        if (groupId != null) {
            // 群聊
            send2Group(currentChannel, nettyResult);
            return;
        }
        Long destUserId = nettyResult.getDestUserId();
        if (destUserId != null) {
            // 单聊
            send2User(currentChannel, nettyResult);
        }
    }

    private static void callback(Channel channel, String msg) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String context = JSONObject.from(NettyResult.builder().uuid(uuid).messageType(MessageType.ERROR).context(msg).build()).toJSONString();
        channel.writeAndFlush(new TextWebSocketFrame(context));
    }

    /**
     * 向指定用户发送数据
     */
    public void send2User(Channel currentChannel, NettyResult nettyResult) {
        Channel destChannel = ChatChannelHandlerPool.getChannel(nettyResult.getDestUserId());
        send(currentChannel, destChannel, nettyResult);
    }

    /**
     * 向指定群聊发送数据
     */
    public void send2Group(Channel currentChannel, NettyResult nettyResult) {
        CustomThreadPoolExecutor.execute(() -> {
            Map<Long, Channel> userChannelMap = ChatChannelHandlerPool.getGroupAllChannel(nettyResult.getGroupId());
            userChannelMap.forEach((destUserId, destChannel) -> {
                send(currentChannel, destChannel, nettyResult);
            });
        });
    }

    private static Long getCurrentChannelUserId(Channel currentChannel) {
        Long userId = null;
        if (currentChannel != null) {
            userId = ChatChannelHandlerPool.getUserIdByChannelId(currentChannel.id());
        }
        return userId;
    }

    /**
     * 向所有在线用户发送消息
     */
    public void send(MessageType messageType, String context) {
        List<Channel> allChannel = ChatChannelHandlerPool.getAllChannel();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        NettyResult build = NettyResult.builder().messageType(messageType).uuid(uuid).context(context).time(LocalDateTime.now()).userId(null).destUserId(null).groupId(null).build();
        for (Channel destChannel : allChannel) {
            send(null, destChannel, build);
        }
    }

}
