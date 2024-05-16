package com.nhjclxc.nettytest.config;

import com.nhjclxc.nettytest.utils.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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
public class NettyResult<T> {
    private MessageType type;
    private T data;
    /**
     * 谁发起的
     */
    private Long userId;
    /**
     * 发给谁的
     */
    private Long toUserId;
}
