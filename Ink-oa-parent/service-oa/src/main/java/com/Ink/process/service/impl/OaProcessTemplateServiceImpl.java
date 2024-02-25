package com.Ink.process.service.impl;

import com.Ink.model.process.ProcessTemplate;
import com.Ink.model.process.ProcessType;
import com.Ink.process.mapper.OaProcessTemplateMapper;
import com.Ink.process.service.OaProcessService;
import com.Ink.process.service.OaProcessTemplateService;
import com.Ink.process.service.OaProcessTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;

@Service
public class OaProcessTemplateServiceImpl extends ServiceImpl<OaProcessTemplateMapper, ProcessTemplate> implements OaProcessTemplateService {
    @Autowired
    private OaProcessTypeService processTypeService;
    @Autowired
    private OaProcessService processService;

    //查询出来的数据是类型id，把id转换成类型名称
    @Override
    public IPage<ProcessTemplate> selectPageProcessTemplate(Page<ProcessTemplate> pageInfo) {
        Page<ProcessTemplate> processTemplatePage = baseMapper.selectPage(pageInfo, null);
        List<ProcessTemplate> records = processTemplatePage.getRecords();
        for (ProcessTemplate record : records) {
            Long processTypeId = record.getProcessTypeId();
            LambdaQueryWrapper<ProcessType> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(ProcessType::getId,processTypeId);
            ProcessType processType = processTypeService.getOne(wrapper);
            if(processType==null){
                continue;
            }
            record.setProcessTypeName(processType.getName());
        }
        return processTemplatePage;
    }
    //修改发布状态
    //部署流程
    @Override
    public void publish(Long id) {
        ProcessTemplate processTemplate = baseMapper.selectById(id);
        processTemplate.setStatus(1);
        baseMapper.updateById(processTemplate);
        //todo 部署流程
        if(!StringUtils.isEmpty(processTemplate.getProcessDefinitionPath())){
            processService.deployByZip(processTemplate.getProcessDefinitionPath());
        }
    }
}
