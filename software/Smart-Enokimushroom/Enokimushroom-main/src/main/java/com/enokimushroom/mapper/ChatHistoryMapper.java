package com.enokimushroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enokimushroom.entity.ChatTypeHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatTypeHistory> {
}
