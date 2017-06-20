/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-10-10
 */
package io.iotp.web.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * 数据库配置, 这里使用HikariCP连接池, 该连接池的性能是非常好的. 有关该连接池的详细信息可以参考这里
 * https://github.com/brettwooldridge/HikariCP .
 *
 * @author CD826
 * @since 1.0.0
 */
@Configuration
@EnableAutoConfiguration
@EnableJpaRepositories({"com.xiaochenghudong.**.repository", "io.iotp.**.repository", "org.twostepsfromjava.**.repository"})
@EntityScan({"io.iotp.**.entity", "com.xiaochenghudong.**.entity", "org.twostepsfromjava.**.entity"})
public class RepositoryConfig {
    @Resource
    private Environment env;

    /**
     * 使用HikariCP创建数据源
     *
     * @return
     * @throws Exception
     */
    @Bean(name = "dataSource", destroyMethod = "close")
    public DataSource dataSource() throws Exception {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setUsername(env.getRequiredProperty("spring.datasource.username"));
        dataSource.setPassword(env.getRequiredProperty("spring.datasource.password"));
        dataSource.setJdbcUrl(env.getRequiredProperty("spring.datasource.url"));

        // XXX: 这里不用设置, 因为HikariCP会根据所设置的JdbcUrl自动判断使用那个.
        // 只需要保证你的项目中有相应包即可.
        dataSource.setDriverClassName(env.getRequiredProperty("spring.datasource.driverClassName"));

        return dataSource;
    }
}
