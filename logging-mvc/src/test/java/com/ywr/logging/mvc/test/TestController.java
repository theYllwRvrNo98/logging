package com.ywr.logging.mvc.test;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * Created by zhanglin on 2019-03-11
 */
@RestController
@RequestMapping("test")
public class TestController {

    @RequestMapping("greeting")
    public Map<String, Object> greeting(@RequestParam(value = "name") String name, @RequestBody Map<String, Object> form) {
        return Collections.singletonMap("resp", "this is response body");
    }

}
