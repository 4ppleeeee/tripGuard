---
name: ai-coding-workflow
description: Use when 用户希望按产品/架构/开发/测试多角色协同推进页面研发全流程，或需要根据 `docs/component` 下的产物判断当前阶段，并由协调 agent 派发下一步工作。
---

# AI Coding Workflow

## Overview
将页面研发流程升级为**显式多 agent 编排**。该 skill 负责：
- 检查 `docs/component/...` 与实际代码产物，识别当前模式、进度和阻塞点
- 为**每个需求执行**创建并维护一份显式进度表，作为继续执行和自动续跑的第一依据
- 创建并维护一个 `Agent Team`
- 以 `workflow-orchestrator` 为协调 agent，驱动需求、架构、开发、QA、评审等角色 agent
- 按产物门禁推进串/并行步骤，并把阶段结果汇总给用户

本 skill **负责协调，不把所有工作塞给单个 agent**。具体分析、设计、编码、验证由被派发的角色 agent 完成；若用户只问进度或下一步，可降级为只做状态判断和建议，不创建团队。

继续、自动续跑、或切到下一步前，都必须先读取对应需求的进度表；若进度表缺失，则先按真实产物回填补齐，再决定动作。

## When to Use
- 用户说"走一遍全流程""按流程开发""从需求到开发""继续推进"。
- 用户希望以多角色 / 多 agent 协同方式推进需求。
- 用户问"当前进度到哪了""下一步做什么"，并希望系统自动接续后续工作。
- 需要根据 `docs/component/{模块}/{页面驼峰}/` 与实际代码产物判断页面处于哪个阶段。

以下场景不要使用本 skill：
- 用户已经明确要执行某个具体步骤，例如"帮我分析 TAPD""帮我设计协议""帮我还原 UI"。此时直接使用对应 skill。
- 用户只需要局部代码修改，不需要跨阶段协同。此时直接进入对应开发 / 调试流程。

## Required Reference
先阅读 `references/workflow-reference.md`，再给出任何流程判断或团队编排。将该文件视为以下内容的唯一参考：
- 步骤编号、产物检查、并行组和人工关卡
- 需求级进度表的文件路径、模板、状态字段、回填规则与更新时间点
- 协调 agent、角色 worker 与 team 生命周期
- `team_create` / `task` / `send_message` / `team_delete` 的使用约定
- 每一步的推荐 prompt 模板

## Workflow

### 1. 先判断执行模式
- 用户只问"当前进度 / 下一步" → 使用**状态模式**：只检查阶段、阻塞点和下一步，不创建团队。
- 用户要求"继续 / 按流程推进 / 全流程协同开发 / 多 agent 协作" → 使用**团队模式**：创建或恢复 team，并由协调 agent 推进。

### 2. 先读取 / 补齐进度表
- 每个需求都必须维护一份**需求级进度表**；路径、字段和模板以 `references/workflow-reference.md` 为准。
- 进入任一步骤、自动续跑、或响应 `@.codebuddy/commands/ws.dev.continue.md` 前，必须先读取进度表。
- 若进度表不存在，先按参考文件中的产物清单**回填创建**，再继续判断下一步。
- 若进度表与真实产物、编译结果、测试证据冲突，以**真实证据为准**，并立即修正进度表。
- 每个步骤完成后，必须**先更新进度表**，再向用户汇总或进入下一步。

### 3. 产物驱动识别当前阶段
- 进度判断时，先看进度表，再按参考文件中的产物检查清单，自前向后检查 `docs/component/...`、代码目录与验证证据。
- 仅将**真实存在的产物或验证结果**视为完成。
- 若发现步骤缺口或顺序倒挂，以**最早缺失步骤**作为阻塞点。
- 混合模式下按页面分别判断，不要把不同页面状态混成一个结论。

### 4. 创建团队
团队模式下，默认创建一个显式 team，并优先生成以下成员：
- `workflow-orchestrator` → 协调者，负责拆解、派发、门禁、汇总；`subagent_name=bmad-orchestrator`，`subagent_path=.codebuddy/agents/bmad-orchestrator.md`
- `requirements-owner` → 需求 / 文档角色；`subagent_name=bmad-po`，`subagent_path=.codebuddy/agents/bmad-po.md`
- `protocol-architect` / `report-architect` / `solution-architect` → 架构 worker；`subagent_name=bmad-architect`，`subagent_path=.codebuddy/agents/bmad-architect.md`
- `ui-developer` / `mock-developer` / `api-developer` / `vm-developer` → 开发 worker；`subagent_name=bmad-dev`，`subagent_path=.codebuddy/agents/bmad-dev.md`
- `quality-engineer` → 检查 / 测试角色；`subagent_name=bmad-qa`，`subagent_path=.codebuddy/agents/bmad-qa.md`
- `workflow-reviewer` → 独立复核角色；需要时再生成；`subagent_name=bmad-review`，`subagent_path=.codebuddy/agents/bmad-review.md`
- `workflow-retrospector` → 回顾角色（Step 14 专用）；`subagent_name=bmad-orchestrator`，`subagent_path=.codebuddy/agents/bmad-orchestrator.md`

