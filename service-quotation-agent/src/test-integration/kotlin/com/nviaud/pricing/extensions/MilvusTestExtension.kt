package com.nviaud.pricing.extensions

import io.milvus.client.MilvusServiceClient
import io.milvus.param.collection.*
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore
import org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreProperties
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * JUnit 5 extension to manage Milvus collection lifecycle for integration tests.
 * It drops the collection before all tests and after all tests to ensure a clean state.
 * It also creates the collection before all tests.
 */
class MilvusTestExtension : BeforeAllCallback, AfterAllCallback {

    override fun beforeAll(context: ExtensionContext) {
        dropCollection(context)
        createCollection(context)

    }

    override fun afterAll(context: ExtensionContext) {
        dropCollection(context)
    }

    fun createCollection(context: ExtensionContext) {
        val applicationContext = SpringExtension.getApplicationContext(context)
        val milvusVectorStore = applicationContext.getBean(MilvusVectorStore::class.java)
        milvusVectorStore.afterPropertiesSet()// Ensure the collection is created
    }

    fun dropCollection(context: ExtensionContext) {
        val applicationContext = SpringExtension.getApplicationContext(context)
        val properties = applicationContext.getBean(MilvusVectorStoreProperties::class.java)
        val milvusClient = applicationContext.getBean(MilvusServiceClient::class.java)
        milvusClient.dropCollection(
            DropCollectionParam.newBuilder()
                .withDatabaseName(properties.databaseName)
                .withCollectionName(properties.collectionName)
                .build()
        )
    }

}