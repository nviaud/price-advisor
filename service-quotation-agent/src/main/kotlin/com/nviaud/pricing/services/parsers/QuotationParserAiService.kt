package com.nviaud.pricing.services.parsers

import net.sourceforge.tess4j.Tesseract
import net.sourceforge.tess4j.TesseractException
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.util.MimeType
import org.springframework.util.MimeTypeUtils
import technology.tabula.ObjectExtractor
import technology.tabula.extractors.BasicExtractionAlgorithm
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm
import technology.tabula.Table
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

@Service
@ConditionalOnProperty(
    name = ["application.services.quotation-parser.stub.enabled"],
    havingValue = "false",
    matchIfMissing = true
)
class QuotationParserAiService(
    private val chatClient: ChatClient,
    private val vectorStore: VectorStore,
    @param:Value("classpath:fra.traineddata")
    private val trainedDataResource: Resource,
) : QuotationParserService {


    private val logger = LoggerFactory.getLogger(QuotationParserAiService::class.java)

    private val beanOutputConverter = BeanOutputConverter(QuotationData::class.java)

    override fun parseDataFromQuotation(mimeType: MimeType, input: Resource): QuotationData {
        if (!input.exists()) {
            throw IllegalArgumentException("Input resource does not exist")
        }
        if (!input.isReadable) {
            throw IllegalArgumentException("Input resource is not readable")
        }
        logger.debug("Parsing quotation from resource: {}", input.filename)

        val quotation = when (mimeType) {
            MimeType.valueOf("application/pdf") -> convertPdfToTxtWithOcr(input)
            MimeTypeUtils.IMAGE_JPEG, MimeTypeUtils.IMAGE_PNG -> convertImageToTxtWithOcr(input)
            else -> throw IllegalArgumentException("Unsupported mime type: $mimeType")
        }

        val userInputTemplate = """
        You are an expert quotation parser. Your task is to extract structured data from the quotation given later.
        Multiple products can be present in the quotation.
        
        If the quotation in not english, translate it to english before extracting the data.
        
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

        The product height, width, depth and weight are optional and may not be present for all products. To extract these dimensions, look for patterns such as "HxW", "HxWxD", "H x W x D", "H mm x W mm", "H mm x W mm x D mm" or similar variations. The height is usually the first number, followed by the width and then the depth. The weight can be indicated by "kg" or "kilos". These dimensions are usually found near the product name or description.
        The quantity, unit price, total price and VAT are usually found in a tabular format or near the product description. The quantity is usually a whole number, while the unit price, total price and VAT are usually decimal numbers with a comma or dot as the decimal separator.

        Do not invent any information. If you are unsure about a data, set it to null.
        
        {format}
        
        Here is a raw OCR extract of a quotation:
        ```
        {quotation}
        ```
        """

        val promptTemplate = PromptTemplate.builder().template(userInputTemplate).variables(
            mapOf(
                "format" to beanOutputConverter.format,
                "quotation" to quotation
            )
        ).build()

        // https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html#_multimodal

//        val medias: List<Media> = when {
//            mimeType == MimeType.valueOf("application/pdf") -> convertPdfToPng(input).map { Media(MimeTypeUtils.IMAGE_PNG, it) }
//            else -> listOf(Media(mimeType, input))
//        }
//        val message = promptTemplate.createMessage(medias)

        val message = promptTemplate.createMessage()
        val prompt = Prompt(message)
        val advisor = QuestionAnswerAdvisor(vectorStore)
        val request = chatClient.prompt(prompt).advisors(advisor)

        logger.trace("Sending prompt to ChatClient: {}", message)

        val response = request.call()
        val content = response.content() ?: throw IllegalStateException("Response content is null")
        logger.debug("Received response from ChatClient: {}", content)

        //Remove ```json ... ``` if present
        val contentCleaned = content.replace(Regex("```json\\s*"), "").replace(Regex("```\\s*"), "")

        return convertToQuotationData(contentCleaned)

    }

    fun convertToQuotationData(json: String): QuotationData {
        return beanOutputConverter.convert(json)
            ?: throw IllegalStateException("Failed to convert response to QuotationData")
    }

    /**
     * Converts each page of a PDF resource to a PNG image and returns them as a list of Resources.
     * Uses PDFBox for PDF rendering.
     * @deprecated Use convertPdfToTxtWithOcr or convertPdfToTxt instead. Llava is not accurate enough on quotation images.
     */
    @Deprecated("Use convertPdfToTxtWithOcr or convertPdfToTxt instead. Llava is not accurate enough on quotation images.")
    private fun convertPdfToPng(pdfResource: Resource): List<Resource> {
        val resources = mutableListOf<Resource>()
        PDDocument.load(pdfResource.inputStream).use { document ->
            val renderer = PDFRenderer(document)
            for (pageIndex in 0 until document.numberOfPages) {
                val tempFile = File.createTempFile("quotation_page_${pageIndex}_", ".png")
                val image: BufferedImage = renderer.renderImage(pageIndex, 5f)
                ImageIO.write(image, "PNG", tempFile)
                resources.add(FileSystemResource(tempFile))
            }
        }
        return resources
    }

    /**
     * Extracts tables from a PDF resource and returns them as tab-separated text.
     * Uses Tabula Java for table extraction.
     * @deprecated Use convertPdfToTxtWithOcr instead. Tabula doesn't extract all the time the tables correctly, specially if it was an image in the PDF.
     */
    @Deprecated("Use convertPdfToTxtWithOcr instead. Tabula doesn't extract all the time the tables correctly, specially if it was an image in the PDF.")
    fun convertPdfToTxt(pdfResource: Resource): String {
        val result = StringBuilder()
        pdfResource.inputStream.use { inputStream ->
            val extractor = ObjectExtractor(PDDocument.load(inputStream))
            extractor.extract().forEach { page ->
                val spreadsheetAlgo = SpreadsheetExtractionAlgorithm()
                val tables = spreadsheetAlgo.extract(page)
                if (tables.isEmpty()) {
                    val basicTables = BasicExtractionAlgorithm().extract(page)
                    basicTables.forEach { table ->
                        result.append(tableToString(table)).append("\n")
                    }
                } else {
                    tables.forEach { table ->
                        result.append(tableToString(table)).append("\n")
                    }
                }
            }
        }
        return result.toString()
    }

    private fun tableToString(table: Table): String {
        return table.rows.joinToString("\n") { row ->
            row.joinToString("\t") { it.text }
        }
    }

    /**
     * Converts a PDF resource to text using OCR (Tesseract via tess4j).
     * Each page is rendered as an image and OCR is performed on it.
     * @param pdfResource PDF file as a Spring Resource
     * @return Concatenated OCR text from all pages
     */
    fun convertPdfToTxtWithOcr(pdfResource: Resource): String {
        val ocr = Tesseract()
         ocr.setDatapath(trainedDataResource.file.parentFile.absolutePath)
         ocr.setLanguage("fra")
        val sb = StringBuilder()
        PDDocument.load(pdfResource.inputStream).use { document ->
            val renderer = PDFRenderer(document)
            for (pageIndex in 0 until document.numberOfPages) {
                val image: BufferedImage = renderer.renderImage(pageIndex, 2f)
                try {
                    val text = ocr.doOCR(image)
                    sb.append(text).append("\n")
                } catch (e: TesseractException) {
                    logger.error("OCR failed on page $pageIndex", e)
                }
            }
        }
        return sb.toString()
    }

    /**
     * Converts an image resource (JPEG, PNG) to text using OCR (Tesseract via tess4j).
     * @param imageResource Image file as a Spring Resource
     * @return OCR text
     */
    fun convertImageToTxtWithOcr(imageResource: Resource): String {
        val ocr = Tesseract()
        ocr.setDatapath(trainedDataResource.file.parentFile.absolutePath)
        ocr.setLanguage("fra")
        return ocr.doOCR(imageResource.file)
    }

}
