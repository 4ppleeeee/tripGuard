---
# 注意不要修改本文头文件，如修改，CodeBuddy（内网版）将按照默认逻辑设置
type: always
---
---

## 执行流程

### 1️⃣ 判断执行模式

**先判断用户意图，再决定执行模式：**

- 用户只问"当前进度 / 下一步" → **状态模式**：只检查阶段、阻塞点和下一步，不创建团队。
- 用户要求"继续 / 按流程推进 / 全流程协同开发 / 多 agent 协作" → **团队模式**：创建 team，由协调 agent 派发角色任务。

### 2️⃣ 先读取 / 补齐进度表

在任何“继续执行”动作开始前，必须先定位对应需求的进度表：

- 新建模式：`docs/component/{模块}/{页面驼峰}/{页面下划线}_progress.md`
- 迭代模式：`docs/component/{模块}/{页面驼峰}/diff/{页面下划线}_progress_diff.md`

执行规则：
- 若进度表存在，先读取它，确认最近一次已完成步骤、当前阻塞点、备注和下一步。
- 若进度表不存在，按 `ai-coding-workflow` 中的产物清单回填创建进度表，再继续。
- 若进度表与真实产物、编译结果或测试证据冲突，以真实证据为准，并同步修正进度表。
- 每个步骤完成后，必须先更新进度表，再决定是否自动推进下一步。

### 3️⃣ 判断进度

按照 `ai-coding-workflow` skill 中“**进度表优先 + 真实产物校验**”的规则，确定当前已完成到哪一步。

**同时判断新建 / 迭代模式**：检查对应页面目录下是否存在 `diff/` 子目录且包含 `_diff.md`：
- 无 diff 目录 → 🆕 新建模式
- 有 diff 目录 → ✏️ 迭代模式

### 4️⃣ 状态模式输出

若为状态模式，输出以下格式后停止：

```
📌 当前模式：{新建模式 / 迭代模式}
📌 当前进度：Step {N} 已完成 / 当前阻塞在 Step {N}
🗂️ 进度表：{progress-path}
👥 当前编排：状态模式
📋 下一步：Step {N+1} — {名称}
🧩 负责人：{角色 agent 名称}
🛠️ 建议调用：{skill-name 或人工步骤}
💡 推荐 Prompt："{可直接复用的 prompt}"
```

### 5️⃣ 团队模式：创建 Team 并编排任务

#### 5.1 创建 Team

```
team_create(team_name = "ai-coding-struct-{模块}-{页面}")

成员列表：
workflow-orchestrator → bmad-orchestrator / .codebuddy/agents/bmad-orchestrator.md
requirements-owner    → bmad-po           / .codebuddy/agents/bmad-po.md
protocol-architect    → bmad-architect    / .codebuddy/agents/bmad-architect.md
report-architect      → bmad-architect    / .codebuddy/agents/bmad-architect.md
solution-architect    → bmad-architect    / .codebuddy/agents/bmad-architect.md
# Step 6~9 由主 Agent 直接执行，不创建 sub-agent 成员
quality-engineer-A    → bmad-qa           / .codebuddy/agents/bmad-qa.md
quality-engineer-B    → bmad-qa           / .codebuddy/agents/bmad-qa.md
quality-engineer-C    → bmad-qa           / .codebuddy/agents/bmad-qa.md
quality-engineer-D    → bmad-qa           / .codebuddy/agents/bmad-qa.md
workflow-reviewer     → bmad-review       / .codebuddy/agents/bmad-review.md（按需创建）
```

> `workflow-orchestrator` 必须最先创建；Step 6~9 由主 Agent 直接执行，禁止为这四个步骤创建或派发 sub-agent。
>
> **通用约束**：每次派发下一步骤前先重新读取进度表；每个步骤完成后先回写进度表，再继续推进。

#### 5.2 Step 1 — 需求分析

```
task(
  subagent_name = "bmad-po",
  subagent_path = ".codebuddy/agents/bmad-po.md",
  name = "requirements-owner",
  team_name = "ai-coding-struct-{模块}-{页面}",
  description = "Step 1 需求分析",
  prompt = "分析以下需求并产出需求文档。输入：{TAPD链接 或 需求文档路径}。请使用 analyze-tapd-story skill 完成。新建页面输出到 docs/component/{模块}/{页面驼峰}/{页面下划线}.md，已有页面输出到 docs/component/{模块}/{页面驼峰}/diff/{页面下划线}_diff.md。",
  mode = "acceptEdits",
  max_turns = 30
)
```

