package com.Ink.custom;

import com.Ink.model.system.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
//自定义小组件 :UserDetails自定义 承载sysUser
/*  我们需要写一个bean实现userDetails接口 重写存储权限 获取状态 获取用户名密码等方法，这个bean里的数据就是我们的初始bean
    但实现这个接口的方法比较麻烦，可以实现user接口，这个接口已经实现了userDetails接口*/
public class CustomUser extends User {
    /**
     * 我们自己的用户实体对象，要调取用户信息时直接获取这个实体对象.
     */
    private SysUser sysUser;

    public CustomUser(SysUser sysUser, Collection<? extends GrantedAuthority> authorities) {
        super(sysUser.getUsername(), sysUser.getPassword(), authorities);
        this.sysUser = sysUser;
    }

    public SysUser getSysUser() {
        return sysUser;
    }

    public void setSysUser(SysUser sysUser) {
        this.sysUser = sysUser;
    }
}
