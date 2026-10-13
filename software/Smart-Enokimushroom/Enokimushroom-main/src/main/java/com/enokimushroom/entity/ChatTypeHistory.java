package com.enokimushroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.enokimushroom.entity.base.BaseTableData;
import lombok.Data;
import lombok.EqualsAndHashCode;



@EqualsAndHashCode(callSuper = true)
@Data
@TableName("d_chat_type_history")
public class ChatTypeHistory extends BaseTableData {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField(value = "type")
    private Integer type;
    
    @TableField(value = "chat_id")
    private String chatId;
    
    @TableField(value = "title")
    private String title;
}
