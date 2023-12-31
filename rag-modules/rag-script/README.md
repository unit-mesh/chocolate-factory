# RAGScript

RAGScript 是一个使用 Kotlin DSL 的脚本语言，以用于快速使用、构建 RAG （检索增强，Retrieval Augmented Generation）应用的 PoC。

## Usage

```
// 声明依赖
@file:DependsOn("cc.unitmesh:rag-script:0.3.3")

// 引入 RAGScript 依赖
import cc.unitmesh.rag.*

rag {
    // 使用 OpenAI 作为 LLM 引擎
    llm = LlmConnector(LlmType.OpenAI)
    // 使用 SentenceTransformers 作为 Embedding 引擎
    embedding = EmbeddingEngine(EngineType.SentenceTransformers)
    // 使用 Memory 作为 Retriever
    store = Store(StoreType.Memory)

    indexing {
        // 从文件中读取文档
        val document = document("filename.txt")
        // 将文档切割成 chunk
        val chunks = document.split()
        // 建立索引
        store.indexing(chunks)
    }

    querying {
        // 查询
        store.findRelevant("workflow dsl design ").also {
            println(it)
        }
    }
}
```

