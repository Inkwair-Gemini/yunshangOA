package com.Ink.process.controller;
import com.Ink.model.process.ProcessType;
import com.Ink.process.service.OaProcessTypeService;
import com.Ink.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
/**
 * <p>
 * 审批类型 前端控制器
 * </p>
 *
 * @author Inkwair
 * @since 2023-05-13
 */
@Api(value = "审批类型", tags = "审批类型")
@RestController
@RequestMapping("/admin/process/processType")
public class OaProcessTypeController {
    @Autowired
    private OaProcessTypeService processTypeService;

    @ApiOperation(value = "获取全部审批分类")
    @GetMapping("findAll")
    public Result findAll(){
        return Result.ok(processTypeService.list());
    }

    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{pageSize}")
    public Result index(@PathVariable Long page, @PathVariable Long pageSize) {
        Page<ProcessType> pageInfo = new Page<>(page, pageSize);
        IPage<ProcessType> pageModel = processTypeService.page(pageInfo);

        return Result.ok(pageModel);
    }

    @PreAuthorize("hasAuthority('bnt.processType.list')")
    @ApiOperation(value = "获取")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        ProcessType processType = processTypeService.getById(id);
        return Result.ok(processType);
    }

    @PreAuthorize("hasAuthority('bnt.processType.add')")
    @ApiOperation(value = "新增")
    @PostMapping("save")
    public Result save(@RequestBody ProcessType processType) {
        processTypeService.save(processType);
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.processType.update')")
    @ApiOperation(value = "修改")
    @PutMapping("update")
    public Result updateById(@RequestBody ProcessType processType) {
        processTypeService.updateById(processType);
        return Result.ok();
    }

    @ApiOperation(value = "删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        processTypeService.removeById(id);
        return Result.ok();
    }
}

