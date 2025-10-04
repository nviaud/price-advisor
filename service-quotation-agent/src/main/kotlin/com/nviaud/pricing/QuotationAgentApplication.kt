package com.nviaud.pricing

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class QuotationAgentApplication

fun main(args: Array<String>) {
	runApplication<QuotationAgentApplication>(*args)
}
