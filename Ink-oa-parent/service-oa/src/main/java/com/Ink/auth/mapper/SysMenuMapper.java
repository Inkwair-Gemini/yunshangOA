package com.Ink.auth.mapper;

import com.Ink.model.system.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 菜单表 Mapper 接口
 * </p>
 *
 * @author Inkwair
 * @since 2023-05-03
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    // 根据 用户id 获取用户可以操作的菜单列表
    @Select("""
        select distinct *
        from sys_menu m
        inner join sys_role_menu rm on rm.menu_id=m.id
        inner join sys_user_role ur on ur.role_id=rm.role_id
        where ur.user_id=#{userId}
            and m.status=1
            and rm.is_deleted=0
            and ur.is_deleted=0
            and m.is_deleted=0
            """)
    List<SysMenu> findUserMenuListByUserId(@Param("userId") Long userId);
}
