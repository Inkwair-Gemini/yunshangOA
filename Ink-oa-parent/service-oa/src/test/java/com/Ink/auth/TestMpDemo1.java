package com.Ink.auth;

import com.Ink.auth.mapper.SysRoleMapper;
import com.Ink.model.system.SysRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class TestMpDemo1 {
    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Test
    public void getAll(){
        List<SysRole> list=sysRoleMapper.selectList(null);//where isDelete=0
        System.out.println(list);
    }
    @Test
    public void testDeleteBatchIds(){
        sysRoleMapper.deleteBatchIds(Arrays.asList(1,2));
    }
    //条件查询
    @Test
    public void testQuery1(){
        //创建QueryWrapper对象，调用方法封装条件
        QueryWrapper<SysRole> wrapper=new QueryWrapper<>();
        wrapper.eq("role_name","总经理");
        List<SysRole> list=sysRoleMapper.selectList(wrapper);
        System.out.println(list);
    }
    //条件查询(lambda表达式)
    @Test
    public void testQuery2(){
        //创建QueryWrapper对象，调用方法封装条件
        LambdaQueryWrapper<SysRole> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleName,"总经理");
        List<SysRole> list=sysRoleMapper.selectList(wrapper);
        System.out.println(list);
    }
}