> **Step 2 人工关卡**：Step 1 完成后，更新进度表并停下提示用户进行需求评审，确认后再继续。

#### 5.3 并行组 A — Step 3 + Step 4（需求评审通过后同时派发）

```
# 同时发出以下两个 task 调用：

task(
  subagent_name = "bmad-architect",
  subagent_path = ".codebuddy/agents/bmad-architect.md",
  name = "protocol-architect",
  team_name = "ai-coding-struct-{模块}-{页面}",
  description = "Step 3 接口协议设计",
  prompt = "根据需求文档设计接口协议。需求文档：docs/component/{模块}/{页面驼峰}/{页面下划线}.md{迭代模式追加：+ diff/{页面下划线}_diff.md}。请使用 design-api-protocol skill 完成。输出：docs/component/{模块}/{页面驼峰}/{页面下划线}_protocol.md（迭代模式：diff/{页面下划线}_protocol_diff.md，无变化则写「保持原样」）。注意：本页面使用 Struct（品字形）架构，DataRepo 直接发起 PB 请求，无需设计标准 REST 接口层。",
  mode = "plan",
  max_turns = 30
)

task(
  subagent_name = "bmad-architect",
  subagent_path = ".codebuddy/agents/bmad-architect.md",
  name = "report-architect",
  team_name = "ai-coding-struct-{模块}-{页面}",
  description = "Step 4 上报需求分析",
  prompt = "分析上报需求。需求文档：docs/component/{模块}/{页面驼峰}/{页面下划线}.md{迭代模式追加：+ diff/{页面下划线}_diff.md}。请使用 analyze-report-document skill 完成。输出：docs/component/{模块}/{页面驼峰}/{页面下划线}_report.md（迭代模式：diff/{页面下划线}_report_diff.md，无变化则写「保持原样」）。",
  mode = "plan",
  max_turns = 30
)
```

#### 5.4 Step 5 — 技术方案设计（Step 3 + 4 全部完成后）

```
task(
  subagent_name = "bmad-architect",
  subagent_path = ".codebuddy/agents/bmad-architect.md",
  name = "solution-architect",
  team_name = "ai-coding-struct-{模块}-{页面}",
  description = "Step 5 技术方案设计",
  prompt = "根据需求文档、接口协议和上报文档设计技术方案。需求文档：docs/component/{模块}/{页面驼峰}/{页面下划线}.md。协议文档：docs/component/{模块}/{页面驼峰}/{页面下划线}_protocol.md。上报文档：docs/component/{模块}/{页面驼峰}/{页面下划线}_report.md。{迭代模式追加对应 diff 文档路径}。请使用 design-tech-solution skill 完成。输出：docs/component/{模块}/{页面驼峰}/{页面下划线}_tech_solution.md（迭代模式：diff/{页面下划线}_tech_solution_diff.md）。\n\n⚠️ 架构约束：本页面使用 Struct（品字形）架构，技术方案必须明确以下内容：\n1. PageArgs / PageWidget / DataRepo 的职责划分\n2. 是否需要自定义 PageViewModel（StructComposePage vs StructComposePage4VM 选型）\n3. CellVM 接口设计与 FeedsVMItem 封装\n4. DataRepo 模式选型（SuspendRepo / LocalRepo / 带 CellVM 工厂的 SuspendRepo）\n5. 注册链路（路由 Key / Service 接口 / CellRegistry / TitleBar/Header Widget 注册）\n6. 日志与可观测性章节（模块日志入口、按层落点、关键节点清单）\n\n产出后停下来等待用户确认是否继续编码。",
  mode = "plan",
  max_turns = 40
)
```

> **Step 5 人工关卡**：技术方案产出后，`workflow-orchestrator` 必须先更新进度表，再向用户汇总技术方案关键结论并停下等待确认，**不得自动进入编码阶段**。

