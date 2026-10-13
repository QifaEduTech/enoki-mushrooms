package com.enokimushroom.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enokimushroom.entity.KnowledgeBase;
import com.enokimushroom.mapper.KnowledgeBaseMapper;
import com.enokimushroom.service.KnowledgeBaseService;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> implements KnowledgeBaseService {

    @Override
    public Page<KnowledgeBase> pageList(int page, int size, String name) {
        Page<KnowledgeBase> pageParam = new Page<>(page, size);
        if (name != null && !name.isEmpty()) {
            QueryWrapper<KnowledgeBase> wrapper = new QueryWrapper<>();
            wrapper.like("name", name);
            return this.page(pageParam, wrapper);
        } else {
            return this.page(pageParam);
        }
    }
}