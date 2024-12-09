package com.nhjclxc.nettytest.netty;

import com.alibaba.fastjson2.JSONObject;
import com.nhjclxc.nettytest.biz.ChatNettyProcessHandler;
import com.nhjclxc.nettytest.utils.MessageType;
import com.nhjclxc.nettytest.utils.NettyResult;
import com.nhjclxc.nettytest.utils.TokenUtils;
import io.jsonwebtoken.Claims;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * netty消息处理句柄
 * <p>
 * NettyRequestHandler
 */
@Slf4j
@Sharable
@Component
public class NettyRequestHandler extends SimpleChannelInboundHandler<Object> {
//public class HttpRequestHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    /**
     * 防止通过new的方式创建对象，new的方式无法交给spring管理
     */
    private NettyRequestHandler() {
    }

    private WebSocketServerHandshaker handshaker;

    private static NettyRequestHandler INSTANCE;

    /**
     * 因为是new出来的handler,没有托给spring容器,所以一定要先初始化,否则autowired失效
     * <a href="https://blog.csdn.net/zisuu/article/details/105922371">...</a>
     */
    @PostConstruct
    public void init() {
        INSTANCE = this;
    }

    public static NettyRequestHandler getInstance() {
        return INSTANCE;
    }


    @Resource
    private ChatNettyProcessHandler chatNettyProcessHandler;

    @Resource
    private TokenUtils tokenUtils;


    // 令牌自定义标识
    @Value("${token.header}")
    private String tokenHeader;


    /**
     * 异常处理，关闭channel
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        if (cause instanceof Exception) {
            log.info("netty服务异常");
            ByteBuf byteBuf = ctx.alloc().buffer();
            com.alibaba.fastjson.JSONObject data = new com.alibaba.fastjson.JSONObject();
            data.put("type", 500);
            data.put("msg", "netty服务异常");
            byteBuf.writeBytes(data.toJSONString().getBytes());
            ctx.channel().writeAndFlush(new TextWebSocketFrame(byteBuf));
        }

        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 第一次连接才会调用
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
    }

    /**
     * 接收请求
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        log.info("channelRead");
        // 请求分发
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
            // 校验权限
            checkAuthorization(ctx, msg);
        } else if (msg instanceof PingWebSocketFrame) {
            pingWebSocketFrameHandler(ctx, (PingWebSocketFrame) msg);
        } else if (msg instanceof TextWebSocketFrame) {
            textWebSocketFrameHandler(ctx, (TextWebSocketFrame) msg);
        } else if (msg instanceof CloseWebSocketFrame) {
            closeWebSocketFrameHandler(ctx, (CloseWebSocketFrame) msg);
        }
        super.channelRead(ctx, msg);

        // netty使用中的LEAK: ByteBuf.release() was not called before it‘s garbage-collected
        // 读完就释放
//        assert msg instanceof ByteBuf;
//        ((ByteBuf) msg).release();
    }

    private void checkAuthorization(ChannelHandlerContext ctx, Object msg) {
        // 只有第一次请求才会进入，这时获取请求包头，里面的token
        // token鉴权用户与信道进行绑定
        if (msg instanceof FullHttpRequest) {
            log.info("webSocket鉴权");
            FullHttpRequest msg1 = (FullHttpRequest) msg;

            Long userId = null;
            try {
                // 获取请求携带的令牌
                HttpHeaders headers = msg1.headers();
                String authorization = headers.get(tokenHeader);
                log.info("webSocket鉴权Authorization = " + authorization);
                userId = checkToken(authorization);

                // 从请求参数里面获取
                if (userId == null){
// ws://127.0.0.1:8085?Authorization=eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJsb2dpbl91c2VyX2lkIjoiMTExIn0.ndtHCsJ8YJgI2z1tDrFN0VbFqkwiGUw9E5YaXPcAv_d4cAt0HNR7D3k4Aq7IODgatXl-CBJ3wGEm4br8ic2QqA
                    String uri = msg1.uri();
                    String query = uri.replace("/?", "");
                    Map<String, String> queryParams = Stream.of(query.split("&"))
                            .map(param -> param.split("="))
                            .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));

                    String token = queryParams.get(tokenHeader);

                    userId = checkToken(token);
                }

                if (userId == null){
                    throw new RuntimeException("无效的token，获取用户数据失败！！！");
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("获取用户token失败");
            }
            // 信道与userId关联
            if (userId != null) {
                ChatChannelHandlerPool.saveChannel(userId, ctx.channel());
//                ChatChannelHandlerPool.saveChannelByToken(userToken, ctx.channel());
//                ChatChannelHandlerPool.saveChannelByUserId(userId, ctx.channel());
//                ChatChannelHandlerPool.saveChannelByProjectId(projectId, ctx.channel());
//                ChatChannelHandlerPool.saveChannelByCompanyId(companyId, ctx.channel());

                // 每一次请求都会进入，获取请求包头
                log.info("接收到客户端的握手包：{}", ctx.channel().id());
            } else {
                // 鉴权失败，向客户端发送消息，然后关闭连接
                Channel channel = ctx.channel();
                channel.writeAndFlush(new TextWebSocketFrame(JSONObject.from(NettyResult.builder().messageType(MessageType.NOT_AUTHORIZATION).build()).toJSONString()));
                ChannelPromise promise = ctx.newPromise();
                promise.addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        System.out.println("Channel closed successfully");
                    } else {
                        future.cause().printStackTrace();
                    }
                });
                ctx.close(promise);
            }
        }
    }


    private Long checkToken(String token) {
        if (token == null){
            return null;
        }
        if (StringUtils.isNotEmpty(token) && TokenUtils.TOKEN_PREFIX.startsWith(token)) {
            token = token.replace(TokenUtils.TOKEN_PREFIX, "");
        }
        // 密钥 12345678
// eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJsb2dpbl91c2VyX2lkIjoiMTExIn0.ndtHCsJ8YJgI2z1tDrFN0VbFqkwiGUw9E5YaXPcAv_d4cAt0HNR7D3k4Aq7IODgatXl-CBJ3wGEm4br8ic2QqA
        Claims claims = tokenUtils.parseToken(token);
        // 解析对应的权限以及用户信息
        return Long.parseLong((String) claims.get(TokenUtils.LOGIN_USER_KEY));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.channel().flush();
    }

    /**
     * 与客户端连接成功
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("与客户端连接成功，通道：{}", ctx.channel().id());
        //添加到通道组
//        // TODO 在这里融入你的业务 1，保存用户信道，用于聊天
//        //todo 添加到信道池里面
//        ChatChannelHandlerPool.saveChannel(ctx.channel());
        super.channelActive(ctx);
    }

    /**
     * 与客户端连接断开
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("与客户端连接断开，通道: {}", ctx.channel().id());
//        ChatChannelHandlerPool.channelGroup.remove(ctx.channel());
//        ChatChannelHandlerPool.removeChannelId(ctx.channel().id());
        // TODO 在这里融入你的业务 3，关闭用户信道，节省资源
        //todo 从信道池里面删除，用户不在线的时候，考虑是否要把聊天记录保存起来
        ChatChannelHandlerPool.removeChannel(ctx.channel());
        super.channelInactive(ctx);
    }

    /**
     * 创建连接之后，客户端发送的消息都会在这里处理
     */
    private void textWebSocketFrameHandler(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String text = frame.text();
        log.info("接收到客户端的消息：{}", text);
        if (text != null && !"".equals(text) && text.toLowerCase().contains("ping")){
            // 心跳篇消息
            Channel channel = ctx.channel();
            channel.writeAndFlush(new TextWebSocketFrame("Pong"));
        }else {
            // TODO 在这里融入你的业务 2，聊天
            chatNettyProcessHandler.send(ctx.channel(), text);
            ctx.fireChannelRead(text);
        }
    }

