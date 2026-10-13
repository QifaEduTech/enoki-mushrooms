package com.enokimushroom.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enokimushroom.entity.KnowledgeBase;


public interface KnowledgeBaseService extends IService<KnowledgeBase> {

    Page<KnowledgeBase> pageList(int page, int size, String name);
}