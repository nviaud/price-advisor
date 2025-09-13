package com.nviaud.pricing.services

import com.nviaud.pricing.entities.Quotation
import com.nviaud.pricing.events.Events
import com.nviaud.pricing.events.v1.QuotationProduct
import com.nviaud.pricing.events.v1.QuotationSubmitted
import com.nviaud.pricing.repositories.QuotationRepository
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.stereotype.Service
import java.math.BigDecimal


@Service
class QuotationService(
    private val productService: ProductService,
    private val quotationRepository: QuotationRepository,
    private val chatClient: ChatClient,
    private val streamBridge: StreamBridge,
)  {

    private val logger = LoggerFactory.getLogger(QuotationService::class.java)

    private val beanOutputConverter = BeanOutputConverter(QuotationData::class.java)

    fun submitQuotation(content: String): Quotation {
        val productBrands = productService.getAllProductBrands()
        val productCategories = productService.getAllProductCategories()
        val productNames = productService.getAllProductNames()
        val quotationData = parseDataFromQuotationContent(content, productBrands, productCategories, productNames)
        // TODO find a way to track unique quotation ID (hash of content ?)
        val quotation = Quotation()
        val quotationSaved = quotationRepository.save(quotation)
        val quotationEvent = entityToEvent(quotationSaved, quotationData)
        publishQuotationSubmittedEvent(quotationEvent)
        return quotationSaved
    }

    private fun entityToEvent(quotation: Quotation, quotationData: QuotationData): QuotationSubmitted {
        return QuotationSubmitted(
            quotationId = quotation.id.toString(),
            products =
                quotationData.products.map {
                    val product = productService.findProductByName(it.productName)
                        ?: throw NoSuchElementException("Product with name ${it.productName} not found")
                    QuotationProduct(
                        productId = product.id.toString(),
                        price = it.unitPrice
                    )
                }
        )
    }

    private fun publishQuotationSubmittedEvent(event: QuotationSubmitted) {
        streamBridge.send(Events.QUOTATION_SUBMITTED_V1, event)
    }

    fun parseDataFromQuotationContent(content: String, productBrands: List<String>, productCategories: List<String>, productNames: List<String>): QuotationData {
        val userInputTemplate = """
        You are an expert quotation parser. Multiple products can be present in a single quotation.
        Your task is to extract structured data from the quotation content.
        
        For the quotation,
        - The quotation date is the date when the quotation was issued.
        - The date must be in ISO 8601 format (YYYY-MM-DD).
        
        For each product in the quotation,
        - The product brand is the manufacturer or brand name of the product.
        - The product name is the specific name or model of the product.
        - The product category is the type or category of the product.
        - The product height is the height of the product in centimeters (mm).
        - The product width is the width of the product in centimeters (mm).
        - The product depth is the depth of the product in centimeters (mm).
        - The product weight is the weight of the product in kilograms (kg).
        - The quantity is the number of units of the product.
        - The unit price is the price per unit of the product in euros (€).
        - The total price is the total price for the quantity of the product in euros (€).
        - The VAT is the value-added tax amount for the product in euros (€).
        
        The product brand can only be one of the allowed values from this list: [{productBrands}]
        The product category can only be one of the allowed values from this list: [{productCategories}]
        The product name can only be one of the allowed values from this list: [{productNames}]

        The product height, width, depth and weight are optional and may not be present for all products. To extract these dimensions, look for patterns such as "HxW", "HxWxD", "H x W x D", "H mm x W mm", "H mm x W mm x D mm" or similar variations. The height is usually the first number, followed by the width and then the depth. The weight can be indicated by "kg" or "kilos". These dimensions are usually found near the product name or description.
        The quantity, unit price, total price and VAT are usually found in a tabular format or near the product description. The quantity is usually a whole number, while the unit price, total price and VAT are usually decimal numbers with a comma or dot as the decimal separator.
        
        If a data is not present in the content, set it to null. 

        {format}
        
        Use the following quotation content for extraction:
        {content}
        """

        // https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html#_using_converters

        val promptTemplate = PromptTemplate.builder().template(userInputTemplate).variables(
            mapOf(
                "productBrands" to productBrands.joinToString(", "),
                "productCategories" to productCategories.joinToString(", "),
                "productNames" to productNames.joinToString(", "),
                "format" to beanOutputConverter.format,
                "content" to content
            )
        ).build()
        val message = promptTemplate.createMessage()
        val prompt = Prompt(message)
        logger.debug("Sending prompt to ChatClient: {}", message)
        val response = chatClient.prompt(prompt).call()
        val content = response.content() ?: throw IllegalStateException("Response content is null")
        logger.debug("Received response from ChatClient: {}", content)
        return convertToQuotationData(content)
    }

    fun convertToQuotationData(json: String): QuotationData {
        return beanOutputConverter.convert(json)
            ?: throw IllegalStateException("Failed to convert response to QuotationData")
    }

    fun parseDataFromQuotationPicture(productBrands: List<String>, productCategories: List<String>, productNames: List<String>): QuotationData {
        TODO("Not yet implemented")
    }
}

data class QuotationData(
   val products: List<QuotationDataProduct>,
   val quotationDate: String,
)

data class QuotationDataProduct (
    val productBrand: String,
    val productName: String,
    val productCategory: String,
    var productHeight: Int? = null,
    var productWidth: Int? = null,
    var productDepth: Int? = null,
    var productWeight: Int? = null,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal,
    val vat: BigDecimal? = null,
)