#### 5.5 并行组 B — Step 6 + 7 + 8（用户确认继续编码后，主 Agent 直接并行执行）

> ⚠️ **Step 6~9 由主 Agent 直接执行，禁止派发 sub-agent。**

```
# 主 Agent 同时调用以下三个 skill（不通过 task 派发）：

[主 Agent] use_skill("restore-ui-design")
  输入：
    - 技术方案：docs/component/{模块}/{页面驼峰}/{页面下划线}_tech_solution.md
    - 需求文档：docs/component/{模块}/{页面驼峰}/{页面下划线}.md
    - Figma 设计稿：{url}（如未提供，向用户索要）
    - 架构约束：Struct 品字形，Cell 视图放 wsCompose/.../cell/，Page 入口使用 StructComposePage 或 StructComposePage4VM

[主 Agent] use_skill("generate-mock-data")
  输入：
    - 技术方案：docs/component/{模块}/{页面驼峰}/{页面下划线}_tech_solution.md
    - 协议文档：docs/component/{模块}/{页面驼峰}/{页面下划线}_protocol.md
    - 架构约束：Mock 数据放 ws{Module}/.../mock/{Feature}MockData.kt，供 DataRepo useMock 模式使用

[主 Agent] use_skill("implement-api-layer")
  输入：
    - 技术方案：docs/component/{模块}/{页面驼峰}/{页面下划线}_tech_solution.md
    - 协议文档：docs/component/{模块}/{页面驼峰}/{页面下划线}_protocol.md
    - 架构约束：Struct 架构中接口层即 DataRepo（IStructDataSuspendRepo），PB 请求在 DataRepo 内直接发起，无独立 Repository 层（除非技术方案明确需要）
```

#### 5.6 Step 9 — VM 实现编码（Step 6 + 8 完成后，主 Agent 直接执行）

```
[主 Agent] use_skill("implement-viewmodel")
  输入：
    - 技术方案：docs/component/{模块}/{页面驼峰}/{页面下划线}_tech_solution.md
    - 需求文档：docs/component/{模块}/{页面驼峰}/{页面下划线}.md
    - 已有 UI 代码（Step 6 产物）
    - 已有 DataRepo 代码（Step 8 产物）
    - 架构约束：
      * 若选型 StructComposePage：无需自定义 VM，DataRepo 直接创建 CellVM
      * 若选型 StructComposePage4VM：实现继承 StructPageViewModel 的自定义 VM，
        通过 Service 接口暴露 create{Feature}PageVM()，
        禁止定义 UiState / Action / dispatch() / UseCase 等 MVVM 概念
```

> **自动续跑**：Step 9 完成后，主 Agent 自动推进 Step 10 → Step 10.5 → Step 11 & 12（并行），直到 Step 13 人工关卡再停下；**每次续跑前都必须重新读取最新进度表**。

#### 5.7 Step 10 — 需求检查

```
task(
  subagent_name = "bmad-qa",
  subagent_path = ".codebuddy/agents/bmad-qa.md",
  name = "quality-engineer-A",
  team_name = "ai-coding-struct-{模块}-{页面}",
  description = "Step 10 需求检查",
  prompt = "对照需求文档和技术方案检查代码实现，输出结构化问题清单 JSON（含 severity / file / fix_suggestion）。需求文档：docs/component/{模块}/{页面驼峰}/{页面下划线}.md。技术方案：docs/component/{模块}/{页面驼峰}/{页面下划线}_tech_solution.md。{迭代模式追加对应 diff 文档路径}。请使用 check-requirements skill 完成。\n\n⚠️ Struct 架构专项检查：\n1. PageWidget / DataRepo / CellVM / FeedsVMItem 是否按技术方案实现\n2. 若有自定义 VM：是否继承 StructPageViewModel，是否通过 Service 接口暴露，是否无 UiState/Action/dispatch()\n3. Cell 注册是否完整（CellRegistry + WsFeedsItemCardService）\n4. TitleBar / Header / Hanging Widget 注册是否完整\n5. 路由 Key 是否已在 ComposeViewKey.kt 中注册\n6. 日志落地验收（对照技术方案「日志与可观测性」章节逐条检查）",
  mode = "acceptEdits",
  max_turns = 30
)
```

