package com.nhjclxc.nettytest.netty;


import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * netty信道池，netty信道与用户id要一一对应
 */
public class ChatChannelHandlerPool {

    private ChatChannelHandlerPool() {
    }

    /**
     * 保存当前的所有信道
     */
    private static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 信道与用户关联
     */
    private static final Map<Long, ChannelId> userChannelIdMap = new ConcurrentHashMap<>(32);

    /**
     * 实现群聊的基础版
     * key（Long）相当于是群聊id，value（List<Long>）相当于是群聊里面的所有人
     */
    // Map<groupId, allUserId>
    private static final Map<Long, List<Long>> groupChannelIdMap = new ConcurrentHashMap<>(32);


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

    public static Long getUserIdByChannelId(ChannelId destChannelId){
        // 遍历Map，查找对应的userId
        Long userId = null;
        for (Map.Entry<Long, ChannelId> entry : userChannelIdMap.entrySet()) {
            if (entry.getValue().equals(destChannelId)) {
                userId = entry.getKey();
                break;
            }
        }
        return userId;
    }

    /**
     * 项目启动的时候初始化群聊关系
     * 以下数据应当存在数据库里面进行持久化
     * @param groupId 群聊id
     * @param userIdList 群聊用户
     * @author 罗贤超
     */
    public static void saveGroupUser(Long groupId, List<Long> userIdList) {
        groupChannelIdMap.put(groupId, userIdList);
    }

    /**
     * 某一个人加入群聊
     */
    public static void saveGroupUser(Long groupId, Long userId) {
        List<Long> userIdList = getGroupAllUserId(groupId);
        userIdList.add(userId);
    }

    /**
     * 某一个人退出群聊
     */
    public static void removeGroupUser(Long groupId, Long userId) {
        List<Long> userIdList = getGroupAllUserId(groupId);
        userIdList.remove(userId);
    }

    /**
     * 获取群聊里面的所有用户
     */
    public static List<Long> getGroupAllUserId(Long groupId) {
        List<Long> userIdList = groupChannelIdMap.get(groupId);
        if (userIdList == null){
            throw new RuntimeException("群聊不存在");
        }
        return userIdList;
    }

    /**
     * 获取某个群聊的所有信道
     */
    public static Map<Long, Channel> getGroupAllChannel(Long groupId) {
        List<Long> userIdList = getGroupAllUserId(groupId);
        return getChannelListByUserIdList(userIdList);
    }

    /**
     * 解散群聊
     */
    public static void removeGroup(Long groupId) {
        groupChannelIdMap.remove(groupId);
    }



    public static List<Channel> getChannelList(List<ChannelId> channelIdList) {
        return channelGroup.stream().filter(channel -> channelIdList.contains(channel.id())).collect(Collectors.toList());
    }


    public static Map<Long, Channel> getChannelListByUserIdList(List<Long> userIdList) {
        Map<Long, Channel> userChannelMap = new HashMap<>();
        userChannelIdMap.forEach((userId, channelId) -> {
            if (userIdList.contains(userId)){
                userChannelMap.put(userId, channelGroup.find(channelId));
            }
        });
        return userChannelMap;
    }


    /**
     * 客户端关闭连接时，移除该用户的信道
     *
     * @param channel 信道
     * @author 罗贤超
     */
    public static void removeChannel(Channel channel) {
        // 移除信道池里面的信道
        boolean flag = channelGroup.remove(channel);

        // 移除用户与信道对应关系
        // 信道还存在才去移除
        if (flag) {
            ChannelId removeChannelId = channel.id();
            // 移除用户 Map<Long, ChannelId> userChannelIdMap
            Iterator<Map.Entry<Long, ChannelId>> iterator = userChannelIdMap.entrySet().iterator();
            Long userId = null;
            while (iterator.hasNext()) {
                Map.Entry<Long, ChannelId> entry = iterator.next();
                ChannelId value = entry.getValue();
                if (value.equals(removeChannelId)) {
                    iterator.remove();
                    userId = entry.getKey();
                    break;
                }
            }

            if (userId != null) {

            }


        }
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
        if (userChannelIdMap.containsKey(userId)) {
            ChannelId channelId = userChannelIdMap.get(userId);
            return channelGroup.find(channelId);
        }
        //todo 保存聊天记录当用户上线的时候发给他
        return null;
    }

    /**
     * 获取所有信道
     */
    public static List<Channel> getAllChannel() {
        return new ArrayList<>(channelGroup);
    }

}
