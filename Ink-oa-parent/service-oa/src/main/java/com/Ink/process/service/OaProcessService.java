package com.Ink.process.service;
import com.Ink.model.process.Process;
import com.Ink.vo.process.ApprovalVo;
import com.Ink.vo.process.ProcessFormVo;
import com.Ink.vo.process.ProcessQueryVo;
import com.Ink.vo.process.ProcessVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface OaProcessService extends IService<Process> {

    IPage<ProcessVo> selectPage(Page<ProcessVo> pageInfo, ProcessQueryVo processQueryVo);
    void deployByZip(String processDefinitionPath);

    void startUp(ProcessFormVo processFormVo);

    IPage<ProcessVo> findPending(Page<Process> pageParam);

    Map<String, Object> show(Long id);

    void approve(ApprovalVo approvalVo);

    Object findProcessed(Page<Process> pageParam);

    IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam);
}
