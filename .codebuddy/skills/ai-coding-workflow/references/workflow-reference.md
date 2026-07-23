# Skill: AI Coding 全流程编排（多 Agent 版）

## 目标
把原来的"轻量级路由表"升级为"显式多 Agent Team 编排"：
- 先识别当前进度与模式
- 再由协调 agent 创建 / 维护角色团队
- 按步骤门禁推进各阶段
- 在人工关卡前停住
- 持续向用户汇报状态与下一步

**本编排负责协调，不把所有工作塞给单个 Agent。** 需求、架构、开发、QA、评审等工作分别交给对应角色 agent 或下游 skill。

---

## 工作模式

### 状态模式（只导航）

**触发条件：**
- 用户只问"当前进度""下一步做什么"
- 或当前上下文不适合创建 team

**行为：**
- 先读取进度表，再用真实产物做校验
- 输出当前阶段、阻塞点、下一步、建议角色 / skill
- 不创建任何成员

### 团队模式（导航 + 推进）

**触发条件：**
- 用户说"继续""按流程推进""从需求到开发""全流程协同开发"
- 或明确要求多 agent / 多角色协同

**行为：**
- 创建 / 恢复 team
- 由协调 agent 派发角色任务
- 并行推进可并行步骤
- 汇总进展，遇到人工关卡停止自动推进

---

## 模式判断：新建页面 vs 旧页面迭代

**在执行任何步骤之前，先判断当前需求属于哪种模式：**

```
CHECK: 对应页面目录（docs/component/{模块名}/{页面名驼峰}/）下是否存在 diff/ 子目录，
       且其中包含 {页面名下划线}_diff.md？
  ├── 全部页面均无 diff 目录 → 🆕 新建模式（全量流程）
  ├── 全部页面均有 diff 目录 → ✏️ 迭代模式（diff 流程）
  └── 部分有 diff 目录、部分没有 → 🔀 混合模式（按页面分别处理）
```

> **说明**：`analyze-tapd-story` 执行完成后，新建页面的文档保存在页面目录根目录，已有页面的 diff 文档保存在 `diff/` 子目录。后续所有 skill 均通过检查 `diff/` 目录是否存在来自动判断模式，无需用户手动指定。

> **迭代模式核心原则**：
> - 所有 skill 以「基线文档 + diff 文档」结合来理解完整需求
> - 协议 / 上报 / VM 接口若无变化，在 diff 文档中写「保持原样」，不重新生成
> - 代码层面只修改 diff 涉及的部分，不重新实现整个页面

> **星码平台协同**：产品和测试同学可通过星码平台（带 CLI Agent 的云工作台）直接修改仓库中的需求文档、测试用例等文档类产物并提交 MR。
> - 需求评审阶段：产品可直接在星码平台修改 `docs/component/` 下的需求文档，提交 MR 由研发 Review 合入
> - 测试用例阶段：测试可直接在星码平台补充 / 修改 `_testcase.md` 或 `diff/_testcase_diff.md`，提交 MR 由研发 Review 合入
> - 文档与代码同仓库管理，所有角色均可参与文档协作，真正实现跨角色协同，且变更历史完整可追溯

---

## 需求级进度表（强制）

### 路径约定

- 新建模式：`docs/component/{模块}/{页面驼峰}/{页面下划线}_progress.md`
- 迭代模式：`docs/component/{模块}/{页面驼峰}/diff/{页面下划线}_progress_diff.md`
- 混合模式：每个页面各自维护一份进度表，禁止多个页面共用一张表

### 使用规则

1. 一旦能确定本次需求对应的 `模块 + 页面`，就立即创建或读取进度表。
2. 每次进入“下一步骤”、自动续跑、或执行 `@.codebuddy/commands/ws.dev.continue.md` 前，必须先读取进度表。
3. 若进度表不存在，先按下方「1️⃣ 判断进度」中的产物清单回填创建，再继续执行。
4. 若进度表与真实产物 / 编译 / 测试证据冲突，以真实证据为准，并立即修正进度表。
5. 每个步骤完成后，先更新进度表，再向用户汇总或派发后续步骤。
6. 并行组中的步骤逐项更新；只有相关步骤都已标记完成，才允许进入后续串行步骤。

### 状态取值

- `⬜ 未开始`
- `🟨 进行中`
- `✅ 已完成`
- `⏸️ 等待人工`
- `🚫 不适用`

### 推荐表头与模板

| Step | 名称 | Owner | 状态 | 产物 / 证据 | 更新时间 | 备注 |
|------|------|------|------|-------------|----------|------|
| 1 | 需求分析 | `requirements-owner` | `⬜ 未开始` | `docs/component/{模块}/{页面驼峰}/{页面下划线}.md` 或 `diff/{页面下划线}_diff.md` | - | - |
| 2 | 需求评审 | 人工 | `⬜ 未开始` | 评审结论 / MR / 评论链接 | - | - |
| 3 | 接口协议设计 | `protocol-architect` | `⬜ 未开始` | `_protocol.md` / `diff/_protocol_diff.md` | - | - |
| 4 | 上报需求分析 | `report-architect` | `⬜ 未开始` | `_report.md` / `diff/_report_diff.md` | - | - |
| 5 | 技术方案设计 | `solution-architect` | `⬜ 未开始` | `_tech_solution.md` / `diff/_tech_solution_diff.md` | - | 产出后若待用户确认，状态改为 `⏸️ 等待人工` |
| 6 | 设计稿还原 | 主 Agent | `⬜ 未开始` | UI 改动文件路径 | - | - |
| 7 | Mock 数据 | 主 Agent | `⬜ 未开始` | Mock 文件路径 | - | - |
| 8 | 接口开发 | 主 Agent | `⬜ 未开始` | Repository / DataRepo 文件路径 | - | - |
| 9 | VM 实现编码 | 主 Agent | `⬜ 未开始` | `*ViewModel.kt` 等实现文件 | - | - |
| 10 | 需求检查 | `quality-engineer-A` | `⬜ 未开始` | 结构化问题清单 JSON | - | - |
| 10.5 | 编译验证与修复 | `quality-engineer-B` | `⬜ 未开始` | 编译结果 / 修复记录 | - | - |
| 11 | 单元测试 | `quality-engineer-C` | `⬜ 未开始` | `*ViewModelTest.kt` | - | - |
| 12 | 测试用例 | `quality-engineer-D` | `⬜ 未开始` | `_testcase.md` / `diff/_testcase_diff.md` | - | - |
| 12.5 | 代码质量打分 | `workflow-judge` | `⬜ 未开始` | `_review_score.md` / `diff/_review_score_diff.md` | - | - |
| 13 | 测试执行 | 人工 | `⬜ 未开始` | 测试报告 / 缺陷单 | - | - |
| 14 | 工作流回顾 | `workflow-retrospector` | `⬜ 未开始` | `_retrospective.md` / `diff/_retrospective_diff.md` | - | - |

> **要求**：进度表必须覆盖 Step 1 ~ 14 全部行；Step 2 / Step 13 这类人工步骤也必须占位。若某一步对当前需求不适用，状态写 `🚫 不适用`，并在备注中说明原因。

---

## 多 Agent 角色拓扑

