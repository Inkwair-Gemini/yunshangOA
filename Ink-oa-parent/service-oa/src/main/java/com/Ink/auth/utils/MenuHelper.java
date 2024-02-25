package com.Ink.auth.utils;

import com.Ink.model.system.SysMenu;
import java.util.ArrayList;
import java.util.List;

public class MenuHelper {
    //递归构建菜单
    public static List<SysMenu> buildTree(List<SysMenu> sysMenuList) {
        //存放最终数据
        List<SysMenu> trees=new ArrayList<>();
        //把所有的菜单数据遍历
        for(SysMenu sysMenu:sysMenuList){
            //递归入口
            if(sysMenu.getParentId().longValue()==0){
                trees.add(getChildren(sysMenu,sysMenuList));//将sysMenu添加，同时为sysMenu添加儿子
            }
        }
        return trees;
    }

    private static SysMenu getChildren(SysMenu sysMenu, List<SysMenu> sysMenuList) {
        sysMenu.setChildren(new ArrayList<>());
        for(SysMenu it:sysMenuList){
            //如果有儿子
            if(sysMenu.getId().longValue()== it.getParentId().longValue()){
                if (sysMenu.getChildren() == null){
                    sysMenu.setChildren(new ArrayList<>());
                }
                sysMenu.getChildren().add(getChildren(it,sysMenuList));
            }
        }
        return sysMenu;
    }
}
