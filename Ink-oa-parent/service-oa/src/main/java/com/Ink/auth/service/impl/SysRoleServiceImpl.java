package com.Ink.auth.service.impl;

import com.Ink.auth.mapper.SysRoleMapper;
import com.Ink.auth.service.SysRoleService;
import com.Ink.auth.service.SysUserRoleService;
import com.Ink.model.system.SysRole;
import com.Ink.model.system.SysUserRole;
import com.Ink.vo.system.AssignRoleVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//mp封装的service层,可以extends ServiceImpl来完成注入mapper
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper,SysRole> implements SysRoleService {
    @Autowired
    private SysUserRoleService sysUserRoleService;
    // 查询所有角色 和 当前用户所属角色
    @Override
    public Map<String, Object> findRoleDataByUserId(Long userId) {
        //1 查询所有角色，返回list集合，返回
        List<SysRole> allRoleList=
                baseMapper.selectList(null);
        //2 根据userid查询 角色用户关系表，查询userid对应所有角色id
        LambdaQueryWrapper<SysUserRole> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId,userId);
        List<SysUserRole> existUserRoleList=sysUserRoleService.list(wrapper);

        List<Long> existRoleIdList =
                existUserRoleList.stream().map(c -> c.getRoleId()).collect(Collectors.toList());
        //3 根据查询所有角色id，找到对应角色信息
        //根据角色id到所有的角色的list集合进行比较
        List<SysRole> assignRoleList=new ArrayList<>();
        for(SysRole sysRole:allRoleList){
            if(existRoleIdList.contains(sysRole.getId())){
                assignRoleList.add(sysRole);
            }
        }
        //4 把得到两部分数据封装map集合，返回
        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("assignRoleList", assignRoleList);
        roleMap.put("allRolesList", allRoleList);
        return roleMap;
    }
    //2 为用户分配角色
    @Override
    public void doAssign(AssignRoleVo assignRoleVo) {
        //把用户之前分配角色数据删除，用户角色关系表里面，根据userid删除
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId,assignRoleVo.getUserId());
        sysUserRoleService.remove(wrapper);
        //重新分配
        List<Long> roleIdList=assignRoleVo.getRoleIdList();
        for(Long roleId:roleIdList){
            if(StringUtils.isEmpty(roleId)){
                continue;
            }
            SysUserRole sysUserRole=new SysUserRole();
            sysUserRole.setUserId(assignRoleVo.getUserId());
            sysUserRole.setRoleId(roleId);
            sysUserRoleService.save(sysUserRole);
        }
    }
}
