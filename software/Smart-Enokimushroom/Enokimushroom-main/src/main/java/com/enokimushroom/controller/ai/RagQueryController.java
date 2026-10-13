package com.enokimushroom.controller.ai;

import com.enokimushroom.entity.AskRequest;
import com.enokimushroom.service.KnowledgeBaseService;
import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;



@RestController
@RequestMapping("/rag-query")
public class RagQueryController {

    @Autowired
    private VectorStore vectorStore;
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    @Autowired
    @Qualifier("ollamaEmbeddingModel")
    private org.springframework.ai.embedding.EmbeddingModel embeddingModel;
    @Resource
    private ChatClient vipRagChatClient;
    @Resource
    private ChatClient ChatClient;
    @Autowired
    private OllamaChatModel ollamaChatModel;

    @PostMapping(value = "/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> ask(@RequestBody AskRequest req) {
        String question = req.question;
        String chatId = req.chatId;
        Integer topK = req.topK;
        String kbId = req.kbId;

        if ("1".equals(kbId)) {
            System.out.println("kbId为1，跳过知识库召回，直接使用chatClient");
            return ollamaChatModel.stream(question);
        }

        SearchRequest request = SearchRequest.builder()
                .query(question)
                .topK(topK != null ? topK * 2 : 40)
                .build();
        List<Document> docs = vectorStore.similaritySearch(request);
        List<Document> filteredDocs = docs.stream()
                .filter(doc -> kbId.equals(doc.getMetadata().get("kbId")))
                .limit(topK != null ? topK : 5)
                .collect(Collectors.toList());
        String context = filteredDocs.stream().map(Document::getText).collect(Collectors.joining("\n"));
        System.out.println("使用的召回率为: " + topK);

        String promptWithRag = "基于以下上下文回答问题：\n\n" + context + "\n\n问题：" + question;
        return ollamaChatModel.stream(promptWithRag);
    }
}
