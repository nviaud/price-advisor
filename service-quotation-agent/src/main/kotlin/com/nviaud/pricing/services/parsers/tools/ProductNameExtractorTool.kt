package com.nviaud.pricing.services.parsers.tools

import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Description
import java.util.function.Function

/**
 * A tool that uses a ChatClient to extract product names from a given quotation text.
 * It sends a prompt to the chat model and retrieves the response containing the product names.
 *
 * https://docs.spring.io/spring-ai/reference/api/tools.html#_dynamic_specification_bean
 */
@Configuration(proxyBeanMethods = false)
class ProductNameExtractorTool(
    private val chatClient: ChatClient,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        const val PRODUCT_NAME_EXTRACTOR_TOOL: String = "tool-productNameExtractor"
    }

    private val systemPrompt = """
        You are an expert in product identification. 
        Your task is to extract and return a list of product names mentioned in the provided quotation text.
        Only return the product names as a list of strings, without any additional information or formatting.
        If no product names are found, return an empty list.
        Make sure to avoid duplicates in the list.
        
        Here is a raw OCR extract of a quotation:
        ```
        {quotation}
        ```
        """

    @Bean(PRODUCT_NAME_EXTRACTOR_TOOL)
    @Description("Extracts product names from a quotation text using AI")
    fun extractProductNamesFromTextBean(): Function<ProductNameExtractorToolRequest, String> {
        return Function { request: ProductNameExtractorToolRequest ->
            extractProductNamesFromText(request.quotation)
        }
    }


    fun extractProductNamesFromText(quotation: String): String {
        logger.debug("Extracting product names from quotation text using AI tool")

        val promptTemplate = PromptTemplate.builder().template(systemPrompt).variables(
            mapOf(
                "quotation" to quotation
            )
        ).build()
        val message = promptTemplate.createMessage()
        val prompt = Prompt(message)
        val request = chatClient.prompt(prompt)

        logger.trace("Sending prompt to ChatClient: {}", message)

        val response = request.call()
        val content = response.content() ?: throw IllegalStateException("Response content is null")
        logger.debug("Received response from ChatClient: {}", content)
        return content
    }
}

class ProductNameExtractorToolRequest(
    @field:ToolParam(description = "OCR data of the quotation")
    val quotation: String
)
