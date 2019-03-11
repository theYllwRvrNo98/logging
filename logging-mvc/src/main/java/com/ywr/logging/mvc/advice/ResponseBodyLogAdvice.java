package com.ywr.logging.mvc.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Created by zhanglin on 2019-03-08
 */

@ControllerAdvice
public class ResponseBodyLogAdvice implements ResponseBodyAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseBodyLogAdvice.class);

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        LOGGER.info("[response body]:{}", body);
        return body;
    }
}
