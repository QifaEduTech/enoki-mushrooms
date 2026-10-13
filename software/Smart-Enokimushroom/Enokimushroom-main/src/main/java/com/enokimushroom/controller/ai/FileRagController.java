package com.enokimushroom.controller.ai;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enokimushroom.common.ApiResponse;
import com.enokimushroom.entity.KnowledgeBase;
import com.enokimushroom.service.KnowledgeBaseService;
import com.enokimushroom.service.imp.FileRagServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file-rag")
public class FileRagController {
    // 文件rag

    @Autowired
    private FileRagServiceImpl fileRagService;
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @PostMapping("/upload")
    public ApiResponse<Long> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "remark", required = false) String remark
    ) {
        Long kbId = fileRagService.processFile(file, name, remark);
        return ApiResponse.ok(kbId);
    }

    @GetMapping("/list")
    public ApiResponse<List<KnowledgeBase>> listKnowledgeBases() {
        List<KnowledgeBase> list = knowledgeBaseService.list();
        return ApiResponse.ok(list);
    }

    @GetMapping("/page")
    public ApiResponse<Map<String, Object>> pageKnowledgeBases(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "name", required = false) String name
    ) {
        Page<KnowledgeBase> pageObj = knowledgeBaseService.pageList(page, size, name);
        Map<String, Object> result = new HashMap<>();
        result.put("total", pageObj.getTotal());
        result.put("list", pageObj.getRecords());
        return ApiResponse.ok(result);
    }

    @PostMapping("/delete")
    public ApiResponse<Boolean> deleteKnowledgeBase(@RequestParam Long id) {
        boolean removed = knowledgeBaseService.removeById(id);
        return ApiResponse.ok(removed);
    }

    @PostMapping("/update")
    public ApiResponse<Boolean> updateKnowledgeBase(@RequestBody KnowledgeBase kb) {
        boolean updated = knowledgeBaseService.updateById(kb);
        return ApiResponse.ok(updated);
    }
}
