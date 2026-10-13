package com.enokimushroom.service.imp;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.enokimushroom.entity.ChatTypeHistory;
import com.enokimushroom.mapper.ChatHistoryMapper;
import com.enokimushroom.service.ChatTypeHistoryService;
import com.enokimushroom.entity.vo.ChatTypeHistoryVo;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ChatTypeHistoryServiceImpl implements ChatTypeHistoryService {

    @Autowired
    private ChatHistoryMapper chatHistoryMapper;

    @Autowired
    private ChatMemory chatMemory;


    /**
     * 保存会话记录
     * @param type 业务类型
     * @param chatId 会话ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Integer type, String chatId){
        QueryWrapper<ChatTypeHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("type", type).eq("chat_id", chatId);
        ChatTypeHistory chatTypeHistory = chatHistoryMapper.selectOne(wrapper);
        if (Objects.isNull(chatTypeHistory)){
            chatTypeHistory = new ChatTypeHistory();
            chatTypeHistory.setType(type);
            chatTypeHistory.setChatId(chatId);
            chatHistoryMapper.insert(chatTypeHistory);
        }
    }

    /**
     * 获取会话ID列表
     * @param type 业务类型
     * @return 会话ID列表
     */
    @Override
    public List<String> getChatIdList(Integer type){
        QueryWrapper<ChatTypeHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("type", type);
        List<ChatTypeHistory> chatTypeHistoryList = chatHistoryMapper.selectList(wrapper);
        return chatTypeHistoryList.stream()
                .map(ChatTypeHistory::getChatId)
                .toList();
    }

    /**
     * 删除会话记录
     * @param type 业务类型
     * @param chatId 会话ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer type, String chatId) {
        UpdateWrapper<ChatTypeHistory> wrapper = new UpdateWrapper<>();
        wrapper.eq("type", type).eq("chat_id", chatId);
        chatHistoryMapper.delete(wrapper);
        chatMemory.clear(chatId);
    }

    /**
     * 获取会话
     * @param type 业务类型
     * @param chatId 会话ID
     * @return 会话
     */
    @Override
    public ChatTypeHistory getChatTypeHistory(Integer type, String chatId) {
        QueryWrapper<ChatTypeHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("type", type).eq("chat_id", chatId);
        return chatHistoryMapper.selectOne(wrapper);
    }

    @Override
    public void updateById(ChatTypeHistory chatTypeHistory){
        chatHistoryMapper.updateById(chatTypeHistory);
    }

    /**
     * 获取会话ID列表
     * @param type 业务类型
     * @return 会话ID列表
     */
    @Override
    public List<ChatTypeHistoryVo> getChatTypeHistoryList(Integer type){
        QueryWrapper<ChatTypeHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("type", type);
        List<ChatTypeHistory> chatTypeHistoryList = chatHistoryMapper.selectList(wrapper);
        return chatTypeHistoryList.stream()
                .map(chatTypeHistory -> {
                    ChatTypeHistoryVo chatTypeHistoryVo = new ChatTypeHistoryVo();
                    BeanUtils.copyProperties(chatTypeHistory, chatTypeHistoryVo);
                    return chatTypeHistoryVo;
                })
                .toList();
    }
}
