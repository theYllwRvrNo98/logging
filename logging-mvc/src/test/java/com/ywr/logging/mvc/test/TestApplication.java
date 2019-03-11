package com.ywr.logging.mvc.test;

import com.ywr.logging.mvc.LoggingMvcAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Created by zhanglin on 2019-03-11
 */
@SpringBootApplication
@Import({LoggingMvcAutoConfiguration.class, TestController.class})
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
