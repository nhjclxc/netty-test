package com.nhjclxc.nettytest.biz;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.nhjclxc.nettytest.config.NettyResult;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * netty聊天融入业务
 *
 */
@Slf4j
@Component
public class ChatNettyProcessHandler {

//    @Autowired
//    NettyRequestHandler handler;

    /**
     * 发送聊天
     *
     * @param myChannel 发送用户的信道
     * @param msg 发送者发送的消息
     * @author 罗贤超
     */
    public void send(Channel myChannel, String msg){
        try {
//            System.out.println(handler);
// {"userId": 1, "toUserId": 1, "type": 1, "isOneToOne": true, "msgType": 1,"content": "你好 222"}
            chat(myChannel, JSON.parseObject(msg, MsgObject.class));
        } catch (JSONException je){
            log.error("netty聊天消息体格式错误，{}", je.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("netty聊天未知异常，{}", e.getMessage());
        }
    }

    private void chat(Channel myChannel, MsgObject msgObject) {

        Channel channel = ChatChannelHandlerPool.getChannel(msgObject.getToUserId());
        if (channel == null) {
            // 用户不在线，返回实时发送失败，转为立离线发送
            myChannel.writeAndFlush(new TextWebSocketFrame(JSONObject.from(NettyResult.error("用户不在线，消息已离线发送")).toJSONString()));

            //todo 把这条消息存起来， 保存聊天记录当用户上线的时候发给他
            return;
        }
        // 发送给目标用户
        channel.writeAndFlush(new TextWebSocketFrame(JSONObject.from(NettyResult.success(null, msgObject.getContent())).toJSONString()));

        // 消息发送状态返回给发送者
        myChannel.writeAndFlush(new TextWebSocketFrame(JSONObject.from(NettyResult.success("实时消息发送成功")).toJSONString()));
    }


    @Data
    private static class MsgObject{
        private Long userId;
        private Long toUserId;
        /**
         * 是否单聊
         */
        private Boolean isOneToOne;
        /**
         * 消息类型（1=文字，2=图片，3=音频，4=视频，...）
         */
        private Integer msgType;
        private String content;
    }
}
