package com.Ink.model.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
//BaseEntity类中的属性被标记为了transient，在序列化和反序列化过程中会被忽略掉。
@Data
public class BaseEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableLogic//逻辑删除 表中数据一直在，逻辑删除后把is_deleted设为1
    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField(exist = false)//可以没有
    private Map<String,Object> param = new HashMap<>();
}
