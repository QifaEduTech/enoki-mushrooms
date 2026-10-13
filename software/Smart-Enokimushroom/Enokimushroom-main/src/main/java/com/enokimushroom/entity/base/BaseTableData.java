package com.enokimushroom.entity.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;


@Data
public class BaseTableData {

    /**
     * 创建时间
     * */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
    
    /**
     * 编辑时间
     * */
    @TableField(value = "edit_time", fill = FieldFill.INSERT_UPDATE)
    private Date editTime;
    
    
    /**
     * 1:正常 0:删除
     */
    @TableField(value = "status")
    private Integer status;
}
