package com.Ink.wechat.controller;
import com.Ink.model.wechat.Menu;
import com.Ink.result.Result;
import com.Ink.wechat.service.WechatMenuService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/admin/wechat/menu")
public class WechatMenuController {
    @Autowired
    private WechatMenuService wechatMenuService;

    @PreAuthorize("hasAuthority('bnt.menu.list')")
    @ApiOperation(value = "获取全部菜单")
    @GetMapping("findMenuInfo")
    public Result findMenuInfo() {
        return Result.ok(wechatMenuService.findWechatMenuInfo());
    }

    @PreAuthorize("hasAuthority('bnt.menu.syncMenu')")
    @ApiOperation(value = "同步菜单")
    @GetMapping("syncMenu")
    public Result createMenu() {
        wechatMenuService.syncWechatMenu();
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.menu.removeMenu')")
    @ApiOperation(value = "删除菜单")
    @DeleteMapping("removeMenu")
    public Result removeMenu() {
        wechatMenuService.removeMenu();
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.menu.list')")
    @ApiOperation(value = "获取")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        Menu menu = wechatMenuService.getById(id);
        return Result.ok(menu);
    }

    @PreAuthorize("hasAuthority('bnt.menu.add')")
    @ApiOperation(value = "新增")
    @PostMapping("save")
    public Result save(@RequestBody Menu menu) {
        wechatMenuService.save(menu);
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.menu.update')")
    @ApiOperation(value = "修改")
    @PutMapping("update")
    public Result updateById(@RequestBody Menu menu) {
        wechatMenuService.updateById(menu);
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.menu.remove')")
    @ApiOperation(value = "删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        wechatMenuService.removeById(id);
        return Result.ok();
    }
}

