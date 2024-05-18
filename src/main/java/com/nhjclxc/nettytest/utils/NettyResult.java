package com.nhjclxc.nettytest.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * netty 统一返回结果集
 *
 * @author LuoXianchao
 * @since 2023/10/03 10:14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class NettyResult {

    /** 消息唯一id */
    private String uuid;

    /** 消息类型 */
    private MessageType messageType;

    /** 具体消息 */
    private String context;

    /** 消息发送时间 */
    private LocalDateTime time;
    /**
     * 谁发起的
     */
    private Long userId;
    /**
     * 发给谁的
     */
    private Long destUserId;
    /**
     * 发给哪个群组
     */
    private Long groupId;
}
