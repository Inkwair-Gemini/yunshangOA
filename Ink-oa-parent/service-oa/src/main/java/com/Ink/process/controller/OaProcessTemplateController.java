package com.Ink.process.controller;


import com.Ink.model.process.ProcessTemplate;
import com.Ink.model.process.ProcessType;
import com.Ink.process.service.OaProcessService;
import com.Ink.process.service.OaProcessTemplateService;
import com.Ink.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 审批模板 前端控制器
 * </p>
 *
 * @author Inkwair
 * @since 2023-05-13
 */
@Api(value = "审批模板管理", tags = "审批模板管理")
@RestController
@RequestMapping(value = "/admin/process/processTemplate")
public class OaProcessTemplateController {
    @Autowired
    private OaProcessTemplateService processTemplateService;
    @Autowired
    private OaProcessService processService;

    @ApiOperation(value = "上传流程定义")
    @PostMapping("/uploadProcessDefinition")
    public Result uploadProcessDefinition(MultipartFile file) throws FileNotFoundException {
        //获取classes目录位置
        String path=new File(ResourceUtils.getURL("classpath:").getPath()).getAbsolutePath();
        //设置上传文件夹
        File tempFile=new File(path+"/processes/");
        if(!tempFile.exists()){
            tempFile.mkdirs();//创建此抽象路径名指定的目录，包括所有必需但不存在的父目录
        }
        //创建空文件，实现文件写入
        String filename=file.getOriginalFilename();
        File zipFile=new File(path+"/processes/"+filename);
        //保存文件
        try{
            file.transferTo(zipFile);
        }catch (IOException e){
            return Result.fail();
        }

        Map<String,Object> map=new HashMap<>();
        //根据上传地址 后续部署流程定义，文件名称为流程定义的默认key
        map.put("processDefinitionPath","processes/"+filename);
        map.put("processDefinitionKey",filename.substring(0, filename.lastIndexOf(".")));
        return Result.ok(map);
    }

    @ApiOperation(value = "获取分页审批模板数据")
    @GetMapping("{page}/{limit}")
    public Result index(@PathVariable Long page, @PathVariable Long limit) {
        Page<ProcessTemplate> pageInfo = new Page<>(page, limit);
        IPage<ProcessTemplate> pageModel =
               processTemplateService.selectPageProcessTemplate(pageInfo);
        return Result.ok(pageModel);
    }

    //部署流程定义（发布）
    @ApiOperation(value = "发布")
    @GetMapping("/publish/{id}")
    public Result publish(@PathVariable Long id){
        //修改发布状态 1：已发布
        processTemplateService.publish(id);
        return Result.ok();
        //部署流程定义

    }

    @PreAuthorize("hasAuthority('bnt.processTemplate.list')")
    @ApiOperation(value = "获取")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        ProcessTemplate processTemplate = processTemplateService.getById(id);
        return Result.ok(processTemplate);
    }

    @PreAuthorize("hasAuthority('bnt.processTemplate.templateSet')")
    @ApiOperation(value = "新增")
    @PostMapping("save")
    public Result save(@RequestBody ProcessTemplate processTemplate) {
        processTemplateService.save(processTemplate);
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.processTemplate.templateSet')")
    @ApiOperation(value = "修改")
    @PutMapping("update")
    public Result updateById(@RequestBody ProcessTemplate processTemplate) {
        processTemplateService.updateById(processTemplate);
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.processTemplate.remove')")
    @ApiOperation(value = "删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        processTemplateService.removeById(id);
        return Result.ok();
    }
}