| 角色名 | subagent_name | subagent_path | 主要职责 | 对应步骤 |
|------|------|------|------|------|
| `workflow-orchestrator` | `bmad-orchestrator` | `.codebuddy/agents/bmad-orchestrator.md` | 协调、拆解、派发、门禁、汇总、清理 | 全流程 |
| `requirements-owner` | `bmad-po` | `.codebuddy/agents/bmad-po.md` | 需求分析、需求文档准备、评审输入收口 | Step 1-2 |
| `protocol-architect` | `bmad-architect` | `.codebuddy/agents/bmad-architect.md` | 接口协议设计 | Step 3 |
| `report-architect` | `bmad-architect` | `.codebuddy/agents/bmad-architect.md` | 上报需求分析 | Step 4 |
| `solution-architect` | `bmad-architect` | `.codebuddy/agents/bmad-architect.md` | 技术方案设计 | Step 5 |
| **主 Agent** | — | — | 设计稿还原 / UI 改造（直接执行，不派发 sub-agent） | Step 6 |
| **主 Agent** | — | — | Mock 数据（直接执行，不派发 sub-agent） | Step 7 |
| **主 Agent** | — | — | 接口层开发（直接执行，不派发 sub-agent） | Step 8 |
| **主 Agent** | — | — | ViewModel 实现收口（直接执行，不派发 sub-agent） | Step 9 |
| `quality-engineer-A` | `bmad-qa` | `.codebuddy/agents/bmad-qa.md` | 需求检查，输出结构化问题清单 | Step 10 |
| `quality-engineer-B` | `bmad-qa` | `.codebuddy/agents/bmad-qa.md` | 编译验证与 P0/P1 问题修复，输入：主 Agent 汇总的问题清单 | Step 10.5 |
| `quality-engineer-C` | `bmad-qa` | `.codebuddy/agents/bmad-qa.md` | 单元测试生成，输入：主 Agent 汇总的 VM 代码摘要 | Step 11 |
| `quality-engineer-D` | `bmad-qa` | `.codebuddy/agents/bmad-qa.md` | 测试用例生成，输入：需求文档 + 主 Agent 汇总的问题清单 | Step 12 |
| `workflow-reviewer` | `bmad-review` | `.codebuddy/agents/bmad-review.md` | 独立复核、风险兜底 | 可选，关键节点启用 |
| `workflow-judge` | `bmad-review` | `.codebuddy/agents/bmad-review.md` | **裁判角色**：对最终生成的代码做结构化打分（需求合规性 / 架构合规性 / Struct 规范合规性 / UI 合规性 / 代码质量 / 测试覆盖 / 日志可观测性），输出总分与维度分 | Step 12.5 |
| `workflow-retrospector` | `bmad-orchestrator` | `.codebuddy/agents/bmad-orchestrator.md` | **回顾角色**：引导用户回顾本次工作流的踩坑点、亮点与改进项，沉淀到单次回顾文档，并同步更新改进台账 | Step 14 |

> `workflow-orchestrator` 是唯一允许直接对用户汇总全局状态的角色。其它角色只对协调 agent 回报，不直接宣称全流程完成。
>
> **质量阶段（Step 10~12.5）主 Agent 协调职责**：
> - Step 10 完成后，主 Agent 解析 `quality-engineer-A` 输出的结构化问题清单，按 severity 分组汇总，将 P0/P1 问题列表 + `next_step_context` 传给 `quality-engineer-B`
> - Step 10.5 完成后，主 Agent 解析修复结果，提取 VM 代码摘要（已修复的接口签名、状态机变更），**并行**派发 `quality-engineer-C`（单元测试）和 `quality-engineer-D`（测试用例）
> - Step 11 + 12 全部完成后，主 Agent 派发 `workflow-judge` 执行 Step 12.5 代码质量打分，输入：需求文档 / 技术方案 / 本次改动的核心代码路径（VM / Repository / UI / 测试）
> - 若 Step 10.5 编译失败，主 Agent 停止自动推进，向用户汇报编译错误并等待指示
> - 若 Step 12.5 总分低于**阈值 70 分**或存在任一维度 < 50 分，主 Agent 不得宣称"全流程完成"，必须将打分报告汇总给用户并等待人工判断是否返工

### subAgent 调用规范

在团队模式下，协调 agent 派发任务时**必须通过 `task` 工具调用 subAgent**，不得在主 agent 内直接替代角色工作。调用时需严格指定以下参数：

| 参数 | 说明 | 是否必填 |
|------|------|---------|
| `subagent_name` | 上表中的 `subagent_name` 列值，如 `bmad-dev` | ✅ 必填 |
| `subagent_path` | 上表中的 `subagent_path` 列值（项目级 agent 定义文件路径） | ✅ 必填（同名 agent 有多个来源时用于消歧） |
| `name` | 角色名，即上表中的「角色名」列值，如 `ui-developer` | ✅ 必填（开启 Team 异步模式） |
| `team_name` | 当前 team 名称，格式 `ai-coding-{模块}-{页面}` | ✅ 团队模式必填 |
| `prompt` | 完整的任务描述，包含输入产物路径、输出预期、约束条件 | ✅ 必填 |
| `description` | 3-5 个词的任务摘要 | ✅ 必填 |
| `mode` | 推荐 `"acceptEdits"`（允许自动写文件）；高风险步骤可用 `"plan"` | 按需 |
| `max_turns` | 防止 agent 无限循环，推荐 20-50 | 按需 |

**调用示例（团队模式下派发 Step 6 UI 还原）：**

```
task(
  subagent_name = "bmad-dev",
  subagent_path = ".codebuddy/agents/bmad-dev.md",
  name = "ui-developer",
  team_name = "ai-coding-user-profilePage",
  description = "Step 6 UI 还原",
  prompt = "根据设计稿还原 UI。Figma: {url}。需求文档: docs/component/user/profilePage/profile_page.md。技术方案: docs/component/user/profilePage/profile_page_tech_solution.md。请使用 restore-ui-design skill 完成 UI 还原。",
  mode = "acceptEdits",
  max_turns = 30
)
```

**调用示例（团队模式下派发 Step 3 协议设计）：**

```
task(
  subagent_name = "bmad-architect",
  subagent_path = ".codebuddy/agents/bmad-architect.md",
  name = "protocol-architect",
  team_name = "ai-coding-user-profilePage",
  description = "Step 3 协议设计",
  prompt = "根据 docs/component/user/profilePage/profile_page.md 设计接口协议。请使用 design-api-protocol skill 完成。输出: docs/component/user/profilePage/profile_page_protocol.md",
  mode = "plan",
  max_turns = 30
)
```

**调用示例（状态模式 / 单次同步调用，不传 name 和 team_name）：**

```
task(
  subagent_name = "bmad-qa",
  subagent_path = ".codebuddy/agents/bmad-qa.md",
  description = "Step 10 需求检查",
  prompt = "对照 docs/component/user/profilePage/profile_page.md 和技术方案检查代码实现。请使用 check-requirements skill 完成。",
  mode = "acceptEdits",
  max_turns = 30
)
```

> **注意**：
> - 同一个 `subagent_name`（如 `bmad-dev`）可被多个角色复用，通过 `name` 参数区分角色身份
> - 并行组中的多个 task 调用可在同一轮消息中并发发出
> - 状态模式下不创建 team，此时不传 `name` 和 `team_name`，task 以同步方式执行
> - 每个 task 的 `prompt` 必须包含完整的输入产物路径和预期输出，不要依赖 agent 的上下文记忆

