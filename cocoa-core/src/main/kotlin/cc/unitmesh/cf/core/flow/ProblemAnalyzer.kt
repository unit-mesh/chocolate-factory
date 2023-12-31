package cc.unitmesh.cf.core.flow

import cc.unitmesh.cf.core.dsl.Dsl

typealias AnalyzeResult = Dsl;

interface ProblemAnalyzer {
    fun analyze(domain: String, question: String): Dsl
}
