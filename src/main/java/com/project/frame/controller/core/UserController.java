package com.project.frame.controller.core;

import com.project.frame.controller.common.BaseController;
import com.project.frame.model.core.User;
import com.project.frame.service.core.UserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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
    @RequiresPermissions({"user:list"})
    public Map<String, Object> selectList(User user) {
        user.setDelFlag(false);
        return getResult(userService.selectList(user));
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping(value = "/getById")
    @RequiresPermissions({"user:getUser"})
    public Map<String, Object> getById(Long userId) {
        User user = userService.getById(userId);
        return getResult(user);
    }
}
