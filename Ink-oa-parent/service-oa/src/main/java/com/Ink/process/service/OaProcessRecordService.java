package com.Ink.process.service;


import com.Ink.model.process.ProcessRecord;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OaProcessRecordService extends IService<ProcessRecord> {
    void record(Long processId,Integer status,String description);
}
