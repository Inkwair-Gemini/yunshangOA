package com.Ink.process.service;

import com.Ink.model.process.ProcessType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface OaProcessTypeService extends IService<ProcessType> {

    List<ProcessType> findProcessType();
}
