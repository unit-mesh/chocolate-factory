---
layout: default
title: Chocolate Factory
nav_order: 1
permalink: /
---

> Chocolate Factory 是一款开源的 AI Agent 应用引擎/应用框架，旨在帮助您轻松打造强大的 SDLC + LLM 生成助手。无论您是需要生成前端页面、后端
> API、SQL 图表，还是测试用例数据，Chocolate Factory 都能满足您的需求。

## 关键概念：领域驱动的问题求解

Chocolate Factory 是基于领域驱动设计思想的，如下图所示：

![Chocolate Factory Concepts](chocolate-factory.svg)

> 软件系统的构建实则是对问题空间的求解，以此获得构建解空间的设计方案。 —— 《解构领域驱动设计》

在领域驱动设计（Domain-Driven Design，简称DDD）中，问题空间（Problem Space）和解空间（Solution
Space）是两个关键的概念，用于帮助开发团队理解和建模复杂的领域问题以及设计相应的软件解决方案：（由 ChatGPT 生成）

- 问题空间（Problem Space） 问题空间是指领域中实际的业务问题、需求和挑战，它关注的是业务领域的本质问题和业务需求。在问题空间中，团队与领域专家合作，收集和理解领域的业务规则、流程、概念以及业务需求。
- 解空间（Solution Space）是指基于问题空间的理解所提出的软件解决方案，包括软件架构、设计模式、编码实现等。在解空间中，开发团队将问题空间的概念和需求映射到具体的代码和技术实现上。

DDD 强调问题空间和解空间之间的分离，这意味着在开始开发之前，团队应该首先深入了解问题空间，与领域专家合作，确保对领域的理解是准确的。然后，他们可以将这个理解映射到解空间，设计和实现相应的软件系统。

## Stage 设计

根据上面的思想，我们设计了五个 Stage：

- ProblemClarifier.kt：根据用户提供的文本来澄清问题，以确保准确理解用户的需求。问题澄清器的任务是分析用户的输入，提取关键信息，可能要求用户提供更多详细信息以确保问题清晰，并将清晰的问题描述传递给下一步。
- ProblemAnalyzer.kt：可以进一步分析问题，将问题细化成更具体的子问题或任务，以帮助解决方案的设计和执行。
- SolutionDesigner.kt：根据经过问题澄清和分析后的用户需求，设计一个解决方案。解决方案设计师的任务是将问题领域的需求转化为可执行的计划或设计，确保解决方案符合用户的期望和要求。
- SolutionReviewer.kt：负责审核已经设计好的解决方案，以确保其质量、安全性和合规性。审核员可能会对解决方案进行测试和评估，并提出建议或修改，以满足特定标准和要求。
- SolutionExecutor.kt：负责实际执行解决方案的步骤，根据设计好的计划来生成结果。根据具体情况，解决方案执行者的任务可以包括编程、自动化、生成文档等。

在不同的领域里，会根据场景不同，而有所取舍。

## Dev

Tech Stack:

- [Spring Boot](https://spring.io/projects/spring-boot) is a framework for building web applications.
- [Kotlin](https://kotlinlang.org/) is a modern programming language that makes developers happier.

To spike:

- [Kotlin Jupyter](https://github.com/Kotlin/kotlin-jupyter)  Kotlin kernel for Jupyter/IPython.
- [Kotlin Dataframe](https://github.com/Kotlin/dataframe) is typesafe in-memory structured data processing for JVM.
- [KInference](https://github.com/JetBrains-Research/kinference) is a library that makes it possible to execute complex
  ML models (written via ONNX) in Kotlin.

## 项目结构

- cocoa-core, 核心模块，包含了核心的流程控制、领域模型、领域流程等
- cocoa-local-embedding，本地向量化模块，包含了本地向量化的实现，如 Sentence-Transformers 等。
- code-interpreter，代码解释器，包含了代码解释器的实现，如 Kotlin Jupyter 等。
- dsl
    - design，Design DSL 的解析器 