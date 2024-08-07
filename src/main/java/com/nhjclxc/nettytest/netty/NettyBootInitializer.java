package com.nhjclxc.nettytest.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


/**
 * netty服务端启动类（Netty服务注册，bean注册）
 */
//@Component
@Slf4j
@Configuration
//@ConditionalOnProperty(name = "netty.enable", havingValue = "true")
public class NettyBootInitializer implements InitializingBean {

    @Value("${netty.port}")
    private Integer nettyPort;

    @Value("${netty.enable}")
    private Boolean enable;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!Boolean.TRUE.equals(enable)) {
            return;
        }
        ServerBootstrap bootstrap = new ServerBootstrap();
        // boss 线程组用于处理连接工作
        EventLoopGroup boss = new NioEventLoopGroup();
        // work 线程组用于数据处理
        EventLoopGroup work = new NioEventLoopGroup();
        bootstrap.group(boss, work).channel(NioServerSocketChannel.class)
                //服务端可连接队列数,对应TCP/IP协议listen函数中backlog参数
                .option(ChannelOption.SO_BACKLOG, 1024)
                //设置TCP长连接,一般如果两个小时内没有数据的通信时,TCP会自动发送一个活动探测数据报文
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                //将小的数据包包装成更大的帧进行传送，提高网络的负载
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        //为什么空闲检测要插入到最前面？因为如果插入到最后面的话，如果这条连接读到了数据，但是在传播的过程中出错了或者数据处理完后不往
                        //后面传递，最终IMIdleStateHandler就不会读到数据  、导致误判。
                        ch.pipeline().addLast(new IMIdleStateHandler());
                        ch.pipeline().addLast("http-codec", new HttpServerCodec()); //http编解码
                        ch.pipeline().addLast("aggregator", new HttpObjectAggregator(65536)); //httpContent消息聚合
                        ch.pipeline().addLast("compressor", new HttpContentCompressor()); // HttpContent 压缩
                        ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler()); //
                        ch.pipeline().addLast(NettyRequestHandler.getInstance()); // 注册消息处理句柄。可注册多个
//				ch.pipeline().addLast(ExceptionHandler.getInstance());
                    }
                });

        // 防止内存泄露
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
        bind(bootstrap, nettyPort);
    }

    public void bind(final ServerBootstrap serverBootstrap, final int port) {
        serverBootstrap.bind(port).addListener(new GenericFutureListener<Future<? super Void>>() {
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("webSocket 端口[" + port + "]绑定成功!");
                } else {
                    System.out.println("webSocket 端口[" + port + "]绑定失败!");
                    bind(serverBootstrap, port + 1);
                }
            }
        });
    }

}
