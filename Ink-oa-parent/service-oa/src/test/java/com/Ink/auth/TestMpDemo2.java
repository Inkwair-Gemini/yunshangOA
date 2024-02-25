package com.Ink.auth;

import com.Ink.auth.mapper.SysRoleMapper;
import com.Ink.auth.service.SysRoleService;
import com.Ink.model.system.SysRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class TestMpDemo2 {
    @Autowired
    private SysRoleService service;
    @Test
    public void getAll(){
        List<SysRole> list=service.list();//where isDelete=0
        System.out.println(list);
    }
}
