package com.Ink.auth.controller;
import com.Ink.auth.service.SysMenuService;
import com.Ink.auth.service.SysUserService;
import com.Ink.exception.SpecialException;
import com.Ink.jwt.JwtHelper;
import com.Ink.md5.MD5;
import com.Ink.model.system.SysUser;
import com.Ink.result.Result;
import com.Ink.vo.system.LoginVo;
import com.Ink.vo.system.RouterVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "后台登录管理")
@RestController
@RequestMapping("/admin/system/index")
public class IndexController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysMenuService sysMenuService;

    //login
    @PostMapping("/login")
    public Result login(@RequestBody LoginVo loginVo) {
        //1 获取输入用户名密码
        //2 根据用户名查询数据库
        String username = loginVo.getUsername();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser sysUser = sysUserService.getOne(wrapper);

        //3 用户信息是否存在
        if (sysUser == null) {
            throw new SpecialException(201, "用户不存在!");
        }
        //4 判断密码
        String db_password = sysUser.getPassword();
        String input_password = MD5.encrypt(loginVo.getPassword());
        if (!db_password.equals(input_password)) {
            throw new SpecialException(201, "密码错误!");
        }

        //5 判断用户是否被禁用
        if (sysUser.getStatus().intValue() == 0) {
            throw new SpecialException(201, "用户已经被禁用!");
        }
        //6 使用jwt根据用户id和用户名生成token
        String adminToken = JwtHelper.createToken(sysUser.getId(), sysUser.getUsername());
        //7 返回
        Map<String, Object> map = new HashMap<>();
        map.put("token", adminToken);
        return Result.ok(map);
    }

    //info
    @GetMapping("/info")
    public Result info(HttpServletRequest request) {

        // 1、从请求头获取用户信息（获取请求头的 token 字符串）
        String token = request.getHeader("token");

        // 2、从 token 字符串中获取 用户id 或者 用户名称
        Long userId = JwtHelper.getUserId(token);

        // 3、根据 用户id 查询数据库， 获取用户信息
        SysUser sysUser = sysUserService.getById(userId);

        // 4、根据 用户id 获取用户可以操作的菜单列表
        // 查询数据库动态构建路由结构，进行显示
        List<RouterVo> routerList = sysMenuService.findUserMenuListByUserId(userId);

        // 5、根据 用户id 获取用户可以操作的按钮列表
        List<String> permsList = sysMenuService.findUserPermsByUserId(userId);

        // 6、返回相应的数据
        Map<String, Object> map = new HashMap<>();
        map.put("roles", "[admin]");
        map.put("name", sysUser.getName());
        map.put("avatar", "https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
        // 返回用户可以操作的菜单
        map.put("routers", routerList);
        // 返回用户可以操作的按钮
        map.put("buttons", permsList);

        return Result.ok(map);
    }

    //logout
    @PostMapping("logout")
    public Result logout(){
        return Result.ok();
    }
}
