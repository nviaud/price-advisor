package com.nviaud.pricing.services.parsers

import com.nviaud.pricing.services.parsers.tools.ProductNameExtractorTool
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
import org.springframework.ai.vectorstore.SearchRequest
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
    @param:Value("quotation_parser_system_prompt.txt")
    private val systemPrompt: Resource
) : QuotationParserService {


    private val logger = LoggerFactory.getLogger(javaClass)

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

        val userInputTemplate = systemPrompt.getContentAsString(Charsets.UTF_8)

        val promptTemplate = PromptTemplate.builder().template(userInputTemplate).variables(
            mapOf(
                "format" to beanOutputConverter.format,
                "productNameExtractorTool" to ProductNameExtractorTool.PRODUCT_NAME_EXTRACTOR_TOOL,
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
        val searchRequest = SearchRequest.builder()
            .query("product details")
            .topK(5)
            .similarityThreshold(0.5)
            .filterExpression("productCategory == windows AND productBrand == phenix")
            .build()

        val advisor = QuestionAnswerAdvisor
            .builder(vectorStore)
            //.searchRequest(searchRequest)
            .build()
        val request = chatClient
            .prompt(prompt)
            .toolNames(ProductNameExtractorTool.PRODUCT_NAME_EXTRACTOR_TOOL)
            .advisors(advisor)

        logger.trace("Sending prompt to ChatClient: {}", message)

        val response = request.call()
        val content = response.content() ?: throw IllegalStateException("Response content is null")
        logger.debug("Received response from ChatClient: {}", content)

        //Remove ```json ... ``` if present because some LLMs add them even if not asked to
        val contentCleaned = content
            .replace(Regex("^```json"), "")
            .replace(Regex("```$"), "")
            .trim()

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
