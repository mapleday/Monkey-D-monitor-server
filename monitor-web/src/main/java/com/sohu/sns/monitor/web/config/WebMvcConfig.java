package com.sohu.sns.monitor.web.config;

import com.google.common.collect.Lists;
import com.sohu.sns.monitor.web.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by zhangnan on 2016/12/16.
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {
    @Autowired
    private AuthInterceptor authInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        authInterceptor.setWhiteLists(Lists.newArrayList("/static/**","/error"));
        registry.addInterceptor(authInterceptor).addPathPatterns("/**").addPathPatterns("/**");//要拦截的请求
    }
}
