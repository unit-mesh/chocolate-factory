package cc.unitmesh.docs

import cc.unitmesh.docs.base.TreeDoc
import cc.unitmesh.docs.render.CustomJekyllFrontMatter
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import java.nio.file.Path

class Runner : CliktCommand() {
    private val dir by option("-d", "--dir", help = "The directory to process").default(".")

    override fun run() {
        val rootDir = Path.of(dir).toAbsolutePath().normalize()

        // the prompt script parts
        val promptScriptDir = rootDir.resolve("llm-modules/prompt-script")

        val treeDocs = PromptScriptDocGen(promptScriptDir).execute()
        val docs = renderDocs(treeDocs)
        val outputDir = rootDir.resolve("docs/prompt-script")
        var index = 10
        docs.forEach { (name, content) ->
            val permalink = uppercaseToDash(name)
            var output = CustomJekyllFrontMatter(name, "Prompt Script", index, permalink)
                .toMarkdown()

            output =
                "$output\n{: .warning }\nAutomatically generated documentation; use the command `./gradlew :docs-builder:run` and update comments in the source code to reflect changes."

            val outputFile = outputDir.resolve("$permalink.md")
            outputFile.toFile().writeText(output + "\n\n" + content)
            index += 1
        }
    }

    fun uppercaseToDash(name: String): String {
        val result = StringBuilder()

        for (char in name) {
            if (char.isUpperCase() && result.isNotEmpty()) {
                result.append('-')
            }
            result.append(char.lowercase())
        }

        return result.toString()
    }


    private fun renderDocs(treeDocs: List<TreeDoc>): Map<String, String> {
        return treeDocs.associate { treeDoc ->
            val output = StringBuilder()
            val root = treeDoc.root
            val children = treeDoc.children
            val rootFileName = root.element?.containingFile?.name ?: "unknown"
            println("rootFileName: $rootFileName")

            output.append("## ${root.element?.name} \n\n> ${root.contentTag.getContent()}\n\n")
            children.forEach { child ->
                output.append("- ${child.element?.name}. ${child.contentTag.getContent()}\n")
            }

            root.element!!.name!! to output.toString()
        }
    }
}

fun main(args: Array<String>) = Runner().main(args)
