/**
 * Copyright (c) 2015 - Two Steps From Java Group.
 * All rights reserved.
 *
 * Created on 2017-02-16
 */
package io.iotp.web.config;

import io.iotp.core.config.BaseRedisConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;


/**
 * Redis数据库配置
 *
 * @author CD826
 * @since 1.0.0
 */
@Configuration
public class RedisConfig extends BaseRedisConfig {
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate(redisConnectionFactory);
        setSerializer(template);
        template.afterPropertiesSet();
        return template;
    }
}