package com.nhjclxc.nettytest.client;

import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;

/**
 *
 * 使用Spring的实现WebSocketStompClient
 *
 * 使用Spring的WebSocketStompClient来实现WebSocket通信，通常涉及以下步骤：
 *      1、添加依赖
 *      2、配置WebSocketStompClient
 *      3、创建一个StompSessionHandler来处理会话
 *      4、连接到WebSocket服务器并发送和接收消息

 <dependency>
 <groupId>org.springframework.boot</groupId>
 <artifactId>spring-boot-starter-websocket</artifactId>
 </dependency>
 <dependency>
 <groupId>org.springframework</groupId>
 <artifactId>spring-messaging</artifactId>
 </dependency>
 <dependency>
 <groupId>org.springframework</groupId>
 <artifactId>spring-websocket</artifactId>
 </dependency>



 *
 * @author LuoXianchao
 * @since 2024/07/12 15:31
 */
public class SpringWebSocketStompClient extends StompSessionHandlerAdapter {

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        System.out.println("Connected! Session: " + session.getSessionId());

        // Subscribe to a topic
        session.subscribe("/app/hello", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("Received: " + payload);
            }
        });

        // Send a message
        session.send("/app/hello", "{\"destUserId\": 111, \"context\": \"contextcontextcontext\"}");
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        System.err.println("Error: " + exception.getMessage());
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        System.err.println("Transport Error: " + exception.getMessage());
    }


    // 测试
    public static void main(String[] args) throws InterruptedException {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());

        String token = "eyJhbGciOiJIUzUxMiJ9.eyJsb2dpbl91c2VyX2lkIjoiMjIyIn0.N866D48lDYFuORf9UhMlL6LHHmCrhZL4yizX267MJR-ZpbRGhztap3zTm7pVrUfOTSN_RDYxj2tNubWkRnqh0g";
        String url = "ws://127.0.0.1:8351?Authorization=" + token;

        SpringWebSocketStompClient sessionHandler = new SpringWebSocketStompClient();
        stompClient.connect(url, sessionHandler);

        // Keep the client running
        Thread.sleep(10000);
    }
}
