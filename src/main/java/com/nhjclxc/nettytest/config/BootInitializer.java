package com.nhjclxc.nettytest.config;

import com.nhjclxc.nettytest.utils.CustomThreadPoolExecutor;
import com.nhjclxc.nettytest.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.*;

/**
 * 服务预热
 */
@Slf4j
@Configuration
public class BootInitializer implements ApplicationRunner, EnvironmentAware {


    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


    @Override
    public void run(ApplicationArguments args) {
        // 模拟群聊数据
        List<List<Long>> init = new ArrayList<>();
        init.add(new ArrayList<>(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L)));
        init.add(new ArrayList<>(Arrays.asList(5L, 6L, 7L, 8L)));
        init.add(new ArrayList<>(Arrays.asList(6L, 8L, 9L)));
        // 初始化所有群聊的数据
//        CustomThreadPoolExecutor.execute(() -> {
//        });
        for (int i = 0; i < init.size(); i++) {
            ChatChannelHandlerPool.saveGroupUser(Long.parseLong((i + 1) + ""), init.get(i));
        }
        log.info("群聊数据初始化完成");

        log.info("11 {}", ChatChannelHandlerPool.getGroupAllUserId(1L));
        // 模拟加入群聊
        ChatChannelHandlerPool.saveGroupUser(1L, 66L);
        log.info("22 {}", ChatChannelHandlerPool.getGroupAllUserId(1L));

        // 模拟退出群聊
        ChatChannelHandlerPool.removeGroupUser(1L, 4L);
        log.info("33 {}", ChatChannelHandlerPool.getGroupAllUserId(1L));

        Map<String, Object> claims = new HashMap<>();
        claims.put("login_user_id", "111");
        String token = TokenUtils.createToken(claims);
        log.info("token = {}", token);

    }


}
