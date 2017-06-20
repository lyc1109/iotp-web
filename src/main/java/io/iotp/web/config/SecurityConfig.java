/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-10-10
 */
package io.iotp.web.config;

import io.iotp.web.service.UserDetailsServiceImpl;
import io.springbootstrap.core.security.LoginFailureHandlerRestSupport;
import io.springbootstrap.core.security.LoginSuccessHandlerRestSupport;
import io.springbootstrap.core.security.LoginUrlAuthenticationEntryPointRestSupport;
import io.springbootstrap.core.security.LogoutSuccessHandlerRestSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsUtils;

import javax.annotation.Resource;


/**
 * Spring Security配置信息
 *
 * @author CD826
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    public static final String LOGIN_URL                = "/login";
    public static final String HOME_URL                 = "/";
    public static final String LOGOUT_URL               = "/logout";
    public static final String ACCESS_DENIED_URL        = "/accessDenied";

    @Resource
    private UserDetailsServiceImpl userDetailsService;

    @Value("${app.security.rememberMe.cookieKey}")
    private String rememberMeCookieKey;
    @Value("${app.security.rememberMe.tokenValiditySeconds}")
    private int rememberTokenValiditySeconds;
    @Value("${app.security.rememberMe.alwaysRemember}")
    private boolean alwaysRememberMe = true;
    @Value("${app.security.rememberMe.useSecureCookie}")
    private boolean useSecureCookie = true;

    /**
     * 白名单配置, 指定哪些资源不需要进行权限拦截
     *
     * @param web WebSecurity
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/dist/**/*", "/iotp-assets/**/*");
    }

    /**
     * http拦截基本配置
     *
     * @param http HttpSecurity
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers()
				/* Allow embedding in iframe if bookmarklet script, otherwise add DENY X-Frame-Options for all other request */
                .frameOptions().sameOrigin()

                .and().authorizeRequests()
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                .antMatchers(
                        LOGIN_URL,
                        "/demos/**",
                        "/helpCenter/**",
                        "/m/demos/**",
                        "/sys/**/*",
                        "/iot/**",
                        "/api/**",
                        "/user/password/reset",
                        "/download/apk/**",
                        "/shop/product/authCode/qrcode/predownload",
                        "/shop/partner/join/*")
                .permitAll()
                // 其它地址都需要用户验证后才可以
                .anyRequest().authenticated()

                // 登录
                .and().formLogin()
                .loginPage(LOGIN_URL)
                .permitAll()
                .defaultSuccessUrl(HOME_URL, true)
                .successHandler(new LoginSuccessHandlerRestSupport())
                .failureHandler(new LoginFailureHandlerRestSupport(LOGIN_URL + "?error"))

                // 登出
                .and().logout()
                .logoutUrl(LOGOUT_URL)
                .deleteCookies("JSESSIONID", rememberMeCookieKey)
                .logoutSuccessHandler(new LogoutSuccessHandlerRestSupport(LOGIN_URL + "?logout"))

                // 异常处理
                .and()
                .csrf().disable()
                .exceptionHandling().accessDeniedPage(ACCESS_DENIED_URL)
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPointRestSupport(LOGIN_URL));

        // session相关配置
        // .and().sessionManagement().session
        // .and().sessionManagement().sessionFixation().none()
        // .sessionCreationPolicy(SessionCreationPolicy.NEVER)
        // .maximumSessions(1).expiredUrl("/expired")
        // .maxSessionsPreventsLogin(true)
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(this.userDetailsService).passwordEncoder(this.passwordEncoder());
    }

    /**
     * 用户密码加密所使用的加密算法
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
    // 用户成功退出后的处理器
    @Bean
    public LogoutSuccessHandler getUserLogoutSuccessHandler(){
        return new UserLogoutSuccessHandler();
    }
    */

    /*public static void main(String[] args){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("888888"));
    }*/
}
