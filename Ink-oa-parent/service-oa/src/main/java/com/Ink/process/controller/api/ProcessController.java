package com.Ink.process.controller.api;
import com.Ink.auth.service.SysUserService;
import com.Ink.model.process.Process;
import com.Ink.model.process.ProcessTemplate;
import com.Ink.model.process.ProcessType;
import com.Ink.process.service.OaProcessService;
import com.Ink.process.service.OaProcessTemplateService;
import com.Ink.process.service.OaProcessTypeService;
import com.Ink.result.Result;
import com.Ink.vo.process.ApprovalVo;
import com.Ink.vo.process.ProcessFormVo;
import com.Ink.vo.process.ProcessVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/process")
@CrossOrigin//跨域
public class ProcessController {
    @Autowired
    private OaProcessTypeService processTypeService;
    @Autowired
    private OaProcessTemplateService processTemplateService;
    @Autowired
    private OaProcessService processService;
    @Autowired
    private SysUserService sysUserService;

    @ApiOperation(value = "查询所有审批分类")
    @GetMapping("findProcessType")
    public Result findProcessType(){
        List<ProcessType> list=processTypeService.findProcessType();
        return Result.ok(list);
    }

    @ApiOperation(value = "查询审批详情信息")
    @GetMapping("show/{id}")
    public Result show(@PathVariable Long id){
        Map<String,Object> map=processService.show(id);
        return Result.ok(map);
    }

    @ApiOperation(value = "获取审批模板页面")
    @GetMapping("getProcessTemplate/{processTemplateId}")
    public Result get(@PathVariable Long processTemplateId){
        ProcessTemplate template = processTemplateService.getById(processTemplateId);
        return Result.ok(template);
    }

    @ApiOperation("启动流程")
    @PostMapping("/startUp")
    public Result startUp(@RequestBody ProcessFormVo processFormVo){
        processService.startUp(processFormVo);
        return Result.ok();
    }

    @ApiOperation(value = "待处理")
    @GetMapping("/findPending/{page}/{limit}")
    public Result findPending(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit) {
        Page<Process> pageParam = new Page<>(page, limit);
        IPage<ProcessVo> voPage=processService.findPending(pageParam);
        return Result.ok(voPage);
    }

    @ApiOperation(value = "已处理")
    @GetMapping("/findProcessed/{page}/{limit}")
    public Result findProcessed(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit) {
        Page<Process> pageParam = new Page<>(page, limit);
        return Result.ok(processService.findProcessed(pageParam));
    }

    @ApiOperation(value = "已发起")
    @GetMapping("/findStarted/{page}/{limit}")
    public Result findStarted(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit) {
        Page<ProcessVo> pageParam = new Page<>(page, limit);
        IPage<ProcessVo> pageModel = processService.findStarted(pageParam);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "审批")
    @PostMapping("approve")
    public Result approve(@RequestBody ApprovalVo approvalVo){
        processService.approve(approvalVo);
        return Result.ok();
    }
    @ApiOperation(value = "获取当前用户信息")
    @GetMapping("getCurrentUser")
    public Result getCurrentUser(){
        Map<String,Object> map=sysUserService.getCurrentUser();
        return Result.ok(map);
    }
}