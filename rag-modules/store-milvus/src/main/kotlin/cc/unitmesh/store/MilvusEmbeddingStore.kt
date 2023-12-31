package cc.unitmesh.store

import cc.unitmesh.nlp.embedding.Embedding
import cc.unitmesh.rag.document.Document
import cc.unitmesh.rag.store.EmbeddingMatch
import cc.unitmesh.rag.store.EmbeddingStore
import io.milvus.client.MilvusServiceClient
import io.milvus.param.ConnectParam

/**
 * The MilvusEmbeddingStore class is an implementation of the EmbeddingStore interface that
 * provides functionality to store and retrieve embeddings using the Milvus service.
 *
 * @property host The host address of the Milvus service. Default value is "localhost".
 * @property port The port number of the Milvus service. Default value is 19530.
 * @property indexName The name of the index used for storing embeddings. Default value is "chocolate-code".
 * @property client The MilvusServiceClient instance used for communication with the Milvus service.
 *
 */
class MilvusEmbeddingStore(
    private val host: String = "localhost",
    private val port: Int = 19530,
    private val indexName: String = "chocolate-code",
) : EmbeddingStore<Document> {
    private var client: MilvusServiceClient

    init {
        val param = ConnectParam.newBuilder()
            .withHost("localhost")
            .withPort(19530)
            .build()

        client = MilvusServiceClient(param)


    }
    override fun add(embedding: Embedding): String {
        TODO("Not yet implemented")
    }

    override fun add(id: String, embedding: Embedding) {
        TODO("Not yet implemented")
    }

    override fun addAll(embeddings: List<Embedding>): List<String> {
        TODO("Not yet implemented")
    }

    override fun findRelevant(
        referenceEmbedding: Embedding,
        maxResults: Int,
        minScore: Double,
    ): List<EmbeddingMatch<Document>> {
        TODO("Not yet implemented")
    }

    override fun addAll(embeddings: List<Embedding>, embedded: List<Document>): List<String> {
        TODO("Not yet implemented")
    }

    override fun add(embedding: Embedding, embedded: Document): String {
        TODO("Not yet implemented")
    }
}