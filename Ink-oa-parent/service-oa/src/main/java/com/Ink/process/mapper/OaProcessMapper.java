package com.Ink.process.mapper;
import com.Ink.model.process.Process;
import com.Ink.vo.process.ProcessQueryVo;
import com.Ink.vo.process.ProcessVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 审批类型 Mapper 接口
 * </p>
 *
 * @author Inkwair
 * @since 2023-05-13
 */
public interface OaProcessMapper extends BaseMapper<Process> {
    IPage<ProcessVo> selectPage(Page<ProcessVo> pageInfo,@Param("vo") ProcessQueryVo processQueryVo);
}
