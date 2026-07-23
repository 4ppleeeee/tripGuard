# wesee-core Agent 定义

## 设计理念

**索引驱动，按需加载**——agent `.md` 文件只保留：

1. **角色定位**：一句话说清职责
2. **执行流程**：步骤 + 每步该调哪个 skill
3. **文档索引**：列出需要 `read_file` 的路径模式
4. **行为红线**：核心约束

所有规范细节（包导入规则、组件用法、MVVM 分层、Kotlin 编码规范等）**不在 agent 里重复**，而是通过以下机制自动注入：

- **Workspace Rules**（`.codebuddy/rules/`）→ 自动注入到 agent context
- **Skill 加载**（`use_skill`）→ agent 执行时按需加载专业指引
- **文档读取**（`read_file`）→ agent 运行时按索引路径读取具体文档

## Agent 列表

| Agent | 文件 | 职责 |
|-------|------|------|
| `bmad-orchestrator` | `bmad-orchestrator.md` | 工作流协调：进度判断、任务派发、门禁控制 |
| `bmad-po` | `bmad-po.md` | 需求分析：TAPD → 结构化需求文档 |
| `bmad-architect` | `bmad-architect.md` | 架构设计：协议/上报/技术方案/VM接口 |
| `bmad-dev` | `bmad-dev.md` | 代码实现：UI/Mock/接口层/ViewModel |
| `bmad-qa` | `bmad-qa.md` | 质量保障：需求检查→编译验证→单元测试→测试用例 |
| `bmad-review` | `bmad-review.md` | 独立代码审查 |
| `bmad-sm` | `bmad-sm.md` | Sprint 规划与任务分解 |

## 关键约定

- 每个 agent 执行具体任务时，**必须先调用 `use_skill` 加载对应 skill**
- Skill 提供具体的操作指引，agent 只负责知道"该调哪个 skill"
- 规范更新只需改 workspace rules 或 skill 定义，**不需要同步改 agent**
