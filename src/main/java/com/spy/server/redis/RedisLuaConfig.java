package com.spy.server.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisLuaConfig {

    @Bean
    public DefaultRedisScript<Long> secKillScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/secKill.lua"));
        script.setResultType(Long.class);
        return script;
    }
}
