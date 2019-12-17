package com.project.frame.controller.core;

import com.alibaba.fastjson.JSONObject;
import com.project.frame.controller.common.BaseController;
import com.project.frame.model.core.User;
import com.project.frame.service.core.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 用户信息控制器
 *
 * @author mxy
 * @date 2019/12/15
 */
@RestController
@RequestMapping(value = "/api/v1/auth/user")
public class UserController extends BaseController {
    private static final long serialVersionUID = 8317181657760221914L;

    @Resource(name = "userServiceImpl")
    private UserService userService;

    /**
     * 获取用户集合
     *
     * @param user 用户对象
     * @return 操作结果
     */
    @PostMapping(value = "/selectList")
    public Map<String, Object> selectList(User user) {
        return getResult(userService.selectList(user));
    }
}
