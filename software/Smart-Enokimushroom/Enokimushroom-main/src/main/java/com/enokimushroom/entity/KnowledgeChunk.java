package com.enokimushroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("knowledge_chunk")
public class KnowledgeChunk {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField(value = "kb_id")
    private Long kbId;
    
    @TableField(value = "chunk_index")
    private Integer chunkIndex;
    
    @TableField(value = "content")
    private String content;
    
    @TableField(value = "create_time")
    private Date createTime;
}