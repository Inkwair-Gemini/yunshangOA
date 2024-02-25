package com.Ink.auth.service;
import com.Ink.model.system.SysMenu;
import com.Ink.vo.system.AssignMenuVo;
import com.Ink.vo.system.RouterVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author Inkwair
 * @since 2023-05-03
 */
public interface SysMenuService extends IService<SysMenu> {

    List<SysMenu> findNodes();

    void removeMenuById(Long id);

    List<SysMenu> findMenuByRoleId(Long roleId);

    void doAssign(AssignMenuVo assignMenuVo);

    List<String> findUserPermsByUserId(Long userId);

    // 根据 用户id 获取用户可以操作的菜单列表
    List<RouterVo> findUserMenuListByUserId(Long userId);
}
