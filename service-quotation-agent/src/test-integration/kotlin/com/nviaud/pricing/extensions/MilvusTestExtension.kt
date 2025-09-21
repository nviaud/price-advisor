package com.nviaud.pricing.extensions

import io.milvus.client.MilvusServiceClient
import io.milvus.param.collection.*
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreProperties
import org.springframework.test.context.junit.jupiter.SpringExtension

class MilvusTestExtension : BeforeAllCallback, AfterAllCallback {

    override fun beforeAll(context: ExtensionContext) {
        val applicationContext = SpringExtension.getApplicationContext(context)
        val properties = applicationContext.getBean(MilvusVectorStoreProperties::class.java)
        val milvusClient = applicationContext.getBean(MilvusServiceClient::class.java)
        milvusClient.createCollection(
            CreateCollectionParam.newBuilder()
                .withDatabaseName(properties.databaseName)
                .withCollectionName(properties.collectionName)
                //spring.ai.vectorstore.milvus.embeddingDimension=1024
                //spring.ai.vectorstore.milvus.indexType=IVF_FLAT
                //spring.ai.vectorstore.milvus.metricType=COSINE
                .build()
        )
    }

    override fun afterAll(context: ExtensionContext) {
        val applicationContext = SpringExtension.getApplicationContext(context)
        val properties = applicationContext.getBean(MilvusVectorStoreProperties::class.java)
        val milvusClient = applicationContext.getBean(MilvusServiceClient::class.java)
        // Drop collection if exists
        milvusClient.dropCollection(
            DropCollectionParam.newBuilder()
                .withDatabaseName(properties.databaseName)
                .withCollectionName(properties.collectionName)
                .build()
        )
    }

}