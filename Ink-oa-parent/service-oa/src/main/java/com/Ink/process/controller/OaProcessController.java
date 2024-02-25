package com.Ink.process.controller;
import com.Ink.process.service.OaProcessService;
import com.Ink.result.Result;
import com.Ink.vo.process.ProcessQueryVo;
import com.Ink.vo.process.ProcessVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/admin/process")
public class OaProcessController {
    @Autowired
    private OaProcessService processService;

    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{limit}")
    public Result index(@PathVariable Long page,
                        @PathVariable Long limit,
                        ProcessQueryVo processQueryVo) {
        Page<ProcessVo> pageInfo = new Page<>(page, limit);
        IPage<ProcessVo> pageModel =
                processService.selectPage(pageInfo,processQueryVo);
        return Result.ok(pageModel);
    }
}

