package cc.unitmesh.cf.domains.frontend.prompt

import cc.unitmesh.cf.domains.frontend.context.FEWorkflow
import org.junit.jupiter.api.Test

class FEWorkflowTest {

    @Test
    fun should_return_pure_text_for_testing_in_gui() {
        println(FEWorkflow.CLARIFY.format())
        println(FEWorkflow.DESIGN.format())
        println(FEWorkflow.EXECUTE.format())
    }
}