package com.Ink.auth.controller;

import com.Ink.auth.service.SysRoleService;
import com.Ink.auth.service.SysUserService;
import com.Ink.md5.MD5;
import com.Ink.model.system.SysRole;
import com.Ink.model.system.SysUser;
import com.Ink.result.Result;
import com.Ink.vo.system.SysUserQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "用户管理接口")
@RestController
@RequestMapping("/admin/system/sysUser")
public class SysUserController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysRoleService sysRoleService;

    @ApiOperation(value = "更新状态")
    @GetMapping("/updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        sysUserService.updateStatus(id, status);
        return Result.ok();
    }

    //用户条件分页查询
    @ApiOperation(value = "用户条件分页查询")//(姓名和时间范围)
    @GetMapping("/{page}/{limit}")
    public Result pageQueryRole(@PathVariable Long page,
                                @PathVariable Long limit,
                                SysUserQueryVo sysUserQueryVo) {
        //1 创建page对象 传递分页相关参数
        Page<SysUser> pageParam = new Page<>(page, limit);
        //2 封装条件 判断条件
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        String keyword = sysUserQueryVo.getKeyword();
        String createTimeBegin = sysUserQueryVo.getCreateTimeBegin();
        String createTimeEnd = sysUserQueryVo.getCreateTimeEnd();
        if (!StringUtils.isEmpty(keyword)) {
            //封装 like模糊 ge大于等于 le小于等于
            wrapper.and(i -> i.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getPhone, keyword)
                    .or().like(SysUser::getName,keyword));
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge(SysUser::getCreateTime, createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le(SysUser::getCreateTime, createTimeEnd);
        }
        //3 调用方法实现
        sysUserService.page(pageParam, wrapper);
        //4 添加角色信息
        for (SysUser user : pageParam.getRecords()) {
            Map<String, Object> roleDataByUserId = sysRoleService.findRoleDataByUserId(user.getId());
            List<SysRole> roleList = (List<SysRole>) roleDataByUserId.get("assignRoleList");
            user.setRoleList(roleList);
        }

        return Result.ok(pageParam);
    }

    /**
     * 获取用户
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "获取用户")
    @GetMapping("/get/{id}")
    public Result get(@PathVariable long id) {
        SysUser user = sysUserService.getById(id);
        return Result.ok(user);
    }

    /**
     * 更新用户
     *
     * @param sysUser
     * @return
     */
    @ApiOperation(value = "更新用户")
    @PutMapping("/update")
    public Result update(@RequestBody SysUser sysUser) {
        boolean is_success = sysUserService.updateById(sysUser);
        if (is_success) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    /**
     * 保存用户
     *
     * @param sysUser
     * @return
     */
    @ApiOperation(value = "保存用户")
    @PostMapping("/save")
    public Result save(@RequestBody SysUser sysUser) {
        //使用md5对密码进行加密
        String passwordMd5 = MD5.encrypt(sysUser.getPassword());
        sysUser.setPassword(passwordMd5);

        boolean is_success = sysUserService.save(sysUser);
        if (is_success) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    /**
     * 删除用户
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "删除用户")
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable long id) {
        boolean is_success = sysUserService.removeById(id);
        if (is_success) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }
}

