package com.nviaud.pricing.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@Suppress("unused")
class AiConfig {

    @Bean
    fun chatClient(chatModel: OllamaChatModel): ChatClient {
        return ChatClient.create(chatModel)
    }

}