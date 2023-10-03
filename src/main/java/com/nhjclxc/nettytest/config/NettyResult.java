package com.nhjclxc.nettytest.config;

/**
 * netty 统一返回结果集
 *
 * @author LuoXianchao
 * @since 2023/10/03 10:14
 */
public class NettyResult<T> {

    private Integer code;
    private String errMsg;
    private T data;

    public static <T> NettyResult<T> success(){
        return new NettyResult<>();
    }

    public static <T> NettyResult<T> success(T data){
        return new NettyResult<>(data);
    }

    public static <T> NettyResult<T> success(String msg, T data){
        return new NettyResult<>(200, msg, data);
    }

    public static <T> NettyResult<T> error(String msg){
        return new NettyResult<>(500, msg, null);
    }

    public NettyResult() {
        this(200, "成功", null);
    }

    public NettyResult(T data) {
        this(200, null, data);
    }
    public NettyResult(Integer code, T data) {
        this(code, "", data);
    }

    public NettyResult(Integer code, String errMsg, T data) {
        this.code = code;
        this.errMsg = errMsg;
        this.data = data;
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getErrMsg() {
        return this.errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }


    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
