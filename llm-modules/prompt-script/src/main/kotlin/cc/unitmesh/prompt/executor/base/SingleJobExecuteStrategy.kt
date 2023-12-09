package cc.unitmesh.prompt.executor.base

import cc.unitmesh.cf.core.llms.LlmMsg
import cc.unitmesh.prompt.model.Job
import cc.unitmesh.prompt.template.TemplateDataCompile
import cc.unitmesh.template.TemplateEngineType
import cc.unitmesh.template.TemplateRoleSplitter
import org.slf4j.Logger
import java.math.BigDecimal
import java.nio.file.Path

open class SingleJobExecuteStrategy(
    open val jobName: String,
    open val job: Job,
    override val basePath: Path
) : JobStrategyExecutor {

    override fun execute() {

    }
    fun execSingleJob(name: String, job: Job, temperature: BigDecimal? = null): String {
        val llmProvider = createLlmProvider(job, temperature)

        val prompt = renderSingleJobTemplate(job)
        val msgs = TemplateRoleSplitter().split(prompt)
        val messages = LlmMsg.fromMap(msgs)

        if (messages.isEmpty()) {
            throw Exception("no messages found in template")
        }

        val resultFileName = createFileName("prompt-log")
        writeToFile(resultFileName, messages.joinToString("\n") { it.content })
        log.info("save prompt to debug file: $resultFileName")

        return llmProvider.completion(messages)
    }

    fun renderSingleJobTemplate(job: Job): String {
        val ext = job.template.substringAfterLast(".")
        return when (ext) {
            "vm", "vsl", "ft" -> {
                val factory = TemplateDataCompile(type = TemplateEngineType.VELOCITY)
                val templatePath = this.basePath.resolve(job.template).toString()
                factory.compile(templatePath, job.templateDatasource, this.basePath)
            }

            else -> throw Exception("unsupported template type: $ext")
        }
    }

    companion object {
        val log: Logger = org.slf4j.LoggerFactory.getLogger(SingleJobExecuteStrategy::class.java)
    }
}