---

## 路由表

| Step | 名称 | Owner Agent | Skill 文件 | 新建模式前置产物 | 迭代模式前置产物 | 输出产物 | 并行组 |
|------|------|-----------|-----------|--------------|--------------|---------|--------|
| 1 | 需求分析 | `requirements-owner` | `analyze-tapd-story` | TAPD 链接 | TAPD 链接 | 新建：`{页面下划线}.md`<br>迭代：`diff/{页面下划线}_diff.md` | — |
| 2 | 需求评审 | **人工** | **人工** | Step 1 产物 + 设计稿 | Step 1 产物 + 设计稿 | 需求文档提交仓库 | — |
| 3 | 接口协议设计 | `protocol-architect` | `design-api-protocol` | Step 2 ✅ | Step 2 ✅ + 基线 `_protocol.md` | 新建：`_protocol.md`<br>迭代：`diff/_protocol_diff.md`（无变化则写「保持原样」） | **A** |
| 4 | 上报需求分析 | `report-architect` | `analyze-report-document` | Step 2 ✅ | Step 2 ✅ + 基线 `_report.md` | 新建：`_report.md`<br>迭代：`diff/_report_diff.md`（无变化则写「保持原样」） | **A** |
| 5 | 技术方案设计 | `solution-architect` | `design-tech-solution` | Step 3 + 4 全✅ | Step 3 + 4 全✅ + 基线技术方案文档 | 新建：`_tech_solution.md`<br>迭代：`diff/_tech_solution_diff.md`<br>**必须包含「日志与可观测性」章节**（见下方说明）<br>人工关卡：产出后需停下等待用户确认是否继续编码 | — |
| 6 | 设计稿还原 | **主 Agent** | `restore-ui-design` | Step 5 ✅ + 用户确认继续编码 | Step 5 ✅ + 用户确认继续编码 + 已有 UI 代码 | 新建：`ui/*.kt`<br>迭代：只修改 diff 涉及的 UI 组件 | **B** |
| 7 | Mock 数据 | **主 Agent** | `generate-mock-data` | Step 5 ✅ + 用户确认继续编码 | Step 5 ✅ + 用户确认继续编码（含 diff） | `mock/*.kt` | **B** |
| 8 | 接口开发 | **主 Agent** | `implement-api-layer` | Step 5 ✅ + 用户确认继续编码 | Step 5 ✅ + 用户确认继续编码（含 diff） | 新建：`repository/*.kt`<br>迭代：只新增 / 修改变更接口 | **B** |
| 9 | VM 实现编码 | **主 Agent** | `implement-viewmodel` | Step 5 ✅ + 用户确认继续编码 + 6 + 8 全✅ | Step 5 ✅ + 用户确认继续编码 + 6 + 8 全✅ + 已有 VM 实现 | 新建：`*ViewModel.kt` + `usecase/*.kt`<br>迭代：只修改 diff 涉及的 UseCase / Action | — |
| 10 | 需求检查 | `quality-engineer-A` | `check-requirements` | Step 9 ✅ | Step 9 ✅ | 结构化问题清单 JSON（含 severity / file / fix_suggestion）<br>**必须包含日志落地验收**（见下方说明）<br>→ 主 Agent 汇总后传给 Step 10.5 | — |
| 10.5 | 编译验证与修复 | `quality-engineer-B` | `verify-build-integrity` | Step 10 ✅ + 主 Agent 汇总的问题清单 | Step 10 ✅ + 主 Agent 汇总的问题清单 | UI 覆盖率报告 + 编译通过确认 + 修复项列表<br>→ 主 Agent 汇总后**并行**派发 Step 11 / 12 | — |
| 11 | 单元测试 | `quality-engineer-C` | `generate-unit-tests` | Step 10.5 ✅ + 主 Agent 汇总的 VM 代码摘要 | Step 10.5 ✅ + 主 Agent 汇总的 VM 代码摘要 | `*ViewModelTest.kt` | **C** |
| 12 | 测试用例 | `quality-engineer-D` | `generate-test-cases` | Step 10.5 ✅ + 需求文档 + 主 Agent 汇总的问题清单 | Step 10.5 ✅ + 需求文档 + 主 Agent 汇总的问题清单 | `docs/testcase/{模块}/{页面}_testcase.md` | **C** |
| 12.5 | 代码质量打分 | `workflow-judge` | — （直接基于 agent 定义执行） | Step 11 + 12 全✅ | Step 11 + 12 全✅ | `docs/component/{模块}/{页面驼峰}/{页面下划线}_review_score.md`<br>结构化评分报告（总分 + 维度分 + 风险点） | — |
| 13 | 测试执行 | **人工** | **人工** | Step 12.5 产物 | Step 12.5 产物 | 测试报告 + 缺陷单 | — |
| 14 | 工作流回顾 | `workflow-retrospector` + **用户**主导 | — （基于 `docs/本地知识库/工作流回顾/_retrospective_template.md`） | Step 13 结束或用户主动触发 | Step 13 结束或用户主动触发 | 新建：`docs/component/{模块}/{页面驼峰}/{页面下划线}_retrospective.md`<br>迭代：`docs/component/{模块}/{页面驼峰}/diff/{页面下划线}_retrospective_diff.md`<br>并同步更新 `docs/本地知识库/工作流回顾/improvement_backlog.md` | — |

> **并行组 A**：Step 3 / 4 需求评审通过后可同时启动  
> **并行组 B**：Step 6 / 7 / 8 在 Step 5 完成并获用户确认继续编码后可同时启动  
> **并行组 C**：Step 11 / 12 在 Step 10.5 编译通过后由主 Agent 同时派发  
> **自动推进规则**：当 Step 6 / 7 / 8 全部完成后，协调 agent 应自动继续执行后续步骤（Step 9 → Step 10 → Step 10.5 → Step 11 & 12 并行 → Step 12.5 代码打分），直到遇到新的阻塞点或 Step 13 人工测试关卡，再向用户汇总并停下等待。
> **质量阶段主 Agent 汇总门禁**：Step 10 → 主 Agent 汇总问题清单 → Step 10.5 → 主 Agent 汇总修复项 → 并行 Step 11 / 12；每个汇总节点主 Agent 必须解析 sub-agent 的结构化输出，不得直接透传原始文本。

---

## 依赖图

```
Step 1 → Step 2(人工)
              ├── Step 3 ──┐
              └── Step 4 ──┴── Step 5(bmad-architect 产出技术方案)
                                   ↓
                            用户确认继续编码
                                   ↓
                         ┌── Step 6(主 Agent: UI 还原) ──┐
                         ├── Step 7(主 Agent: Mock 数据)  │
                         └── Step 8(主 Agent: 接口层) ─────┴── Step 9(主 Agent: VM 实现)
                                                               ↓
                                               Step 10(quality-engineer-A 需求检查)
                                                               ↓
                                          主 Agent 汇总问题清单（P0/P1 issues JSON）
                                                               ↓
                                          Step 10.5(quality-engineer-B 编译验证+修复)
                                                               ↓
                                          主 Agent 汇总修复项 + VM 代码摘要
                                                    ┌──────────┴──────────┐
                                       Step 11(quality-engineer-C 单测)  Step 12(quality-engineer-D 测试用例)
                                                    └──────────┬──────────┘
                                                               ↓
                                          Step 12.5(workflow-judge 代码质量打分)
                                                               ↓
                                                        Step 13(人工测试)
                                                               ↓
                                          Step 14(workflow-retrospector 工作流回顾)
                                                               ↓
                                      更新 improvement_backlog.md（闭环下一次工作流）
```

