package com.spy.server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationStartupLogger implements CommandLineRunner {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${server.port}")
    private String serverPort;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public void run(String... args) {
        log.info("应用启动完成：应用名称={}，当前环境={}，端口={}，上下文路径={}", applicationName, activeProfile, serverPort, contextPath);
    }
}
