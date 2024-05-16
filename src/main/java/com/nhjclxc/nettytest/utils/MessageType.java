package com.nhjclxc.nettytest.utils;

/**
 * 状态类型
 */
public enum MessageType {
    SUCCESS("SUCCESS", "成功"),
    ERROR("ERROR", "失败"),
    NOT_AUTHORIZATION("NOT_AUTHORIZATION", "用户鉴权失败"),
    TYPE1("TYPE1", "消息一"),
    TYPE2("TYPE2", "消息二");
    public final String key;
    public final String value;

    MessageType(String key, String value) {
        this.value = value;
        this.key = key;
    }
}
