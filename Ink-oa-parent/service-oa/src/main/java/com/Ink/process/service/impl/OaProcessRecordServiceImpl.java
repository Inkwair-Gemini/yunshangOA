package com.Ink.process.service.impl;

import com.Ink.auth.service.SysUserService;
import com.Ink.custom.LoginUserInfoHelper;
import com.Ink.model.process.ProcessRecord;
import com.Ink.model.system.SysUser;
import com.Ink.process.mapper.OaProcessRecordMapper;
import com.Ink.process.service.OaProcessRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OaProcessRecordServiceImpl extends ServiceImpl<OaProcessRecordMapper, ProcessRecord> implements OaProcessRecordService {
    @Autowired
    private SysUserService service;
    @Override
    public void record(Long processId, Integer status, String description) {
        Long userId= LoginUserInfoHelper.getUserId();
        SysUser sysUser = service.getById(userId);
        ProcessRecord processRecord = new ProcessRecord();
        processRecord.setProcessId(processId);
        processRecord.setStatus(status);
        processRecord.setDescription(description);
        processRecord.setOperateUser(sysUser.getName());
        processRecord.setOperateUserId(userId);
        baseMapper.insert(processRecord);
    }
}
