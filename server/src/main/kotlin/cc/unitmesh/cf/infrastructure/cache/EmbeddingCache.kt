package cc.unitmesh.cf.infrastructure.cache

import cc.unitmesh.nlp.embedding.Embedding
import cc.unitmesh.cf.core.utils.Constants
import cc.unitmesh.cf.core.utils.IdUtil
import cc.unitmesh.cf.infrastructure.cache.utils.EmbeddingConverter
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id

@Entity
public class EmbeddingCache(
    @Id
    val id: String = IdUtil.uuid(),

    @Column(unique = true)
    val text: String,

    @Column(length = Constants.MAX_TOKEN_LENGTH)
    @Convert(converter = EmbeddingConverter::class)
    val embedding: Embedding,
)
