package com.project.frame.controller;

import com.project.frame.commons.controller.BaseController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 测试控制器
 *
 * @author mxy
 * @date 2019/12/15
 */
@RestController
@RequestMapping(value = "/api/vi/anon/test")
public class TestController extends BaseController {
    private static final long serialVersionUID = 5199396357453768833L;

    /**
     * 测试接口
     *
     * @param test 测试参数
     * @return 操作结果
     */
    @GetMapping(value = "/")
    public Map<String, Object> test(@RequestParam(name = "test", required = false) String test) {
        logger.info("\r\n ********* 测试接口被调用，传递内容：{}", test);
        return getResult(test);
    }
}
