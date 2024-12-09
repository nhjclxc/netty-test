package com.nhjclxc.nettytest.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

/**
 * Java代码的WebSocket API实现
 *
 * @author LuoXianchao
 * @since 2024/07/12 15:26
 */

@ClientEndpoint
public class JavaxWebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(JavaxWebSocketClient.class);

    private Session session;

    private JavaxWebSocketClient() { }

    public Session getSession() {
        return session;
    }


    /**
     * ws服务器连接成功之后的回调
     */
    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to server");
        this.session = session;
    }

    /**
     * 接收消息
     */
    @OnMessage
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
    }

    /**
     * 连接被服务端关闭后回调
     */
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Connection closed: " + closeReason.getReasonPhrase());
    }

    /**
     * 出现异常时被回调的方法
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Error occurred: " + throwable.getMessage());
        throwable.printStackTrace();
    }

    /**
     * 发送消息的方法
     */
    public void sendMessage(String message) {
        if (this.session != null && this.session.isOpen()) {
            this.session.getAsyncRemote().sendText(message);
        } else {
            System.err.println("Cannot send message, session is closed or null");
        }
    }

    public static JavaxWebSocketClient connectToServer(String url){
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            JavaxWebSocketClient client = new JavaxWebSocketClient();
            container.connectToServer(client, URI.create(url));
            return client;
        } catch (DeploymentException e) {
            log.info("WebScoket服务连接失败，{}", e.getMessage());
            throw new RuntimeException("WebScoket服务连接失败，服务端未启动");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJsb2dpbl91c2VyX2lkIjoiMjIyIn0.N866D48lDYFuORf9UhMlL6LHHmCrhZL4yizX267MJR-ZpbRGhztap3zTm7pVrUfOTSN_RDYxj2tNubWkRnqh0g";
        String url = "ws://127.0.0.1:8351?Authorization=" + token;
        JavaxWebSocketClient webSocketClient = JavaxWebSocketClient.connectToServer(url);
        webSocketClient.sendMessage("{\"destUserId\": 111, \"context\": \"contex11111tcontextcontext\"}");

        Thread.sleep(50000); // 或根据需要调整时间
    }

    public static void main2(String[] args) {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJsb2dpbl91c2VyX2lkIjoiMjIyIn0.N866D48lDYFuORf9UhMlL6LHHmCrhZL4yizX267MJR-ZpbRGhztap3zTm7pVrUfOTSN_RDYxj2tNubWkRnqh0g";
        String uri = "ws://127.0.0.1:8351?Authorization=" + token;
        try {
            JavaxWebSocketClient client = new JavaxWebSocketClient();
            Session session = container.connectToServer(client, URI.create(uri));


            // 等待连接建立
            Thread.sleep(1000);

            // 发送消息
            client.sendMessage("{\"destUserId\": 111, \"context\": \"contex11111tcontextcontext\"}");

//            session.close();
//            client.sendMessage("{\"destUserId\": 111, \"context\": \"contex11111tcontextcontext\"}");

            // 保持客户端运行一段时间以接收响应
            Thread.sleep(5000); // 或根据需要调整时间
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
