package com.enokimushroom.service.imp;

import com.enokimushroom.entity.KnowledgeBase;
import com.enokimushroom.entity.KnowledgeChunk;
import com.enokimushroom.mapper.KnowledgeChunkMapper;
import com.enokimushroom.service.KnowledgeBaseService;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class FileRagServiceImpl {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    @Autowired
    private KnowledgeChunkMapper knowledgeChunkMapper;
    @Autowired
    private VectorStore vectorStore;
    @Autowired
    @Qualifier("ollamaEmbeddingModel")
    private EmbeddingModel embeddingModel;

    /**
     * 数据解析及向量化
     * @param file 文件
     * @param name 名称
     * @param remark 备注
     * @return
     */
    public Long processFile(MultipartFile file, String name, String remark) {
        String content = parseFile(file);
        int chunkSize = esChunkSize(content.length());
        int topK = esTopK(content.length());
        KnowledgeBase kb = new KnowledgeBase();
        kb.setName(name);
        kb.setRemark(remark);
        kb.setStatus(1);
        kb.setTopK(topK);
        kb.setFileName(file.getOriginalFilename());
        kb.setUploadTime(new Date());
        knowledgeBaseService.save(kb);
        Long kbId = kb.getId();
        List<String> chunks = splitContent(content, chunkSize);
        int idx=0;
        for (String chunk:chunks){
            Document doc = new Document(chunk);
            doc.getMetadata().put("kbid",kbId);
            vectorStore.add(Collections.singletonList(doc));
            KnowledgeChunk kc = new KnowledgeChunk();
            kc.setKbId(kbId);
            kc.setContent(chunk);
            kc.setChunkIndex(idx++);
            kc.setCreateTime(new Date());
            knowledgeChunkMapper.insert(kc);
        }

        return kbId;
    }

    private String parseFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) throw new RuntimeException("文件名为空");
        String lower = filename.toLowerCase();
        try {
            if (lower.endsWith(".md")) {
                // Markdown解析
                String md = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
                // 去除markdown语法，仅保留文本
                org.commonmark.node.Node document = new org.commonmark.parser.Parser.Builder().build().parse(md);
                org.commonmark.renderer.text.TextContentRenderer renderer = org.commonmark.renderer.text.TextContentRenderer.builder().build();
                return renderer.render(document);
            } else if (lower.endsWith(".docx")) {
                // Word(docx)解析
                try (org.apache.poi.xwpf.usermodel.XWPFDocument doc = new org.apache.poi.xwpf.usermodel.XWPFDocument(file.getInputStream())) {
                    org.apache.poi.xwpf.extractor.XWPFWordExtractor extractor = new org.apache.poi.xwpf.extractor.XWPFWordExtractor(doc);
                    return extractor.getText();
                }
            } else if (lower.endsWith(".txt")) {
                // 纯文本
                return new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
            } else {
                throw new RuntimeException("暂不支持的文件类型: " + filename);
            }
        } catch (Exception e) {
            throw new RuntimeException("文件解析失败: " + filename, e);
        }
    }

    private List<String> splitContent(String content, int chunkSize) {
        // 优先按段落切分
        String[] paragraphs = content.split("\\n+|\\r+|\\r\\n+");
        List<String> chunks = new ArrayList<>();
        for (String para : paragraphs) {
            if (para.trim().isEmpty()) continue;
            // 按句子切分
            String[] sentences = para.split("(?<=[。！？.!?])");
            StringBuilder chunk = new StringBuilder();
            int sentenceCount = 0;
            for (String sentence : sentences) {
                if (sentence.trim().isEmpty()) continue;
                chunk.append(sentence);
                sentenceCount++;
                // 合并2-3句为一个chunk，或超出chunkSize就切分
                if (sentenceCount >= 3 || chunk.length() > chunkSize) {
                    chunks.add(chunk.toString());
                    chunk = new StringBuilder();
                    sentenceCount = 0;
                }
            }
            if (chunk.length() > 0) {
                chunks.add(chunk.toString());
            }
        }
        return chunks;
    }

    private int esChunkSize(int totalLength) {
        if (totalLength < 1000) return 100;
        if (totalLength < 5000) return 200;
        if (totalLength < 20000) return 300;
        return 400;
    }

    private int esTopK(int totalLength) {
        if (totalLength < 1000) return 3;
        if (totalLength < 5000) return 5;
        if (totalLength < 20000) return 8;
        return 10;
    }
}