package com.enokimushroom.config;


import com.enokimushroom.advisor.ChatTypeHistoryAdvisor;
import com.enokimushroom.advisor.ChatTypeTitleAdvisor;
import com.enokimushroom.enums.ChatType;
import com.enokimushroom.service.ChatTypeHistoryService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.ollama.OllamaChatModel;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.enokimushroom.config.Prompts.*;


@Configuration
public class PrivateRagAiAutoConfiguration {



    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(20)
                .build();
    }

    @Bean
    public ChatClient ChatClient(OllamaChatModel model, ChatMemory chatMemory,
                                 ChatTypeHistoryService chatTypeHistoryService, @Qualifier("titleChatClient")ChatClient titleChatClient) {
        return ChatClient
                .builder(model)
                .defaultSystem(Prompts.Client_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).order(MESSAGE_CHAT_MEMORY_ADVISOR_ORDER).build()
                )
                .build();
    }

    @Bean
    public ChatClient vipRagChatClient(OllamaChatModel model, ChatMemory chatMemory,
                                       ChatTypeHistoryService chatTypeHistoryService, @Qualifier("titleChatClient")ChatClient titleChatClient) {
        return ChatClient
                .builder(model)
                .defaultSystem(Prompts.PRIVATE_RAG_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        ChatTypeHistoryAdvisor.builder(chatTypeHistoryService).type(ChatType.PRIVATERAG.getCode())
                                .order(CHAT_TYPE_HISTORY_ADVISOR_ORDER).build(),
                        ChatTypeTitleAdvisor.builder(chatTypeHistoryService).type(ChatType.PRIVATERAG.getCode())
                                .chatClient(titleChatClient).chatMemory(chatMemory).order(CHAT_TITLE_ADVISOR_ORDER).build(),
                        MessageChatMemoryAdvisor.builder(chatMemory).order(MESSAGE_CHAT_MEMORY_ADVISOR_ORDER).build()
                )
                .build();
    }
    @Bean
    public ChatClient clusterChatClient(OllamaChatModel model, ChatMemory chatMemory,
                                       ChatTypeHistoryService chatTypeHistoryService, @Qualifier("titleChatClient")ChatClient titleChatClient) {
        return ChatClient
                .builder(model)
                .defaultSystem(Prompts.CLUSTER_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).order(MESSAGE_CHAT_MEMORY_ADVISOR_ORDER).build()
                )
                .build();
    }
    @Bean
    public ChatClient diseaseChatClient(OllamaChatModel model, ChatMemory chatMemory,
                                       ChatTypeHistoryService chatTypeHistoryService, @Qualifier("titleChatClient")ChatClient titleChatClient) {
        return ChatClient
                .builder(model)
                .defaultSystem(Prompts.DISEASE_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).order(MESSAGE_CHAT_MEMORY_ADVISOR_ORDER).build()
                )
                .build();
    }

    @Bean
    public ChatClient titleChatClient(DeepSeekChatModel model) {
        return ChatClient
                .builder(model)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
                .build();
    }

    @Bean
    public VectorStore vectorStore(@Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
