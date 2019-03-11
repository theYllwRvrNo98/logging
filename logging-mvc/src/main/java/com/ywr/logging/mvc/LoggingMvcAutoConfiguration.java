package com.ywr.logging.mvc;

import com.ywr.logging.mvc.filter.URILogFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by zhanglin on 2019-03-08
 */
@Configuration
@ComponentScan("com.ywr.logging.mvc")
public class LoggingMvcAutoConfiguration {

    @Bean
    public FilterRegistrationBean uriLoggingFilterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new URILogFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
