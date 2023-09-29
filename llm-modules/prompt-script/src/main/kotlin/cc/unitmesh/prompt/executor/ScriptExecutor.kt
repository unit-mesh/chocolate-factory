package cc.unitmesh.prompt.executor

import cc.unitmesh.connection.BaseConnection
import cc.unitmesh.connection.OpenAiConnection
import cc.unitmesh.openai.OpenAiProvider
import cc.unitmesh.prompt.model.Job
import cc.unitmesh.prompt.model.PromptScript
import cc.unitmesh.prompt.template.TemplateCompilerFactory
import cc.unitmesh.prompt.template.TemplateEngineType
import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.decodeFromString
import java.io.File
import java.io.InputStream

class ScriptExecutor(
    val scriptFile: InputStream,
) {
    fun execute() {
        // load script file and parse to PromptScript
        val context = scriptFile.readBytes().toString(Charsets.UTF_8)
        val script: PromptScript = PromptScript.fromString(context) ?: return

        // execute script
        script.jobs.forEach { (name, job) ->
            println("execute job: $name")
            runJob(job)
        }
    }

    private fun runJob(job: Job) {
        val connection = createConnection(job)
        val llmProvider = when (connection) {
            is OpenAiConnection -> OpenAiProvider(connection.apiKey, connection.apiHost)
            else -> throw Exception("unsupported connection type: ${connection.type}")
        }

        val template = createTemplate(job)
    }

    private fun createTemplate(job: Job): String {
        // get ext from job.template, and select the right template engine
        val ext = job.template.substringAfterLast(".")
        return when (ext) {
            "vm" -> {
                val factory = TemplateCompilerFactory(type = TemplateEngineType.VELOCITY)
                factory.compile(job.template, job.data!!)
            }

            else -> throw Exception("unsupported template type: $ext")
        }
    }

    private fun createConnection(job: Job): BaseConnection {
        val connectionFile: File = File(job.connection.file)
        val connection = connectionFile.readBytes().toString(Charsets.UTF_8)

        val configuration = YamlConfiguration(polymorphismStyle = PolymorphismStyle.Property)
        return Yaml(configuration = configuration).decodeFromString<BaseConnection>(connection)
    }
}