package com.nhjclxc.nettytest.biz;


import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * netty信道池，netty信道与用户id要一一对应
 */
public class ChatChannelHandlerPool {

    private ChatChannelHandlerPool() {  }

    /**
     * 保存当前的所有信道
     */
    private static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 信道与用户关联
     */
    private static final Map<Long, ChannelId> userChannelIdMap = new ConcurrentHashMap<>(32);


    /**
     * 客户端链接到netty服务时，保存该用户的信道
     *
     * @param channel 信道
     * @author 罗贤超
     */
    public static void saveChannel(Long userId, Channel channel) {
        channelGroup.add(channel);
        userChannelIdMap.put(userId, channel.id());
    }

    /**
     * 客户端关闭连接时，移除该用户的信道
     *
     * @param channel 信道
     * @author 罗贤超
     */
    public static boolean removeChannel(Channel channel) {
        // 移除信道池里面的信道
        boolean flag = channelGroup.remove(channel);

        // 移除用户与信道对应关系
        // 信道还存在才去移除
        if (flag){
            ChannelId channelId = channel.id();
            if (userChannelIdMap.containsValue(channelId)){
                Long userId = null;
                for (Map.Entry<Long, ChannelId> entry : userChannelIdMap.entrySet()) {
                    if (channelId.equals(entry.getValue())){
                        userId = entry.getKey();
                        break;
                    }
                }
                if (userId == null){
                    return flag;
                }
                userChannelIdMap.remove(userId);
            }
        }

        return flag;
    }

    /**
     * 获取信道
     *
     * @param id 信道id
     * @return 信道
     * @author 罗贤超
     */
    public static Channel getChannel(ChannelId id) {
        return channelGroup.find(id);
    }

    /**
     * 获取某个用户的信道
     *
     * @param userId userId
     * @return 信道
     * @author 罗贤超
     */
    public static Channel getChannel(Long userId) {
        if (userChannelIdMap.containsKey(userId)){
            ChannelId channelId = userChannelIdMap.get(userId);
            return channelGroup.find(channelId);
        }
        //todo 保存聊天记录当用户上线的时候发给他
        return null;
    }

}