> Step 10 完成后，主 Agent 解析问题清单，提取 P0/P1 issues，更新进度表，再传给 Step 10.5。

#### 5.8 Step 10.5 — 编译验证与修复

```
task(
  subagent_name = "bmad-qa",
  subagent_path = ".codebuddy/agents/bmad-qa.md",
  name = "quality-engineer-B",
  team_name = "ai-coding-struct-{模块}-{页面}",
  description = "Step 10.5 编译验证与修复",
  prompt = "修复 P0/P1 问题并验证 androidApp 编译通过。输入：{主 Agent 汇总的 P0/P1 问题清单 JSON}。请使用 verify-build-integrity skill 完成。编译失败时停止并向主 Agent 汇报错误详情。",
  mode = "acceptEdits",
  max_turns = 40
)
```

> Step 10.5 完成后，主 Agent 汇总修复项与 VM 代码摘要，更新进度表后再**并行**派发 Step 11 / 12。编译失败时主 Agent 停止自动推进，向用户汇报并等待指示。

#### 5.9 并行组 C — Step 11 + 12（Step 10.5 编译通过后同时派发）

```
# 同时发出以下两个 task 调用：

task(
  subagent_name = "bmad-qa",
  subagent_path = ".codebuddy/agents/bmad-qa.md",
  name = "quality-engineer-C",
  team_name = "ai-coding-struct-{模块}-{页面}",
  description = "Step 11 单元测试",
  prompt = "为 ViewModel 生成单元测试。输入：{主 Agent 汇总的 VM 代码摘要（已修复的接口签名、状态机变更、关键文件路径）}。请使用 generate-unit-tests skill 完成。输出：commonTest/.../{Feature}ViewModelTest.kt。\n\n注意：若选型 StructComposePage（无自定义 VM），则重点测试 DataRepo 的数据转换逻辑和 CellVM 的状态流转。",
  mode = "acceptEdits",
  max_turns = 30
)

task(
  subagent_name = "bmad-qa",
  subagent_path = ".codebuddy/agents/bmad-qa.md",
  name = "quality-engineer-D",
  team_name = "ai-coding-struct-{模块}-{页面}",
  description = "Step 12 测试用例",
  prompt = "生成测试用例。输入：需求文档 docs/component/{模块}/{页面驼峰}/{页面下划线}.md + {主 Agent 汇总的问题清单摘要}。{迭代模式追加：diff 文档路径}。请使用 generate-test-cases skill 完成。输出：docs/testcase/{模块}/{页面下划线}_testcase.md（迭代模式：docs/component/{模块}/{页面驼峰}/diff/{页面下划线}_testcase_diff.md）。",
  mode = "acceptEdits",
  max_turns = 30
)
```

#### 5.10 Step 13 — 测试执行（人工关卡）

Step 12 完成后，更新进度表并停下向用户交接：

```
📋 Step 13 — 测试执行（人工）
📥 测试同学需要：
  - 测试用例：docs/testcase/{模块}/{页面下划线}_testcase.md
  - 单元测试：commonTest/.../{Feature}ViewModelTest.kt
  - 编译验证报告（Step 10.5 产物）
🔔 请测试同学执行测试，完成后提交测试报告与缺陷单。
```

### 6️⃣ 输出格式

每完成一个步骤后，输出进度摘要：

```
✅ 已完成：Step {N} — {名称}
📌 当前模式：{新建模式 / 迭代模式}
📌 当前进度：{已完成步骤概览}
🗂️ 进度表：{progress-path}
👥 当前编排：{状态模式 / 团队模式}（协调者：workflow-orchestrator）
📋 下一步：Step {N+1} — {名称}
🧩 负责人：{角色 agent 名称 或 主 Agent}
🛠️ 建议调用：{skill-name 或人工步骤}
```

若存在并行项，追加：

```
⚡ 可并行执行：
- Step {X} — {名称} → {角色 agent} / {skill-name}
- Step {Y} — {名称} → {角色 agent} / {skill-name}
```

若阻塞在 Step 5 后人工关卡：

```
⏸️ 等待用户确认：技术方案已产出，请确认是否继续进入编码阶段（Step 6/7/8）。
```
