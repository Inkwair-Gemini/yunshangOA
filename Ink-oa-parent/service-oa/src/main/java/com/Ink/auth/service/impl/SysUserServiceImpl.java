package com.Ink.auth.service.impl;
import com.Ink.auth.mapper.SysUserMapper;
import com.Ink.auth.service.SysUserService;
import com.Ink.custom.LoginUserInfoHelper;
import com.Ink.model.system.SysUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author Inkwair
 * @since 2023-05-03
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    //更新状态
    @Override
    public void updateStatus(Long id, Integer status) {
        //根据id查询用户
        SysUser sysUser=baseMapper.selectById(id);
        //设置状态值
        if (status == 0 || status == 1) {
            sysUser.setStatus(status);
        } else {
            log.error("数值不合法");
        }
        //更新
        baseMapper.updateById(sysUser);
    }
    //根据用户名查询
    @Override
    public SysUser getUserByUserName(String username) {
        LambdaQueryWrapper<SysUser> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername,username);
        SysUser sysUser = baseMapper.selectOne(wrapper);
        return sysUser;
    }

    @Override
    public Map<String, Object> getCurrentUser() {
        SysUser sysUser=baseMapper.selectById(LoginUserInfoHelper.getUserId());
        Map<String,Object> map =new HashMap<>();
        map.put("name",sysUser.getName());
        map.put("phone",sysUser.getPhone());
        return map;
    }
}