---

## Team 生命周期

### 1️⃣ 预检查
协调 agent 先读取 `docs/component/{模块}/{页面驼峰}/` 与真实代码目录，识别：
- 当前是新建 / 迭代 / 混合模式
- 当前已完成的最高步骤
- 最早阻塞点
- 是否进入状态模式还是团队模式

### 2️⃣ 创建 / 维护 Team
推荐使用显式 team，并优先懒加载成员：只在对应阶段即将开始时创建角色 worker。

**推荐 Team 启动模板：**

```text
team_create(team_name = "ai-coding-{模块}-{页面}")

# 角色名 → subagent_name / subagent_path
workflow-orchestrator → bmad-orchestrator / .codebuddy/agents/bmad-orchestrator.md
requirements-owner   → bmad-po           / .codebuddy/agents/bmad-po.md
protocol-architect   → bmad-architect    / .codebuddy/agents/bmad-architect.md
report-architect     → bmad-architect    / .codebuddy/agents/bmad-architect.md
solution-architect   → bmad-architect    / .codebuddy/agents/bmad-architect.md
# Step 6~9 由主 Agent 直接执行，不创建 sub-agent 成员
quality-engineer-A   → bmad-qa           / .codebuddy/agents/bmad-qa.md
quality-engineer-B   → bmad-qa           / .codebuddy/agents/bmad-qa.md
quality-engineer-C   → bmad-qa           / .codebuddy/agents/bmad-qa.md
quality-engineer-D   → bmad-qa           / .codebuddy/agents/bmad-qa.md
workflow-judge       → bmad-review       / .codebuddy/agents/bmad-review.md
workflow-reviewer    → bmad-review       / .codebuddy/agents/bmad-review.md（按需创建）
```

> 每个角色在 `task` 调用时，`name` 填角色名（如 `ui-developer`），`subagent_name` 填对应 agent 名，`subagent_path` 填 agent 定义文件路径。详见上方「subAgent 调用规范」。

**推荐原则：**
- `workflow-orchestrator` 必须最先创建
- 并行组 A / B 使用不同成员，不要把多个并行步骤塞给同一个 worker
- 如果当前只需"当前进度 / 下一步"，不创建 team
- 如果当前 team 已存在，优先复用，不重复建队

### 3️⃣ 派发与协作
- `requirements-owner` 完成 Step 1 后，把需求文档路径、模式判断和待评审要点回报给 `workflow-orchestrator`
- Step 2 完成前，协调 agent 不能派发 Step 3 / 4
- Step 3 / 4 完成后，由 `solution-architect` 汇总协议 / 上报结论并产出 Step 5 输入
- Step 5 完成后，由 `workflow-orchestrator` 先向用户汇总技术方案并停下来等待确认
- 仅当用户明确确认继续编码后，主 Agent 直接并行执行 Step 6 / 7 / 8（调用对应 skill，不派发 sub-agent）
- Step 9 只能在 Step 5 已获用户确认且 Step 6 + 8 完成后由主 Agent 直接执行
- `quality-engineer-A` 只能在 Step 9 后接手验证链路；完成后主 Agent 汇总问题清单再派发 `quality-engineer-B`
- `quality-engineer-B` 完成后主 Agent 汇总修复项与 VM 代码摘要，再**并行**派发 `quality-engineer-C` 和 `quality-engineer-D`
- `workflow-judge` 在 Step 11 + 12 全部完成后由主 Agent 派发，对最终交付物做结构化打分；打分报告产出后由 `workflow-orchestrator` 汇总给用户，再进入 Step 13
- `workflow-reviewer` 用于阶段性复核，不替代执行角色

### 4️⃣ 计划审批
高风险阶段建议启用计划先行：
- `protocol-architect` / `report-architect` / `solution-architect` 在开始前先提交计划
- `ui-developer` / `api-developer` / `vm-developer` 涉及较大改动时先提交计划
- 由 `workflow-orchestrator` 审批通过后再执行

### 5️⃣ 消息与汇总
- 角色成员之间使用点对点 `send_message`
- 只有需求变更、全局阻塞、人工关卡通知等场景才使用广播
- `workflow-orchestrator` 在"完成一个步骤""阻塞点变化""等待人工动作"这三类事件发生时向用户汇总

### 6️⃣ 收尾与清理
- 主流程结束后，先让所有空闲成员关闭
- 确认无活跃成员后，再清理 team
- 若成员未完成或仍在处理工具调用，不要强行删除 team

---

## 执行流程：协调 Agent 行为规范

### 0️⃣ 加载历史改进项（启动预热）

协调 agent 在每次启动（无论状态模式还是团队模式）时，**必须先读取改进台账**：

```
路径：docs/本地知识库/工作流回顾/improvement_backlog.md
```

**筛选规则**：
- 状态为「待修订」或「生效中」的条目
- 载体与本次即将执行的 Step / Skill 匹配
- 影响范围覆盖本次模式（新建 / 迭代）

**注入规则**：
- 在派发对应 Step 的 `task.prompt` 末尾，追加一段「历史改进约束」：
  ```
  ⚠️ 历史改进约束（来自 improvement_backlog.md）：
  - [IMP-00x] {改进项标题}：{具体约束描述}
  - ...
  请在本次产出中严格遵守以上约束，避免重复踩坑。
  ```
- 若台账文件不存在或为空，跳过该步骤，不视为错误。
- 主 Agent 直接执行 Step 6~9 时，同样需在 skill 调用前把匹配的改进项作为额外约束纳入执行上下文。

> **目的**：形成"回顾 → 沉淀 → 注入 → 防复发"的闭环，让工作流每一轮都比上一轮更稳。

### 1️⃣ 读取进度表并判断进度

用户触发时，按以下顺序执行：

1. **先判断模式**：检查 `docs/component/{模块名}/{页面名驼峰}/diff/` 目录是否存在且包含 `_diff.md`
2. **定位并读取进度表**：新建模式读 `{页面下划线}_progress.md`；迭代模式读 `diff/{页面下划线}_progress_diff.md`
3. **用真实产物做交叉校验**：按以下清单检查文件是否存在、编译是否通过、测试证据是否齐全
4. **必要时修表 / 回填**：若进度表不存在则按清单创建；若进度表状态与真实证据不一致，则立即修正

> **判断优先级**：导航时优先参考进度表；完成判定时必须回到真实产物 / 编译 / 测试证据。若两者冲突，以真实证据为准。

随后按以下清单确定已完成的 Step：

