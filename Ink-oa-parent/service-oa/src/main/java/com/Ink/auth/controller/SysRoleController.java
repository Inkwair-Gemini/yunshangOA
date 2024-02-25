package com.Ink.auth.controller;

import com.Ink.auth.service.SysRoleService;
import com.Ink.model.system.SysRole;
import com.Ink.result.Result;
import com.Ink.vo.system.AssignRoleVo;
import com.Ink.vo.system.SysRoleQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Api(tags = "角色管理接口")
@RestController
@RequestMapping("/admin/system/sysRole")
public class SysRoleController {
    @Autowired
    private SysRoleService sysRoleService;

    //1 查询所有角色 和 当前用户所属角色
    @ApiOperation(value = "根据用户获取角色数据")
    @GetMapping("/toAssign/{userId}")
    public Result toAssign(@PathVariable Long userId) {
        Map<String, Object> map = sysRoleService.findRoleDataByUserId(userId);
        return Result.ok(map);
    }

    //2 为用户分配角色
    @ApiOperation(value = "为用户分配角色")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestBody AssignRoleVo assignRoleVo) {
        sysRoleService.doAssign(assignRoleVo);
        return Result.ok();
    }

    @ApiOperation(value = "查询所有角色")
    @GetMapping("/findAll")
    public Result findAll() {
        List<SysRole> list = sysRoleService.list();
        return Result.ok(list);
    }

    // page：当前页 limit：每页显示记录数
    // SysRoLeQueryVo：条件对象
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation(value = "角色条件分页查询")
    @GetMapping("/{page}/{limit}")
    public Result pageQueryRole(@PathVariable Long page,
                                @PathVariable Long limit,
                                SysRoleQueryVo sysRoleQueryVo) {
        //1 创建page对象 传递分页相关参数
        Page<SysRole> pageParam = new Page<>(page, limit);
        //2 封装条件 判断条件
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        String roleName = sysRoleQueryVo.getRoleName();
        if (!StringUtils.isEmpty(roleName)) {
            //封装
            wrapper.like(SysRole::getRoleName, roleName);
        }
        //3 调用方法实现
        sysRoleService.page(pageParam, wrapper);//结果装入pageParam
        return Result.ok(pageParam);
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.add')")
    @ApiOperation(value = "添加角色")
    @PostMapping("/save")
    public Result save(@RequestBody SysRole sysRole) {
        return sysRoleService.save(sysRole) ? Result.ok() : Result.fail();
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation(value = "通过Id查询角色")
    @GetMapping("/get/{id}")
    public Result get(@PathVariable Long id) {
        SysRole sysRole = sysRoleService.getById(id);
        return Result.ok(sysRole);
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.update')")
    @ApiOperation(value = "修改角色")
    @PutMapping("/update")
    public Result update(@RequestBody SysRole sysRole) {
        return sysRoleService.updateById(sysRole) ? Result.ok() : Result.fail();
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation(value = "删除角色")
    @DeleteMapping("/delete/{id}")
    public Result remove(@PathVariable Long id) {
        return sysRoleService.removeById(id) ? Result.ok() : Result.fail();
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation(value = "批量删除角色")
    @DeleteMapping("/ids")
    public Result batchRemove(@RequestBody List<Long> alist) {
        return sysRoleService.removeByIds(alist) ? Result.ok() : Result.fail();
    }
}
