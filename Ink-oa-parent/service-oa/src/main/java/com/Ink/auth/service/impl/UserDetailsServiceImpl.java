package com.Ink.auth.service.impl;

import com.Ink.auth.service.SysMenuService;
import com.Ink.auth.service.SysUserService;
import com.Ink.custom.CustomUser;
import com.Ink.custom.UserDetailsService;
import com.Ink.model.system.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

//自定义小组件 :根据用户名查询数据库
/*1.根据账号去查数据库
        编写service实现userDetailsService
        service里重写loadUserByUsername方法 查询数据库 返回userDetails对象，实际是实现userDetails接口的自定义bean*/
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysMenuService sysMenuService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查询
        SysUser sysUser = sysUserService.getUserByUserName(username);
        if(null == sysUser) {
            throw new UsernameNotFoundException("用户名不存在！");
        }

        if(sysUser.getStatus().intValue() == 0) {
            throw new RuntimeException("账号已停用");
        }
        // 根据 user_id 查询用户操作权限数据
        List<String> userPermsList= sysMenuService.findUserPermsByUserId(sysUser.getId());
        // 创建list集合，封装最终权限数据
        List<SimpleGrantedAuthority> authorityList=new ArrayList<>();
        // 遍历 authList
        for (String perm :userPermsList) {
            authorityList.add(new SimpleGrantedAuthority(perm.trim()));
        }
        return new CustomUser(sysUser,authorityList);
    }
}