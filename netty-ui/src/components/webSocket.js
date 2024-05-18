

import {isEmpty, isNotEmpty} from '@/components/utils'

class MyWebSocket {
    constructor(url, callback) {
        this.socket = new WebSocket(url);

        this.socket.addEventListener('open', () => {
            console.log('WebSocket 连接已打开');
            callback(WebSocketOptType.Open)

            // 启动心跳检测
            // this.startHeartbeat(3, 'Ping');
        });

        // 监听 WebSocket 连接关闭事件
        this.socket.addEventListener('close', () => {
            console.log('WebSocket 连接已关闭');
            callback(WebSocketOptType.Close)
            // 停止心跳检测
            this.stopHeartbeat();
        });

        // 监听 WebSocket 消息事件
        this.socket.addEventListener('message', (event) => {
            // console.log('收到消息:', event.data);

            // 如果收到 Pong 消息，则说明连接正常，继续心跳检测
            if (event.data.toLowerCase().includes('pong')) {
                // 心跳响应数据
                console.log('收到 Pong 消息，连接正常');
                callback(WebSocketOptType.Pong)
                return
            }

            if (isNotEmpty(callback)) {
                callback(WebSocketOptType.Message, event.data)
            }
        });

        // 监听 WebSocket 错误事件
        this.socket.addEventListener('error', (error) => {
            console.error('WebSocket 错误:', error);
            callback(WebSocketOptType.Error)
        });

    }

    /**
     * 关闭连接
     */
    close() {
        console.log('close', this.socket)
        this.socket.close()
    }

    /**
     * 消息发送
     */
    send(msg) {
        let str = JSON.stringify(msg)
        this.socket.send(str)
    }

    /**
     * 启动心跳检测
     */
    startHeartbeat(heartbeatInterval, heartbeatIntervalSendContext) {
        // this.socket.ping()
        // 发送心跳消息并定时执行
        this.heartbeatIntervalId = setInterval(() => this.heartbeat(this.socket), heartbeatInterval * 1000);
    }
    /**
     * 定义心跳检测函数
     * @param socket
     */
    heartbeat(socket) {
        // console.log('发送心跳消息');
        // 发送 Ping 消息
        socket.send('ping');
        console.log('心跳检测')
    }

    /**
     * 停止心跳检测
     */
    stopHeartbeat() {
        if (isNotEmpty(this.heartbeatIntervalId)){
            console.log('this.heartbeatIntervalId', this.heartbeatIntervalId)
            // 清除定时器
            clearInterval(this.heartbeatIntervalId);
            console.log('心跳关闭')
        }
    }

}

function initMyWebSocket(url, callback) {
    return new MyWebSocket(url, callback);
}

const WebSocketOptType = {
    Open: 'Open',
    Close: 'Close',
    Error: 'Error',
    Ping: 'Ping',
    Pong: 'Pong',
    Message: 'Message',
}

const MessageType = {
    SUCCESS: 'SUCCESS', // 成功
    ERROR: 'ERROR', //失败
    SEND_RESULT: 'SEND_RESULT', // 消息发送结果
    NOT_AUTHORIZATION: 'NOT_AUTHORIZATION', // 用户鉴权失败
    CHAT: 'CHAT', // 单人聊天
    MULTI_CHAT: 'MULTI_CHAT', // 多人聊天
}

export {initMyWebSocket, WebSocketOptType, MessageType}