    /**
     * 处理Http请求，主要是完成HTTP协议到Websocket协议的绑定
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (!request.decoderResult().isSuccess()) {
            // 链接失败处理
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            // 返回应答给客户端
            if (response.status().code() != 200) {
                ByteBuf buf = Unpooled.copiedBuffer(response.status().toString(), CharsetUtil.UTF_8);
                response.content().writeBytes(buf);
                buf.release();
            }
            // 如果是非Keep-Alive，关闭连接
            boolean keepAlive = HttpUtil.isKeepAlive(request);
            ChannelFuture f = ctx.channel().writeAndFlush(response);
            if (!keepAlive) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
            return;
        }

        // 链接成功，创建信道
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                "ws:/" + ctx.channel() + "/websocket", null, false);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);
        this.handshaker = handshaker;
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), request);
        }
    }

    /**
     * 客户端发送断开请求处理
     */
    private void closeWebSocketFrameHandler(ChannelHandlerContext ctx, CloseWebSocketFrame frame) {
        // TODO 在这里融入你的业务 3，关闭用户信道，节省资源
        //todo 从信道池里面删除，用户不在线的时候，考虑是否要把聊天记录保存起来
        ChatChannelHandlerPool.removeChannel(ctx.channel());

        log.info("接收到主动断开请求：{}", ctx.channel().id());
        ctx.close();
    }


    /**
     * 处理客户端心跳包
     */
    private void pingWebSocketFrameHandler(ChannelHandlerContext ctx, PingWebSocketFrame frame) {
        log.info("Ping 客户端心跳检测：{}", frame.content().toString());
        ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
    }

}
