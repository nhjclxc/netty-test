package com.nhjclxc.nettytest.utils;

/**
 * 状态类型
 */
public enum MessageType {
    SUCCESS("SUCCESS", "成功"),
    ERROR("ERROR", "失败"),
    SEND_RESULT("SEND_RESULT", "消息发送结果"),
    NOT_AUTHORIZATION("NOT_AUTHORIZATION", "用户鉴权失败"),
    CHAT("CHAT", "单人聊天"),
    MULTI_CHAT("MULTI_CHAT", "多人聊天");
    public final String key;
    public final String value;

    MessageType(String key, String value) {
        this.value = value;
        this.key = key;
    }
}
