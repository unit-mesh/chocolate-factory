package cc.unitmesh.cf.domains.frontend

import cc.unitmesh.cf.core.base.Answer
import cc.unitmesh.cf.core.base.FlowActionFlag
import cc.unitmesh.cf.core.prompt.*
import cc.unitmesh.cf.core.workflow.StageContext
import cc.unitmesh.cf.core.workflow.Workflow
import cc.unitmesh.cf.core.workflow.WorkflowResult
import cc.unitmesh.cf.domains.frontend.context.FEDslContextBuilder
import cc.unitmesh.cf.domains.frontend.context.FEVariableResolver
import cc.unitmesh.cf.domains.frontend.flow.FEProblemClarifier
import cc.unitmesh.cf.domains.frontend.flow.FESolutionDesigner
import cc.unitmesh.cf.domains.frontend.flow.FESolutionExecutor
import cc.unitmesh.cf.domains.frontend.model.UiPage
import cc.unitmesh.cf.infrastructure.llms.completion.LlmProvider
import cc.unitmesh.cf.infrastructure.llms.model.LlmMsg
import cc.unitmesh.cf.presentation.controller.Message
import cc.unitmesh.cf.presentation.domain.ChatWebContext
import com.azure.ai.openai.models.ChatRole
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class FEWorkflow() : Workflow() {
    @Autowired
    private lateinit var contextBuilder: FEDslContextBuilder

    @Autowired
    private lateinit var variableResolver: FEVariableResolver

    @Autowired
    private lateinit var llmProvider: LlmProvider

    override val prompts: LinkedHashMap<StageContext.Stage, StageContext>
        get() = linkedMapOf(
            CLARIFY.stage to CLARIFY,
            DESIGN.stage to DESIGN,
            EXECUTE.stage to EXECUTE
        )

    override fun execute(prompt: StageContext, chatWebContext: ChatWebContext): WorkflowResult? {
        val messages = chatWebContext.messages

        var stage = prompt.stage

        if (chatWebContext.stage == StageContext.Stage.Design && chatWebContext.messages.last().content == "YES") {
            stage = StageContext.Stage.Execute
        }

        when (stage) {
            StageContext.Stage.Clarify -> {
                val clarify = FEProblemClarifier(contextBuilder, llmProvider, variableResolver)
                    .clarify(
                        domain = "frontend",
                        question = messages.last().content,
                        histories = messages.map { it.content }
                    )

                val nextStage = when (clarify.first) {
                    FlowActionFlag.CONTINUE -> StageContext.Stage.Clarify
                    FlowActionFlag.FINISH -> StageContext.Stage.Design
                }

                // if current stage finish, then go to next stage
                if (nextStage == StageContext.Stage.Design) {
                    chatWebContext.messages += Message(
                        role = LlmMsg.ChatRole.Assistant.value,
                        content = clarify.second,
                    )

                    return execute(DESIGN, chatWebContext)
                }

                return WorkflowResult(
                    currentStage = StageContext.Stage.Clarify,
                    nextStage = nextStage,
                    responseMsg = clarify.second,
                    resultType = String::class.java.toString(),
                    result = clarify.second
                )
            }

            StageContext.Stage.Design -> {
                val design = FESolutionDesigner(contextBuilder, llmProvider, variableResolver).design(
                    domain = "frontend",
                    question = messages.last().content,
                    histories = messages.map { it.content }
                )

                return WorkflowResult(
                    currentStage = StageContext.Stage.Design,
                    nextStage = StageContext.Stage.Design,
                    responseMsg = design.content,
                    resultType = UiPage::class.java.toString(),
                    result = Json.encodeToString(design)
                )
            }

            StageContext.Stage.Execute -> {
                // the layout should be the last - 1 message
                if (messages.size < 2) {
                    throw IllegalStateException("messages size should be greater than 2")
                }

                val layout = messages[messages.size - 2].content
                val uiPage = UiPage.parse(layout)
                val answer: Answer = FESolutionExecutor(contextBuilder, llmProvider, variableResolver).execute(uiPage)
                return WorkflowResult(
                    currentStage = StageContext.Stage.Execute,
                    nextStage = StageContext.Stage.Done,
                    responseMsg = answer.values.toString(),
                    resultType = String::class.java.toString(),
                    result = answer.toString()
                )
            }
            else -> {
                throw IllegalStateException("stage not supported")
            }
        }
    }

    companion object {
        val CLARIFY: StageContext = StageContext(
            id = "FrontendClarify",
            stage = StageContext.Stage.Clarify,
            questionPrefix = "输入：",
            systemPrompt = """你是一个专业的前端技术咨询师（Advisor），职责是帮助开发人员用户收集和分析需求。
            |- 你必须使用中文和用户沟通。
            |- 当用户问你问题时，你必须帮助用户明确他们的需求。
            |- 当用户问你是谁时，你必须回答：我是一个专业的前端技术专家，职责是帮助用户编写前端代码。
            |- 当用户的问题比较发散、不明确，请和用户沟通，收集更多的信息，帮助用户明确他们的需求。
            |---
            |
            |已有布局方式如下：
            |${'$'}{layouts}
            |---
            |
            |请严格用以下格式输出:
            |思考：是否包含了用户故事，页面元素的描述和布局信息，如果明确请结束询问，如果不明确，请继续询问。
            |行动：为"CONTINUE"或者"FINISH"
            |询问：想继续询问的问题
            |最终输出：完整的问题
            |思考-行动-询问可以循环数次，直到最终输出
            |
            |示例一：
            |输入：编写一个登录页面
            |输出：
            |思考：用户想要编写登录页面，但是没有说明页面元素的描述和布局信息，应该继续询问。
            |行动：CONTINUE
            |询问：您想要什么布局方式？
            |
            |示例二：
            |输入：编写一个栅格布局的登录页面，需要包含用户名、密码输入框和登录按钮
            |输出：
            |思考：用户想要编写栅格布局的登录页面，其中栅格布局是布局方式，登录页面是用户故事，页面元素包含用户名、密码输入框和登录按钮，已经明确，应该结束询问。
            |行动：FINISH
            |最终输出：编写一个栅格布局的登录页面
            |
            |以下是您和当前用户的交互历史：${'$'}{histories}
        """.trimMargin()
        )

        /**
         * UI 布局 DSL 基于： [https://github.com/phodal/design](https://github.com/phodal/design)
         */
        val DESIGN: StageContext = StageContext(
            id = "FrontendDesign",
            stage = StageContext.Stage.Design,
            systemPrompt = """你是一个专业的前端技术咨询师（Advisor），请以如下的 ASCII 描述用户所需要的页面。
            |
            |- 如果用户没有给出页面元素的描述，你必须自己补充。
            |- 你必须等待用户确认，确认后才能继续。
            | 
            |所有的组件列表如下：
            |###
            |${'$'}{components}
            |###
            |
            |如下是基本的 ASCII 规则，以便用户以程序解析它：
            |
            |- a(), p() 以小写字母开头的函数，表示页面元素
            |- Footer(10x),BlogList(10x) 以大写字母开头的函数，表示页面组件
            |- Empty(2x) 表示空白, 2x 表示页面元素的宽度为 2x栅格宽度
            |- NavComponent(10x) 表示导航栏, 10x 表示页面元素的宽度为 10x栅格宽度
            |- 以 Component 结尾，表示是一个新的页面组件，如 NavComponent, BlogListComponent 等
            |
        """.trimMargin(),
            updateExamples = listOf(
                QAUpdateExample(
                    question = "生成一个导航栏组件的 mockup",
                    answer = """请确认以下的设计是否符合您的要求。如果符合，请回复"YES"，如果不符合，请提出你的要求。
            |```design
            |componentName: NavComponent
            |usedComponents: Link, Button
            |--------------------------------------
            || Link("home") | Link("博客") | Button("Login")  |
            |--------------------------------------
            |```
                """.trimMargin(),
                    nextAction = "这里的 login 应该是 button，而不是 a",
                    finalOutput = """
            |```design
            |componentName: NavComponent
            |usedComponents: Link, Button
            |--------------------------------------
            || Link("home") | Link("博客") | Button("Login")  |
            |--------------------------------------
            |```
                """.trimMargin()
                ),
                QAUpdateExample(
                    question = "生成一个包含图片的博客列表面 mockup",
                    answer = """请确认以下的设计是否符合您的要求。如果符合，请回复"YES"，如果不符合，请提出你的要求。
            |```design
            |pageName: 博客列表
            |usedComponents: NavComponent, Text, BlogListComponent, ArchivesComponent, Footer
            |------------------------------------------------------
            ||      NavComponent(10x)                             |
            |------------------------------------------------------
            || Empty(2x) | Text(6x)           | Empty(2x)         |
            |------------------------------------------------------
            || BlogListComponent(8x)     | ArchivesComponent(2x)  |
            |------------------------------------------------------
            || Footer(10x)                                        |
            |------------------------------------------------------
            |```
                """.trimMargin(),
                    nextAction = "YES",
                )
            )
        )
        val EXECUTE: StageContext = StageContext(
            id = "FrontendExecute",
            stage = StageContext.Stage.Execute,
            systemPrompt = """你是一个资深的前端开发人员，帮助编写用户设计好的前端 UI。你需要根据下面的需求和页面，生成对应的前端代码。
            |- 项目的技术栈是 Vue + TypeScript + Element UI。
            |
            |###
            |请根据用户提供的问题，生成前端代码。
            |###
            |
            |相关的组件列表如下：
            |${'$'}{userComponents}
            |
            |用户提供的问题：
            |${'$'}{question}
            |
            |页面布局要求：
            |${'$'}{userLayout}
            |
            |现在请你生成前端代码，代码使用 Markdown 语言编写，以便用户可以直接复制到项目中。
            |
        """.trimMargin()
        )
    }
}