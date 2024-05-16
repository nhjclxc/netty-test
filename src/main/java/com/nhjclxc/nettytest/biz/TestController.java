package com.nhjclxc.nettytest.biz;

import com.nhjclxc.nettytest.config.NettyRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试在业务中获取netty对象
 */
@RestController
@RequestMapping("test")
public class TestController {

    @Autowired
    NettyRequestHandler handler;

    @GetMapping()
    public void test2() {
        System.out.println("TestController.test" + handler);
    }
}