**🆕 新建模式：**
```
docs/component/{模块}/{页面驼峰}/{页面下划线}.md              → Step 1 ✅
docs/component/{模块}/{页面驼峰}/{页面下划线}_protocol.md      → Step 3 ✅
docs/component/{模块}/{页面驼峰}/{页面下划线}_report.md        → Step 4 ✅
docs/component/{模块}/{页面驼峰}/{页面下划线}_tech_solution.md → Step 5 ✅
module/{模块}/{功能}/ui/*Page.kt                              → Step 6 ✅
module/{模块}/{功能}/mock/*MockData.kt                        → Step 7 ✅
module/{模块}/{功能}/repository/*.kt                          → Step 8 ✅
module/{模块}/{功能}/*ViewModel.kt                            → Step 9 ✅
（UI 组件覆盖率 100% + androidApp 编译通过）                    → Step 10.5 ✅
commonTest/.../*ViewModelTest.kt                              → Step 11 ✅
docs/component/{模块}/{页面驼峰}/{页面下划线}_testcase.md      → Step 12 ✅
docs/component/{模块}/{页面驼峰}/{页面下划线}_review_score.md  → Step 12.5 ✅
```

**✏️ 迭代模式（已有页面）：**
```
docs/component/{模块}/{页面驼峰}/diff/{页面下划线}_diff.md              → Step 1 ✅
docs/component/{模块}/{页面驼峰}/diff/{页面下划线}_protocol_diff.md     → Step 3 ✅
docs/component/{模块}/{页面驼峰}/diff/{页面下划线}_report_diff.md       → Step 4 ✅
docs/component/{模块}/{页面驼峰}/diff/{页面下划线}_tech_solution_diff.md → Step 5 ✅
（已有 UI 代码中修改了 diff 涉及的组件）                               → Step 6 ✅
（已有 Mock 数据中新增了 diff 接口的 Mock）                            → Step 7 ✅
（已有 Repository 中新增 / 修改了 diff 接口）                          → Step 8 ✅
（已有 ViewModel 中新增 / 修改了 diff 涉及的 UseCase / Action）         → Step 9 ✅
（UI 组件覆盖率 100% + androidApp 编译通过）                            → Step 10.5 ✅
commonTest/.../*ViewModelTest.kt（含 diff 新增用例）                    → Step 11 ✅
docs/component/{模块}/{页面驼峰}/diff/{页面下划线}_testcase_diff.md      → Step 12 ✅
docs/component/{模块}/{页面驼峰}/diff/{页面下划线}_review_score_diff.md  → Step 12.5 ✅
```

> **Step 5 人工确认门禁**：
> - 即使 `_tech_solution.md` / `_tech_solution_diff.md` 已存在，也不等于可以自动进入编码阶段
> - 若当前会话中用户尚未明确确认"按该技术方案继续编码"，默认阻塞在 Step 5 后人工关卡
> - 协调 agent 需先向用户汇总技术方案产物与关键结论，待用户确认后再派发 Step 6 / 7 / 8

> **Step 5 日志与可观测性设计**（参考 `docs/本地知识库/开发规范/如何打印日志.md`）：
>
> 技术方案文档（`_tech_solution.md` / `_tech_solution_diff.md`）**必须包含「日志与可观测性」章节**，至少覆盖以下内容：
>
> 1. **模块日志入口**：当前页面 / 功能所属的日志 Tag 模块。若已有（如 `HomeLog`、`DramaLog`、`UserLog`），直接复用；若没有，在技术方案中定义新的模块级 `BaseBizLog` 子对象，参照 `WsLogHelper.kt` 中的声明方式。
> 2. **按层落点**：
>    - **Repository / DataRepo 层**：请求发起前打印关键请求参数，响应后打印返回结果（成功时摘要、失败时 errorMsg + errorCode）
>    - **ViewModel / UseCase 层**：状态流转关键节点打印（如加载开始 / 完成 / 失败、用户操作、重试）
>    - **UI 层**：仅在页面生命周期或关键曝光处打印，不在重组高频路径打日志
> 3. **关键节点清单**：列出本需求中需要打印日志的关键节点，例如：
>    - 页面进入 / 退出
>    - 核心接口请求前（参数）、请求后（结果/错误）
>    - 关键用户操作（点击、切换、提交）
>    - 异常分支与降级
> 4. **对象日志格式**：复杂参数使用 `KtJson.safeEncode(...)` 序列化后打印，避免拼接大量 `toString()`
> 5. **约束**：
>    - 敏感信息脱敏（不打印 token、密码等）
>    - 生产环境默认走 `fileLog`，调试日志走 `d()` / `i()`
>    - 不在 `LazyColumn` / `items` / `Composable` 重组路径中打高频日志

> **Step 10 日志落地验收**：
>
> `quality-engineer` 在 Step 10 需求检查阶段，**必须额外验收以下日志相关项**：
>
> 1. **是否已按技术方案「日志与可观测性」章节补齐日志**：对照 Step 5 产物逐条检查
> 2. **关键链路覆盖**：核心接口请求/响应日志是否齐全、ViewModel 状态流转日志是否覆盖成功/失败/异常分支
> 3. **模块归属正确**：日志是否使用了正确的模块级 `BaseBizLog` 子对象（如 `DramaLog.d()`），而非随意 `println` 或裸 `Log.d()`
> 4. **无违规日志**：
>    - 无敏感信息直出（token、密码、用户隐私字段）
>    - 无在 UI 重组高频路径中的刷日志代码
>    - 无遗留的 `println` / `System.out` / 裸 `Log.*` 调用
> 5. 若发现日志遗漏或违规，列入遗漏清单并要求修复后才可通过 Step 10

