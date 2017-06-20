/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-10-10
 */
package io.iotp.web.config;

import io.iotp.core.config.CacheConfig;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;


/**
 * 基于Redis的缓存服务配置
 *
 * @author CD826
 * @since 1.0.0
 */
@Configuration
public class RedisCacheConfig extends CacheConfig {
    @Bean
    public KeyGenerator cacheKeyGenerator(){
        return this.getCacheKeyGenerator();
    }

    @Bean
    public CacheManager cacheManager(RedisTemplate redisTemplate) {
        return this.getCacheManager(redisTemplate);
    }
}
