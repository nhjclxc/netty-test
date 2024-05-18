package com.nhjclxc.nettytest.biz;

import com.alibaba.fastjson2.JSONObject;
import com.nhjclxc.nettytest.netty.NettyRequestHandler;
import com.nhjclxc.nettytest.utils.MessageType;
import com.nhjclxc.nettytest.utils.NettyResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 测试在业务中获取netty对象
 */
@RestController
@RequestMapping("test")
public class TestController {

    @Autowired
    NettyRequestHandler handler;

    @Autowired
    ChatNettyProcessHandler chatHandler;

    @GetMapping()
    public void test2() {
        System.out.println("TestController.test" + handler);
    }

//    @Scheduled(fixedDelay = 5000)
    public void sendAll(){
        chatHandler.send(MessageType.CHAT, "测试" + System.currentTimeMillis());
    }
}