> **Step 12.5 代码质量打分（`workflow-judge`）**：
>
> `workflow-judge` 角色由 `bmad-review` agent 扮演，但与 `workflow-reviewer` 定位不同：**只打分、不派活**，对最终交付物做结构化评估。
>
> **输入**：
> - 需求文档：`docs/component/{模块}/{页面驼峰}/{页面下划线}.md`（迭代模式还需加载 `diff/*_diff.md`）
> - 技术方案：`{页面下划线}_tech_solution.md`（含迭代 diff）
> - 本次改动涉及的核心代码路径（VM / Repository / UI / Mock / 单测）
> - Step 10 问题清单、Step 10.5 修复记录、Step 11 单测产物、Step 12 测试用例
>
> **打分维度（总分 100，每项单独给分）**：
>
> | 维度 | 权重 | 评估要点 |
> |------|------|---------|
> | 需求合规性 | 25 | 功能点覆盖、交互逻辑、异常分支、边界条件是否与需求 + diff 一致 |
> | 架构合规性 | 17 | 依赖方向、MVVM 分层、VM 接口面向 UI 设计、状态管理用 StateFlow、无违反通用架构规范 |
> | Struct 规范合规性 | 10 | **仅对 Struct 页面生效**。依据 `.codebuddy/rules/Struct结构规范/RULE.mdc` 评估：<br>① **模块落位**：`PageArgs` / 页面 VM 接口 / Service Factory 是否在契约层（`wsCore`）；`PageWidget` / `DataRepo` / Widget VM 实现是否在逻辑模块（`wsUser` / `wsDrama` 等）；UI 入口是否在 UI 模块（`wsCompose`）并通过 `Service.pageFactory` 获取装配，禁止 `new` 逻辑实现类<br>② **页面骨架**：`StructPageConfig` 的 `titleBar` / `header` / `pager` / `layers` / `hanging` / `titleHanging` / `bottomBar` 槽位使用是否正确（多 tab 页面不把 tab 条错放进 `layers` 或 `titleHanging`）<br>③ **吸顶 / 悬浮模式选择**：原位吸顶型（模式 A）、延迟出现型（模式 B）、顶部固定型（模式 C）是否按需求对号入座，未出现"为了吸顶机械加 titleHanging"或"应该用 `fixChannelBarBelowTitleBar` 却自己模拟吸顶"的反模式<br>④ **多 tab 实现**：tab 通过 `PagerWidget + ChannelBarWidget.createByChannels(...)` 构建；每个 tab 为独立 `StructPageChannelWidget` + 独立 `DataRepo`；`channelKey` / `channelName` / 默认选中项有稳定映射；动态 tab 变更时 `channels` / `mainChannel` / 初始 index / widget provider 同步更新<br>⑤ **页面路由与参数**：`@Page(...)` 正确注册；`PageArgs` 实现 `IComposePageArgs` 且标记 `@Serializable`<br>⑥ **结构测试**：是否有 `*PageWidgetStructureTest.kt` 锁定槽位结构、tab 顺序、默认选中项、hanging 配置 |
> | UI / Kuikly 合规性 | 13 | 包导入（`com.tencent.kuikly.compose.*`）、QnText / QnImage / QnLottie 等组件使用、禁用 API 未出现 |
> | Kotlin 代码质量 | 13 | 空安全、协程作用域、命名、不可变优先、错误处理清晰 |
> | 测试覆盖 | 13 | VM 单测覆盖成功 / 失败 / 空态 / 重试；测试用例 diff 完整 |
> | 日志与可观测性 | 9 | 按技术方案「日志与可观测性」章节落地，模块 Tag 正确，无敏感信息、无高频重组日志、无裸 `Log.*` |
>
> **Struct 规范维度适用判定**（裁判 agent 必须在报告开头写明判定结果）：
> - **适用条件**（满足任一即视为 Struct 页面）：页面入口使用 `StructComposePage` / `StructComposePage4VM`；或实现类继承 `StructPageWidget2` / 使用 `StructPageConfig`；或使用 `IStructDataRepo` / `IStructDataSuspendRepo` / `PagerWidget` / `ChannelBarWidget`；或需求文档明确声明"多 tab / 吸顶 / 悬浮 / 品字形"。
> - **不适用时**：Struct 规范维度按"N/A"处理，该维度的 10 分按比例重分配到其他 6 个维度（各维度权重 × 100/90 向上取整），报告中明确标注"本页面非 Struct 页面，Struct 规范维度不参与评分"。
>
> **输出产物**：
> - 新建模式：`docs/component/{模块}/{页面驼峰}/{页面下划线}_review_score.md`
> - 迭代模式：`docs/component/{模块}/{页面驼峰}/diff/{页面下划线}_review_score_diff.md`
>
> **输出格式（Markdown）**：
>
> ```markdown
> # Code Review Score Report
>
> ## 概要
> - **总分**：{0-100}
> - **结论**：Pass (>=85) / Pass with Risk (70-84) / Fail (<70)
> - **评估范围**：{涉及的文件和模块}
> - **Struct 规范适用判定**：{适用 / 不适用 + 判定依据（命中的关键类 / 配置 / 文件路径）}
>
> ## 维度分
> | 维度 | 得分 | 权重 | 加权得分 |
> |------|------|------|---------|
> | 需求合规性 | x/25 | 25% | ... |
> | 架构合规性 | x/17 | 17% | ... |
> | Struct 规范合规性 | x/10 或 N/A | 10% | ...（N/A 时在下方注明权重重分配方案） |
> | UI / Kuikly 合规性 | x/13 | 13% | ... |
> | Kotlin 代码质量 | x/13 | 13% | ... |
> | 测试覆盖 | x/13 | 13% | ... |
> | 日志与可观测性 | x/9 | 9% | ... |
>
> ## 关键扣分项
> ### Critical（每项扣 ≥ 10 分）
> - {问题 + 文件:行号 + 修复建议}
>
> ### Major（每项扣 3~10 分）
> - ...
>
> ### Minor（每项扣 1~3 分）
> - ...
>
> ## 风险提示
> - {潜在风险 / 技术债 / 后续观察点}
>
> ## 建议
> - {是否需要返工 / 是否可进入人工测试}
> ```
>
> **阈值与门禁**：
> - **总分 ≥ 85**：Pass，可进入 Step 13 人工测试
> - **总分 70~84**：Pass with Risk，主 Agent 汇总风险项给用户，由用户决定是否进入 Step 13
> - **总分 < 70 或 任一维度 < 50%**：Fail，主 Agent **必须停止自动推进**，向用户汇报打分报告并等待指示（通常回到 Step 9 / 10.5 修复）
>
> **行为红线**：
> - 裁判 agent **不修复问题、不写代码**，只产出评分报告
> - 评分必须引用具体文件路径和行号作为证据，禁止凭感觉给分
> - 不得参考 Step 10 / 10.5 的自评结论，保持独立评估视角
> - 迭代模式下，打分范围聚焦 diff 涉及的改动，不对未改动代码重复扣分
> - **Struct 规范维度的适用判定必须在报告开头明确给出**，不得含糊处理；判定为"适用"时必须针对 6 个评估要点逐条给出通过 / 扣分依据，不得只给一个总分数字

> **Step 14 工作流回顾（`workflow-retrospector`）**：
>
> Step 13 人工测试结束后，`workflow-orchestrator` 必须**主动提示**用户做回顾，不能默认跳过。用户也可在任意阶段说"回顾本次工作流"主动触发。
>
> **输入**：
> - 本次工作流涉及的所有产物路径（需求文档、协议、上报、技术方案、代码改动、测试用例、打分报告）
> - Step 12.5 打分报告（总分 + 维度分 + 扣分项）
> - 执行过程中的关键卡点日志（从协调 agent 汇总记录中提取）
> - 历史改进台账 `docs/本地知识库/工作流回顾/improvement_backlog.md`（识别是否有本次新出现的、未记录的问题）
>
> **引导流程（固定 5 段，逐段追问，不要一次性丢完模板）**：
>
> 1. **基本信息**：日期、模式（新建 / 迭代）、涉及角色、工作流版本（取 `workflow-reference.md` 的 git commit）、Step 12.5 总分
> 2. **踩坑清单**：逐 Step 询问"是否有返工 / 卡壳 / 输出不达预期"，字段 = Step / 问题描述 / 影响（返工次数 / 人工介入时长）/ 临时对策 / 根因猜测
> 3. **亮点**：哪些 Step 超预期、哪些并行节省了时间、哪些门禁提前拦截了问题
> 4. **改进建议**：将踩坑项转化为具体 Action Items，字段 = 改进项 / 类型（Skill 修订 / Prompt 收紧 / 规则扩展 / Agent 定义调整）/ 载体文件 / 优先级（P0/P1/P2）
> 5. **数据指标**：总耗时、人工介入次数、自动推进成功率、Step 10 问题数按 P0/P1/P2 分布、Step 12.5 维度最低分
>
> **输出产物**：
> - 新建模式：`docs/component/{模块}/{页面驼峰}/{页面下划线}_retrospective.md`
> - 迭代模式：`docs/component/{模块}/{页面驼峰}/diff/{页面下划线}_retrospective_diff.md`
> - **同步更新**：`docs/本地知识库/工作流回顾/improvement_backlog.md`，把新产生的改进建议追加为新条目（ID 递增，状态置为「待修订」）
>
> **产物模板**：`docs/本地知识库/工作流回顾/_retrospective_template.md`（直接 copy 填充即可）
>
> **行为红线**：
> - 回顾角色**只沉淀、不修复**：不在本环节改 skill、改 prompt、改规则文件；所有修订动作通过 `improvement_backlog.md` 条目驱动，在下一次工作流启动前由人工或专门会话处理
> - **不得编造问题**：只记录用户明确反馈或协调 agent 日志中可查的真实卡点
> - **改进项必须落到具体载体**：禁止出现"优化一下流程"这类模糊描述，必须指向具体文件（如 `.codebuddy/skills/design-api-protocol/SKILL.md` 或 `workflow-reference.md` Step X 段落）
> - **台账追加而非覆写**：新增条目的 ID 必须全局递增，禁止复用旧 ID
> - 若用户表示"这次没什么问题"，仍然要求至少记录"数据指标"和"亮点"两段，保留可量化的工作流运行数据
>
> **闭环验证**：
> - 下一次工作流启动时，协调 agent 的「0️⃣ 加载历史改进项」环节会读取台账中状态为「待修订」的条目并注入到对应 Step 的 prompt 中
> - 改进项在后续工作流中确认不再复发后，由 `workflow-retrospector` 或人工将该条目状态置为「已关闭」，并填写「验证页面」字段

