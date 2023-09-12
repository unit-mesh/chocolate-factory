package cc.unitmesh.cf.core.llms

import io.reactivex.rxjava3.core.Flowable

enum class TemperatureMode(val value: Double) {
    Creative(0.7),
    Balanced(0.3),
    Default(0.0),
}

interface LlmProvider {
    var temperature: Double

    fun createCompletions(messages: List<LlmMsg.ChatMessage>): List<LlmMsg.ChatChoice>

    fun simpleCompletion(messages: List<LlmMsg.ChatMessage>): String

    fun flowCompletion(messages: List<LlmMsg.ChatMessage>): Flowable<String>

    fun setTemperatureMode(mode: TemperatureMode) {
        this.temperature = mode.value
    }

    class CompletionResultNotFoundException(val messages: List<LlmMsg.ChatMessage>) :
        RuntimeException("自动补齐无结果，提示词：${messages}")
}