**派发时必须通过 `task` 工具调用 subAgent**，具体参数规范见 `references/workflow-reference.md` 的「subAgent 调用规范」章节。关键要求：
- `subagent_name` 和 `subagent_path` 必填，用于指定被调用的 agent 定义
- `name` 填角色名（如 `ui-developer`），开启 Team 异步模式
- `team_name` 填当前 team 名称
- `prompt` 必须包含完整输入产物路径和预期输出，不依赖 agent 上下文记忆

若当前平台或上下文不适合创建 team，才降级为单会话串行执行；降级时必须在输出里明确说明"已降级"。

### 5. 协调 agent 职责
`workflow-orchestrator` 必须：
- **启动时先加载历史改进项**：读取 `docs/本地知识库/工作流回顾/improvement_backlog.md`，筛选状态为「待修订 / 生效中」且匹配本次 Step 的条目，在派发 `task.prompt` 末尾以「⚠️ 历史改进约束」形式追加注入；台账不存在或为空时跳过（详见 `workflow-reference.md` 的「0️⃣ 加载历史改进项」）
- 维护当前模式、阶段、阻塞点、并行组状态
- **维护需求级进度表**：启动时读取 / 补齐；每步完成后回写状态、产物路径、更新时间和备注
- 任何自动推进前都先重新读取最新进度表，避免基于旧状态续跑
- 只根据真实产物推进下一步，禁止凭主观假设跳步
- 为每个角色 agent 派发**单一、可交付、带输入 / 输出路径**的任务
- 在 Step 2 / Step 5 / Step 13 等人工关卡停止自动推进，并明确所需人工动作
- 若 Step 5 技术方案已产出但当前会话尚无用户确认，默认阻塞在"技术方案确认"而不是直接进入编码阶段
- 在并行组 A / B 中等待所有前置成员回报完成后，才允许进入下一个串行步骤
- 汇总成员结果后，用固定状态格式对用户反馈

### 6. 角色派发原则
- `requirements-owner`：Step 1、Step 2 的文档准备与需求澄清
- `protocol-architect`：Step 3，负责协议设计
- `report-architect`：Step 4，负责上报需求分析
- `solution-architect`：Step 5，负责技术方案，并在产出后等待用户确认
- `ui-developer` / `mock-developer` / `api-developer`：Step 6 / 7 / 8，仅在 Step 5 获得用户确认后并行执行
- `vm-developer`：Step 9，负责 ViewModel 收口实现
- `quality-engineer`：Step 10、10.5、11、12，负责检查、编译验证、单测与测试用例
- `workflow-reviewer`：在关键里程碑或主流程结束后做独立复核，不直接替代执行角色
- `workflow-retrospector`：Step 13 人工测试结束后，`workflow-orchestrator` 主动提示用户做回顾，用户确认后切换为该角色引导 5 段式回顾（基本信息 / 踩坑清单 / 亮点 / 改进建议 / 数据指标），产物保存到页面目录下的 `_retrospective.md`（迭代模式放 `diff/` 目录），并同步追加新条目到 `docs/本地知识库/工作流回顾/improvement_backlog.md`。该角色**只沉淀不修复**，所有修订动作通过台账条目在下一次工作流中被注入。

### 7. 输出格式
状态模式或团队模式都使用简洁、固定的协调输出：

```text
📌 当前模式：{新建模式 / 迭代模式 / 混合模式}
📌 当前进度：Step {N} 已完成 / 当前阻塞在 Step {N}
🗂️ 进度表：{progress-path}
👥 当前编排：{状态模式 / 团队模式}（协调者：workflow-orchestrator）
📋 下一步：Step {N+1} — {名称}
🧩 负责人：{角色 agent 名称}
🛠️ 建议调用：{skill-name 或人工步骤}
💡 推荐 Prompt："{可直接复用的 prompt}"
```

若存在并行项，追加：

```text
⚡ 可并行执行：
- Step {X} — {名称} → {角色 agent} / {skill-name}
- Step {Y} — {名称} → {角色 agent} / {skill-name}
```

### 8. Strict Boundaries
- 不要绕过 `references/workflow-reference.md` 自行发明步骤、角色、进度表路径或输出路径。
- 不要在未读取 / 校验进度表前推进下一步或响应 `ws.dev.continue`。
- 不要在未检查产物前就创建大量成员并盲目派发。
- 不要让协调 agent 直接吞掉人工评审 / 人工测试节点。
- 不要在步骤完成后漏掉进度表回写。
- 不要在团队未清理或成员未空闲时重复创建新 team。
- 多 agent 协作失败时，先回退到"状态模式 + 明确阻塞说明"，不要伪装成已完成。