### 2️⃣ 输出引导

```text
📌 当前模式：{新建模式 / 迭代模式 / 混合模式}
📌 当前进度：Step {N} 已完成 / 当前阻塞在 Step {N}
🗂️ 进度表：{progress-path}
👥 当前编排：{状态模式 / 团队模式}（协调者：workflow-orchestrator）
📋 下一步：Step {N+1} — {名称}
🧩 负责人：{角色 agent 名称}
📥 需要你提供：{该步骤的输入}
💡 Prompt："{示例 Prompt}"
```

若阻塞在 Step 5 后人工关卡，`下一步` 必须写成"请用户确认技术方案是否进入编码阶段"，不要直接列出 Step 6 / 7 / 8。

若有可并行的步骤，一并列出：

```text
⚡ 可并行执行：
  - Step {X}：{角色 agent} → "{Prompt X}"
  - Step {Y}：{角色 agent} → "{Prompt Y}"
```

### 3️⃣ 派发规则
- **所有步骤通用规则**：每次准备执行“下一步”前，先读取对应需求的进度表；每个步骤完成后，先回写进度表，再决定是否自动推进。
- **Step 1**：由 `requirements-owner` 调用 `analyze-tapd-story`
- **Step 2**：人工评审，协调 agent 只负责列出所需输入和待确认点
- **Step 3 / 4**：分别派给 `protocol-architect` 与 `report-architect`，严格并行
- **Step 5**：交给 `solution-architect` 汇总 Step 3 / 4 结论后完成技术方案，并在产出后停下来请求用户确认
- **Step 6 / 7 / 8**：主 Agent 直接并行执行（调用 `restore-ui-design` / `generate-mock-data` / `implement-api-layer` skill），仅在用户确认继续编码后启动，**不派发 sub-agent**
- **自动续跑**：当 Step 6 / 7 / 8 全部完成后，主 Agent 默认自动推进 Step 9 → Step 10 → Step 10.5 → Step 11 → Step 12；但每次续跑前都必须重新读取最新进度表，只有遇到新的阻塞点或 Step 13 人工测试关卡时才停下
- **Step 9**：主 Agent 直接执行（调用 `implement-viewmodel` skill），在 Step 5 已获用户确认且 Step 6 + 8 完成后启动，**不派发 sub-agent**
- **Step 10**：由 `quality-engineer-A` 负责，输出结构化问题清单 JSON；主 Agent 解析后汇总 P0/P1 问题传给下一步
- **Step 10.5**：由 `quality-engineer-B` 负责，输入为主 Agent 汇总的问题清单；编译失败时主 Agent 停止自动推进
- **Step 11 / 12**：Step 10.5 通过后由主 Agent **并行**派发 `quality-engineer-C`（单测）和 `quality-engineer-D`（测试用例）；两者输入均来自主 Agent 汇总的摘要，不依赖彼此
- **Step 12.5**：Step 11 + 12 全部完成后由主 Agent 派发 `workflow-judge`，对最终交付物做结构化打分；报告产出后 `workflow-orchestrator` 汇总结论向用户汇报，再进入 Step 13
- **Step 13**：人工测试执行，协调 agent 只输出测试输入与交接说明
- **Step 14**：Step 13 完成后，`workflow-orchestrator` 主动提示用户做工作流回顾；用户确认后切换为 `workflow-retrospector` 角色引导回顾；产物保存到页面目录下的 `_retrospective.md`（迭代模式放 `diff/` 目录），并同步更新 `docs/本地知识库/工作流回顾/improvement_backlog.md` 中的新增改进项。用户也可在任意阶段主动说"回顾本次工作流"强制触发 Step 14。

### 4️⃣ 执行规则
- **Step 6~9 由主 Agent 直接执行，禁止为这四个步骤创建或派发 sub-agent**
- **不要跳过人工关卡**
- **如果 Step 5 已完成但尚未获得用户确认，不得派发 Step 6 / 7 / 8**
- **不要把并行组压回串行单 worker，除非明确资源不足并在输出里解释**
- **不要凭想象标记"已完成"**
- **如果 team 执行失败，降级到状态模式，并明确谁卡住、卡在哪一步、缺什么输入**

---

## Prompt 速查表

