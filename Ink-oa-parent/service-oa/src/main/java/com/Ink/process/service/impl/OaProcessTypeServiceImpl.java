package com.Ink.process.service.impl;

import com.Ink.model.process.ProcessTemplate;
import com.Ink.model.process.ProcessType;
import com.Ink.process.mapper.OaProcessTypeMapper;
import com.Ink.process.service.OaProcessTemplateService;
import com.Ink.process.service.OaProcessTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OaProcessTypeServiceImpl extends ServiceImpl<OaProcessTypeMapper, ProcessType> implements OaProcessTypeService {

    @Autowired
    private OaProcessTemplateService processTemplateService;

    //查询所有审批分类和其模板
    @Override
    public List<ProcessType> findProcessType() {
        List<ProcessType> processTypeList = baseMapper.selectList(null);
        for (ProcessType processType : processTypeList) {
            Long typeId=processType.getId();
            LambdaQueryWrapper<ProcessTemplate> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(ProcessTemplate::getProcessTypeId,typeId);
            List<ProcessTemplate> processTemplates = processTemplateService.list(lambdaQueryWrapper);
            processType.setProcessTemplateList(processTemplates);
        }
        return processTypeList;
    }
}
