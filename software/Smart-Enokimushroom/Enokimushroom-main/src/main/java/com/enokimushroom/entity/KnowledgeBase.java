package com.enokimushroom.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("knowledge_base")
public class KnowledgeBase {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField(value = "name")
    private String name;
    
    @TableField(value = "remark")
    private String remark;
    
    @TableField(value = "file_name")
    private String fileName;
    
    @TableField(value = "user_id")
    private Long userId;
    
    @TableField(value = "upload_time")
    private Date uploadTime;
    
    @TableField(value = "status")
    private Integer status;
    
    @TableField(value = "top_k")
    private Integer topK;
}
