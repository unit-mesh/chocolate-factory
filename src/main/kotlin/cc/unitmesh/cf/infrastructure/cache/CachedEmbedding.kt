package cc.unitmesh.cf.infrastructure.cache

import cc.unitmesh.cf.core.llms.Embedding
import cc.unitmesh.cf.core.llms.EmbeddingProvider
import cc.unitmesh.cf.infrastructure.repository.EmbeddingCacheRepository
import org.springframework.stereotype.Component

@Component
class CachedEmbedding(
    val embeddingProvider: EmbeddingProvider,
    val cacheRepository: EmbeddingCacheRepository,
) {
    fun createEmbedding(text: String): Embedding {
        val cache = EmbeddingCache(text = text, embedding = embeddingProvider.createEmbedding(text))
        val entry = cacheRepository.findByText(text) ?: cacheRepository.save(cache)
        return entry.embedding
    }
}