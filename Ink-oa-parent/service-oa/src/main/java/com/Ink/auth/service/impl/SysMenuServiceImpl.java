package com.Ink.auth.service.impl;

import com.Ink.auth.mapper.SysMenuMapper;
import com.Ink.auth.service.SysMenuService;
import com.Ink.auth.service.SysRoleMenuService;
import com.Ink.auth.utils.MenuHelper;
import com.Ink.model.system.SysMenu;
import com.Ink.model.system.SysRoleMenu;
import com.Ink.vo.system.AssignMenuVo;
import com.Ink.vo.system.MetaVo;
import com.Ink.vo.system.RouterVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.Ink.exception.SpecialException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {
    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    //菜单列表
    @Override
    public List<SysMenu> findNodes() {
        List<SysMenu> sysMenuList = baseMapper.selectList(null);
        List<SysMenu> resultList = MenuHelper.buildTree(sysMenuList);
        return resultList;
    }

    //移除菜单
    @Override
    public void removeMenuById(Long id) {
        //判断当前菜单是否有下一层菜单
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId, id);
        Long count = Long.valueOf(baseMapper.selectCount(wrapper));
        //有下一层 不能删除
        if (count > 0) {
            throw new SpecialException(201, "菜单不能删除");
        }
        baseMapper.deleteById(id);
    }

    //获取角色菜单
    @Override
    public List<SysMenu> findMenuByRoleId(Long roleId) {
        //查询所有菜单
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysMenu::getStatus, 1);
        List<SysMenu> allSysMenuList = baseMapper.selectList(queryWrapper);
        //获取角色拥有的菜单id集
        LambdaQueryWrapper<SysRoleMenu> wrapperSysRM = new LambdaQueryWrapper<>();
        wrapperSysRM.eq(SysRoleMenu::getRoleId, roleId);
        List<SysRoleMenu> sysRoleMenus = sysRoleMenuService.list(wrapperSysRM);
        List<Long> MenuIdList = sysRoleMenus.stream().map(c -> c.getMenuId()).collect(Collectors.toList());
        //比较 获得菜单集
        allSysMenuList.stream().forEach(item -> {
            item.setSelect(MenuIdList.contains(item.getId()));
        });
        //返回规定格式菜单集
        List<SysMenu> sysMenuList = MenuHelper.buildTree(allSysMenuList);
        return sysMenuList;
    }

    //为角色分配菜单
    @Override
    public void doAssign(AssignMenuVo assignMenuVo) {
        //删除原来的菜单
        LambdaQueryWrapper<SysRoleMenu> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId,assignMenuVo.getRoleId());
        sysRoleMenuService.remove(wrapper);
        //从参数里获得角色新分配菜单id列表 添加进关系表
        List<Long> menuIdList = assignMenuVo.getMenuIdList();
        for (Long menuId:menuIdList){
            if(StringUtils.isEmpty(menuId)){
                continue;
            }
            SysRoleMenu sysRoleMenu=new SysRoleMenu();
            sysRoleMenu.setMenuId(menuId);
            sysRoleMenu.setRoleId(assignMenuVo.getRoleId());
            sysRoleMenuService.save(sysRoleMenu);
        }
    }
    // 根据 用户id 获取用户可以操作的菜单列表
    @Override
    public List<RouterVo> findUserMenuListByUserId(Long userId) {
        List<SysMenu> sysMenuList=null;
        //1 判断用户是不是管理员
        if(userId.longValue()==1) {
            //管理员->所有菜单
            LambdaQueryWrapper<SysMenu> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus,1);
            wrapper.orderByAsc(SysMenu::getSortValue);
            sysMenuList=baseMapper.selectList(wrapper);
        }else{
            //非管理员->根据userId查询(三表联查)
            sysMenuList=baseMapper.findUserMenuListByUserId(userId);
        }
        //2 把数据转换成框架要求的路由结构(菜单的一部分就是路由)
        List<SysMenu> menus=MenuHelper.buildTree(sysMenuList);
        List<RouterVo> routerVoList=this.buildRouter(menus);
        return routerVoList;
    }

    // 根据 用户id 获取用户可以操作的按钮列表
    @Override
    public List<String> findUserPermsByUserId(Long userId) {
        // 1、判断是否是管理员，如果是管理员，查询所有按钮列表
        List<SysMenu> sysMenusList = null;
        if (userId.longValue() == 1) {
            // 查询所有菜单列表
            LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysMenu::getStatus, 1);
            sysMenusList = baseMapper.selectList(queryWrapper);
        }else {
            // 2、如果不是管理员，根据userId查询可以操作按钮列表
            // 多表关联查询:sys_role、sys_role_menu、sys_menu
            sysMenusList = baseMapper.findUserMenuListByUserId(userId);
        }

        // 3、从查询出来的数据里面，获取可以操作按钮值的List集合，返回
        List<String> permsList = sysMenusList.stream()
                .filter(item -> item.getType() == 2)
                .map(item -> item.getPerms())
                .collect(Collectors.toList());
        return permsList;
    }

    //构建框架要求的路由结构(路由只包括菜单的第一二层，最底层不是路由)
    private List<RouterVo> buildRouter(List<SysMenu> menus) {
        //创建list集合 ,存取最终数据
        List<RouterVo> routers=new ArrayList<>();
        //menus遍历
        for(SysMenu menu:menus){
            //创建基本router
            RouterVo router=new RouterVo();
            router.setHidden(false);
            router.setAlwaysShow(false);
            router.setPath(getRouterPath(menu));
            router.setComponent(menu.getComponent());
            router.setMeta(new MetaVo(menu.getName(), menu.getIcon()));
            //下一层数据
            List<SysMenu> children=menu.getChildren();
            //加载隐藏路由 菜单层级为2同时Component非空就是隐藏路由
            if(menu.getType().intValue()==1){
                List<SysMenu> hiddenMenuList=children.stream().filter(item->
                        !StringUtils.isEmpty(item.getComponent()))
                        .collect(Collectors.toList());
                for (SysMenu hiddenMenu : hiddenMenuList) {
                    RouterVo hiddenRouter = new RouterVo();
                    hiddenRouter.setHidden(true);
                    hiddenRouter.setAlwaysShow(false);
                    hiddenRouter.setPath(getRouterPath(hiddenMenu));
                    hiddenRouter.setComponent(hiddenMenu.getComponent());
                    hiddenRouter.setMeta(new MetaVo(hiddenMenu.getName(), hiddenMenu.getIcon()));
                    routers.add(hiddenRouter);
                }
             //加载普通路由
            }else{
                if(!CollectionUtils.isEmpty(children)){
                    if(children.size()>0){
                        router.setAlwaysShow(true);
                    }
                    router.setChildren(buildRouter(children));
                }
            }
            routers.add(router);
        }
        return routers;
    }
    //获取路由地址
    public String getRouterPath(SysMenu menu) {
        String routerPath = "/" + menu.getPath();//第一层需要加“/"
        if (menu.getParentId().intValue() != 0) {
            routerPath = menu.getPath();
        }
        return routerPath;
    }
}
