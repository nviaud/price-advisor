package com.priceadvisor.bff

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BffGatewayApplication

fun main(args: Array<String>) {
    runApplication<BffGatewayApplication>(*args)
}