```
**🆕 新建页面流程**

Sprint 1 — 需求
  requirements-owner:
    "帮我分析一下这个需求：{TAPD链接}"                    → analyze-tapd-story
  （线下评审）

Sprint 2 — 设计
  protocol-architect:
    "根据 docs/component/xxx/{页面名}.md 设计接口协议"     → design-api-protocol        ┐ 并行组 A
  report-architect:
    "分析 docs/component/xxx/{页面名}.md 的上报需求"       → analyze-report-document    ┘
  ── 等上面 2 个完成 ──
  solution-architect:
    "根据 docs/component/xxx/{页面驼峰}/{页面下划线}.md 设计技术方案"  → design-tech-solution

Sprint 3 — 编码（先并行，再串行；Step 6~9 均由主 Agent 直接执行）
  ── 并行组 B：主 Agent 同时执行以下三个 skill ──
  [主 Agent] 根据设计稿还原 UI  Figma: {url}               → restore-ui-design          ┐
  [主 Agent] 根据 _tech_solution.md 生成 Mock 数据          → generate-mock-data         │ 并行组 B
  [主 Agent] 根据 _tech_solution.md 开发接口层              → implement-api-layer        ┘
  ── 等上面 3 个完成 ──
  [主 Agent] 根据已有代码实现 xxx 的 ViewModel              → implement-viewmodel
  ── Step 9 完成后，主 Agent 提炼交接摘要（VM 接口签名、三态逻辑、关键文件路径）──
  quality-engineer-A:
    "对照 docs/component/xxx.md 检查代码实现，输出结构化问题清单 JSON"
                                                         → check-requirements
  ── 主 Agent 汇总：解析问题清单，提取 P0/P1 issues + next_step_context ──
  quality-engineer-B:
    "修复 P0/P1 问题并验证 androidApp 编译，输入：{主 Agent 汇总的问题清单}"
                                                         → verify-build-integrity
  ── 主 Agent 汇总：解析修复项，提取 VM 代码摘要；编译失败则停止并告知用户 ──
  ── 并行组 C：同时派发以下两个 ──
  quality-engineer-C:
    "为 xxx ViewModel 生成单元测试，输入：{主 Agent 汇总的 VM 代码摘要}"
                                                         → generate-unit-tests  ┐ 并行组 C
  quality-engineer-D:
    "为 xxx 生成测试用例，输入：需求文档 + {主 Agent 汇总的问题清单摘要}"
                                                         → generate-test-cases  ┘
  ── 等上面 2 个完成 ──
  workflow-judge:
    "对最终交付物做结构化代码质量打分。
     输入：
       需求: docs/component/xxx/{页面驼峰}/{页面下划线}.md
       技术方案: docs/component/xxx/{页面驼峰}/{页面下划线}_tech_solution.md
       核心代码: VM / Repository / UI / Mock / 单测路径
     输出: docs/component/xxx/{页面驼峰}/{页面下划线}_review_score.md
     要求: 按 6 个维度打分，总分 100；Fail(<70) 必须停止并回报用户"
                                                         → Step 12.5 代码质量打分

Sprint 4 — 测试
  （测试同学执行）

Sprint 5 — 回顾（Step 14）
  workflow-orchestrator → 在 Step 13 结束后主动提示：
    "✅ 本次工作流已完成。建议做一次 3-5 分钟回顾（Step 14）。
     回复 '开始回顾' 将沉淀踩坑点和改进项到：
       docs/component/xxx/{页面驼峰}/{页面下划线}_retrospective.md
     并更新 docs/本地知识库/工作流回顾/improvement_backlog.md"
  workflow-retrospector（用户确认后启动）:
    基于模板 docs/本地知识库/工作流回顾/_retrospective_template.md 逐段引导：
      1. 基本信息（日期 / 模式 / 参与角色 / 工作流版本 / 打分）
      2. 踩坑清单（Step / 问题 / 影响 / 临时对策 / 根因猜测）
      3. 亮点
      4. 改进建议（逐项追加到 improvement_backlog.md）
      5. 数据指标（总耗时 / 人工介入次数 / 自动推进成功率 / Step 10 问题分布 / Step 12.5 维度最低分）

---

**✏️ 已有页面迭代流程（diff 模式）**

Sprint 1 — 需求
  requirements-owner:
    "帮我分析一下这个需求：{TAPD链接}"                    → analyze-tapd-story（自动识别已有页面，输出 diff 文档）
  （线下评审，结合基线文档 + diff 文档一起评审）

Sprint 2 — 设计
  protocol-architect:
    "根据 docs/component/xxx/{页面驼峰}/{页面下划线}.md 和 diff/{页面下划线}_diff.md 设计接口协议变更"
                                                         → design-api-protocol（迭代模式）        ┐ 并行组 A
  report-architect:
    "根据 docs/component/xxx/{页面驼峰}/{页面下划线}.md 和 diff/{页面下划线}_diff.md 分析上报变更需求"
                                                         → analyze-report-document（迭代模式）    ┘
  （若协议 / 上报无变化，对应 diff 文档写「保持原样」，跳过该步骤）
  ── 等上面 2 个完成 ──
  solution-architect:
    "根据 docs/component/xxx/{页面驼峰}/{页面下划线}.md 和 diff/{页面下划线}_diff.md 设计技术方案变更"
                                                         → design-tech-solution（迭代模式）

Sprint 3 — 编码（先并行，再串行；Step 6~9 均由主 Agent 直接执行）
  ── 并行组 B：主 Agent 同时执行以下三个 skill ──
  [主 Agent] 根据 diff 设计稿修改 UI  基线: {页面下划线}.md  diff: diff/{页面下划线}_diff.md  Figma: {url}
                                                         → restore-ui-design（迭代模式）          ┐
  [主 Agent] 根据 diff/{页面下划线}_tech_solution_diff.md 更新 Mock 数据
                                                         → generate-mock-data                     │ 并行组 B
  [主 Agent] 根据 diff/{页面下划线}_tech_solution_diff.md 更新接口层
                                                         → implement-api-layer                    ┘
  ── 等上面 3 个完成 ──
  [主 Agent] 根据基线文档 + diff 文档更新 xxx 的 ViewModel
                                                         → implement-viewmodel（迭代模式，只修改 diff 涉及部分）
  ── Step 9 完成后，主 Agent 提炼交接摘要（已修改的 VM 接口签名、diff 涉及的 UseCase/Action）──
  quality-engineer-A:
    "对照基线文档 + diff 文档检查代码实现，输出结构化问题清单 JSON"
                                                         → check-requirements（迭代模式）
  ── 主 Agent 汇总：解析问题清单，提取 P0/P1 issues + next_step_context ──
  quality-engineer-B:
    "修复 P0/P1 问题并验证 androidApp 编译，输入：{主 Agent 汇总的问题清单}"
                                                         → verify-build-integrity
  ── 主 Agent 汇总：解析修复项，提取 VM 代码摘要；编译失败则停止并告知用户 ──
  ── 并行组 C：同时派发以下两个 ──
  quality-engineer-C:
    "为 xxx ViewModel 补充 diff 相关单元测试，输入：{主 Agent 汇总的 VM 代码摘要}  diff需求: docs/component/xxx/{页面驼峰}/diff/{页面下划线}_diff.md"
                                                         → generate-unit-tests  ┐ 并行组 C
  quality-engineer-D:
    "为 xxx 的 diff 需求生成测试用例，输入：需求文档 + diff 文档 + {主 Agent 汇总的问题清单摘要}"
                                                         → generate-test-cases（迭代模式，生成增量用例保存到 diff 目录）┘
  ── 等上面 2 个完成 ──
  workflow-judge:
    "对 diff 涉及的改动做结构化代码质量打分（迭代模式）。
     输入：
       基线需求: docs/component/xxx/{页面驼峰}/{页面下划线}.md
       diff 需求: docs/component/xxx/{页面驼峰}/diff/{页面下划线}_diff.md
       技术方案 diff: docs/component/xxx/{页面驼峰}/diff/{页面下划线}_tech_solution_diff.md
       核心改动代码: 本次变更涉及的 VM / Repository / UI / Mock / 单测
     输出: docs/component/xxx/{页面驼峰}/diff/{页面下划线}_review_score_diff.md
     要求: 打分聚焦 diff 改动；Fail(<70) 必须停止并回报用户"
                                                         → Step 12.5 代码质量打分（迭代模式）

Sprint 4 — 测试
  （测试同学执行）

Sprint 5 — 回顾（Step 14，迭代模式）
  workflow-orchestrator → Step 13 结束后主动提示用户回顾
  workflow-retrospector（用户确认后启动）:
    基于模板引导回顾，产物保存到：
      docs/component/xxx/{页面驼峰}/diff/{页面下划线}_retrospective_diff.md
    并更新改进台账：
      docs/本地知识库/工作流回顾/improvement_backlog.md
    重点关注：
      - 基线文档 + diff 文档结合使用时的踩坑点
      - 迭代模式下「保持原样」判断是否准确
      - diff 涉及范围是否精准，有无误改基线代码
```
