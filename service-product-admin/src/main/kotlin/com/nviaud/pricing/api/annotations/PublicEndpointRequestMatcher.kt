package com.nviaud.pricing.api.annotations

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping

@Component
class PublicEndpointRequestMatcher(
    private val handlerMappings: List<RequestMappingInfoHandlerMapping>
) {
    private val publicPatterns = mutableSetOf<String>()

    @PostConstruct
    fun init() {
        handlerMappings.forEach { mapping ->
            mapping.handlerMethods.forEach { (info, handler) ->
                if (handler is HandlerMethod && handler.hasMethodAnnotation(PublicEndpoint::class.java)) {
                    info.patternsCondition?.patterns?.forEach { pattern ->
                        publicPatterns.add(pattern.toString())
                    }
                    info.pathPatternsCondition?.patterns?.forEach { pattern ->
                        publicPatterns.add(pattern.toString())
                    }
                }
            }
        }
    }

    fun getPublicPatterns(): Set<String> = publicPatterns
}