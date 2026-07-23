---
name: "tools-viewmodel-design-review"
description: "KMM/Compose 场景下的 ViewModel 架构审查 Skill。用于审查已有 VM 是否符合 KMM/Compose 分层规范，输出架构审查报告与打分。"
keywords: "ViewModel,VM,KMM,Compose,架构审查,代码审查,面向UI设计"
triggers:
  - "帮我审查 VM 设计是否合理"
  - "检查这份 ViewModel 是否面向 UI"
  - "ViewModel code review"
  - "审查 VM 架构"
  - "VM 设计审查"
  - "ViewModel 架构检查"
---

# KMM/Compose ViewModel 架构审查 Skill

> 目标：将 VM 设计为"面向 UI 的稳定接口"，将业务细节收敛到实现层；提供可执行审查流程，稳定判断 VM 是否合理。

## 0. 工程配置

**所有工程相关参数集中在 `config.json` 中**，迁移到新工程时只需修改该文件。

**执行审查前，先读取 `config.json` 获取当前工程的配置**：
- `layers.interface.path_marker`：接口层目录标识（当前工程为 `wsCore/`）
- `layers.ui.path_marker`：UI 层目录标识（当前工程为 `wsCompose/`）
- `whitelist_types`：白名单类型列表
- `forbidden_ui_calls`：UI 层禁止调用的业务服务
- `git.target_branch`：对比的目标分支
- `report.output_dir` / `report.md_filename` / `report.html_filename`：报告输出路径

> ⚠️ 以下文档中出现的 "接口层目录"、"UI 层目录"、"白名单"、"目标分支" 等均指 config.json 中的对应配置值，不要使用硬编码。

---

## 1. 执行流程（按顺序执行，优先快速跳出）

### ⚡ Step 0+1：预检 + 静态扫描（一条命令搞定）

**执行脚本（合并预检、文件分类、确定性检查为一步）：**
```bash
cd <项目根目录>
python3 .codebuddy/skills/tools-viewmodel-design-review/scripts/vm_lint.py
```

**脚本输出 JSON 结构：**
```json
{
  "vm_files": [...],
  "interface_files": [...],
  "impl_files": [...],
  "ui_files": [...],
  "issues": [...],
  "summary": {
    "total_vm_files": 0,
    "critical": 0,
    "warning": 0,
    "info": 0,
    "has_hard_violation": false
  },
  "config_used": { ... }
}
```

**快速跳出判定：**
- `total_vm_files == 0`：→ 直接跳到 **Step 5-快速输出**，结论"未命中审查范围"
- `total_vm_files > 0`：→ 继续 Step 2/3/4，**脚本已覆盖的检查项直接采信，AI 仅需聚焦语义判断项**

**脚本已覆盖的确定性检查（无需 AI 重复检查）：**

| 规则 ID | 检查项 | 层级 |
|---------|--------|------|
| H1 | 接口暴露非白名单业务模型 | 接口层 |
| H2 | 接口出现 `var` / `MutableStateFlow` / `MutableSharedFlow` | 接口层 |
| H5 | UI 直接调用禁止的业务服务 | UI 层 |
| H6 | 新增公开方法缺少注释 | 接口层 |
| - | `MutableStateFlow` 在接口层暴露（实现层允许直接 override） | 接口层 |
| - | 结构完整性（有无 val / StateFlow / fun） | 接口层 |
| - | UI 导入实现类而非接口 | UI 层 |

### Step 2/3/4：三层并行审查（⚡ 必须并行执行，不要串行！）

> Step 2、3、4 之间**无数据依赖**，必须通过并行 tool call 同时执行。
> 脚本已覆盖的检查项直接从 JSON 结果中读取，**AI 仅需 read_file 后聚焦以下语义判断项**。

#### Step 2：接口层审查 —— AI 聚焦项

1. **🤖 命名规范**：方法是否面向 UI 语义（`onXxx` / `refresh`），而非面向业务过程
2. **🤖 白名单下钻**：白名单字段是否仅"出现"，未继续扩散接口职责

> ✅ 以下已由脚本完成：结构完整性、业务模型泄漏、可变性违规、注释完整性

#### Step 3：实现层审查 —— AI 聚焦项

1. **🤖 业务收敛**：业务分支（if-else / 路由 / 埋点）是否在实现层而非 UI
2. **🤖 数据转换**：是否将业务对象转为 UI 字段（非直接透传非白名单模型）
3. **🤖 可扩展性**：新增场景能否通过新实现类复用 UI

> ✅ 以下已由脚本完成：MutableStateFlow 在接口层暴露检查

#### Step 4：UI 层审查 —— AI 聚焦项

1. **🤖 无业务分支**：是否无业务 if-else 逻辑（需理解上下文判断是否属于"业务"）
2. **🤖 无模型下钻**：白名单字段可出现但不应下钻读取内部业务字段

> ✅ 以下已由脚本完成：UI 直接调用业务服务、UI 导入实现类

### Step 5：结论判定与输出

**判定标准：**

| 结论 | 条件 |
|------|------|
| 通过 | 无硬性违规，结构清晰 |
| 有条件通过 | 无硬性违规，存在中风险设计问题 |
| 不通过 | 存在任一硬性违规 |
| 未命中审查范围 | 本次 MR 未涉及 ViewModel 相关文件 |

**打分（总分 100）：**

| 维度 | 分值 |
|------|------|
| 接口纯度 | 30 |
| 分层边界 | 25 |
| 可扩展性 | 20 |
| UI 清洁度 | 15 |
| 注释与可读性 | 10 |

90~100 通过 / 75~89 有条件通过 / <75 不通过 / 无 ViewModel 变更 N/A

**⚡ Step 5-快速输出（未命中审查范围时）：**
直接生成两个报告文件即可，不需要执行 Step 1-4。报告输出路径从 config.json 的 `report` 字段读取：
1. Markdown 报告 - 读取 `templates/report-not-hit.md`，替换占位符后输出
2. HTML 报告 - 读取 `templates/not-hit.html`，替换占位符后输出

**正常输出（命中审查范围时）：**
1. Markdown 报告 - 读取 `templates/report.md`，替换占位符后输出
2. HTML 报告 - 根据结论选择对应模板：
   - 通过：读取 `templates/pass.html`
   - 不通过/有条件通过：读取 `templates/issues.html`

---

## 2. 核心审查规则

### 2.1 硬性规则（违反即不通过）

| # | 规则 | 检查位置 |
|---|------|---------|
| H1 | 接口禁止暴露非白名单业务模型 | `wsCore/` |
| H2 | 接口禁止 `var` / 可变 Flow | `wsCore/` |
| H3 | 接口层目录只定义接口不放实现 | `wsCore/` |
| H4 | UI 层目录只消费接口不依赖业务实现 | `wsCompose/` |
| H5 | UI 只拿可直接渲染的数据，只调语义动作方法 | `wsCompose/` |
| H6 | 新增公开方法必须有方法注释 | 所有层 |

> 白名单类型、禁止调用列表等均从 config.json 读取。
> 白名单仅豁免"可出现"，**不豁免** UI 下钻读取其内部业务字段。

### 2.2 高频反模式（发现即标红）

| 反模式 | 问题 | 修复方向 |
|--------|------|---------|
| 接口暴露非白名单业务模型 | `val userInfo: IUserInfo` | 改为 `val userName: String` 等 UI 字段 |
| 接口暴露可变 Flow | UI 可修改状态 | 改为只读 `StateFlow` |
| Compose 写业务分支 | UI 含 `if (vm.xxx.isXxx)` | 改为 `vm.onXxxClick()` |
| Compose 直接调业务服务 | 调用 `appRouter()` / `appReport()` / `appLogin()` | 收敛到 VM 方法 |
| 接口膨胀 | 多业务字段堆叠 | 拆接口 + 多实现类 |

### 2.3 审查模式

| 模式 | 适用场景 | 硬性违规处理 |
|------|---------|-------------|
| **严格审查**（默认） | 新业务、新页面 | 直接不通过 |
| **宽松迁移** | 存量代码重构 | 允许分阶段整改，可"有条件通过" |

用户未指定时：新功能→严格，老代码改造→宽松，无法判断→严格（报告标注"模式待确认"）。

---

## 3. 输出格式

### 3.1 Markdown 报告格式

**文件路径**：`{report.output_dir}/{report.md_filename}`（从 config.json 读取，已存在则覆盖）

**模板文件位置**：`templates/` 目录下
- `report.md` - 命中审查范围（通过 / 有条件通过 / 不通过）
- `report-not-hit.md` - 未命中审查范围

**使用方式**：读取对应模板文件，替换 `{{占位符}}` 后输出。

**report.md 占位符说明：**

| 占位符 | 说明 | 示例 |
|--------|------|------|
| `{{INTERFACE_FILES}}` | 审查的接口文件路径 | 接口层目录下的 VM 文件 |
| `{{IMPL_FILES}}` | 审查的实现文件路径 | 业务模块下的 VM 文件 |
| `{{UI_FILES}}` | 审查的 UI 文件路径 | UI 层目录下的文件 |
| `{{VERDICT}}` | 判定结论 | `通过` / `有条件通过` / `不通过` |
| `{{SCORE}}` | 总分 | `85` |
| `{{CRITICAL_ISSUES}}` | 硬性问题列表 | 按"问题N：标题+位置+原因+风险+修复建议"格式 |
| `{{SUGGESTIONS}}` | 改进建议列表 | 按"建议N：标题+位置+建议"格式 |
| `{{HIGHLIGHTS}}` | 亮点列表 | 编号列表 |

**report-not-hit.md 占位符说明：**

| 占位符 | 说明 |
|--------|------|
| `{{CURRENT_BRANCH}}` | 当前分支名 |
| `{{CHANGED_FILES}}` | 本次 MR 修改的文件列表（每行一个，`  - path/to/file.kt` 格式） |

### 3.2 HTML 报告

**文件路径**：`{report.output_dir}/{report.html_filename}`（从 config.json 读取，已存在则覆盖）

**模板文件位置**：`templates/` 目录下
- `not-hit.html` - 未命中审查范围
- `pass.html` - 审查通过
- `issues.html` - 不通过/有条件通过

**使用方式**：读取对应模板文件，替换 `{{占位符}}` 后输出。

**Header 强制提示（必须）**：
- 生成的 HTML 报告在 `header` 区域必须包含一条**红色粗体**提示，固定文案如下：
- `AI检查的问题不代表一定错误，具有模糊性，流水线卡住时应该找架构师讨论，“流水线挂了一定错”在AI模式下不适用。`
- 该提示在 `not-hit.html`、`pass.html`、`issues.html` 三个模板中都必须出现。

**颜色主题：**
- 🔴 不通过：`#ff6b6b` → `#ee5a6f`
- 🟡 有条件通过：`#ffa726` → `#fb8c00`
- 🟢 通过：`#26a69a` → `#00897b`
- ⚪ 未命中：`#9e9e9e` → `#757575`

**代码块样式要求（重要）：**
- **必须使用浅色背景 + 深色文字**（`background: #f6f8fa; color: #24292e;`），禁止使用深色背景
- 违规高亮使用浅红底 + 红色文字（`background: #fff1f0; color: #cf1322;`）

每个问题卡片需包含：问题位置（文件:行号）、问题代码（高亮违规部分）、违规原因、修复建议。

---

## 4. 执行要求

1. 发现 ViewModel 变更时主动触发审查
2. 审查前先锁定修改点，建立"接口-实现-UI"三层映射
3. 优先识别硬性违规，再给优化建议
4. 每个问题绑定具体修改点（文件路径 + 行号 + 修复建议）
5. 必须包含"是否可合入" + "必须修复项"
6. **必须输出两个报告文件**，无论任何结论
7. HTML 与 Markdown 结论必须一致
8. 硬性违规必须标注因果关系
9. 审查通过时也需给出"已复核修改点"与"通过依据"

---

## 5. 流水线集成脚本

`scripts/` 目录下包含 3 个脚本，覆盖 "静态扫描 → AI 审查 → 结果检查" 全流程：

| 脚本 | 用途 | 阶段 |
|------|------|------|
| `vm_lint.py` | 静态预检 + 文件分类 | 本地 / CI 预检 |
| `vm_review_tool.sh` | 调用 AI 审查服务，下载报告 | CI 审查阶段 |
| `check_viewmodel_design_report.sh` | 读取报告判定结果，返回退出码 | CI 门禁阶段 |

### 5.1 vm_review_tool.sh（AI 审查）

调用远端 AI 审查服务，实时流式输出审查内容，自动下载生成的报告文件。

> **重要**：调用方只需要传入基础 `question`；脚本会在真正发请求前，自动把 `mrUrl` 追加成完整的 MR 审查范围约束。

```bash
MR_URL="https://git.example.com/group/project/-/merge_requests/123"
QUESTION="请审查该 MR 的 ViewModel 设计是否符合规范。"

bash scripts/vm_review_tool.sh \
  --apikey "YOUR_API_KEY" \
  --mr-url "${MR_URL}" \
  --question "${QUESTION}"
```

**配置项**：
- 服务端地址：环境变量 `VM_REVIEW_BASE_URL`（默认 `https://newsai.woa.com`）
- 报告输出目录：从 `config.json` 的 `report.output_dir` 读取
- 外部入参：仅 `apiKey`、`mrUrl`、`question` 三项

### 5.2 check_viewmodel_design_report.sh（结果检查）

读取 Markdown 审查报告，解析"判定"字段，输出机器可读结果。

```bash
# 默认模式：非通过则失败退出
bash scripts/check_viewmodel_design_report.sh

# CI 模式：只输出结果字符串，始终返回 0
bash scripts/check_viewmodel_design_report.sh --result-only

# 指定报告路径
bash scripts/check_viewmodel_design_report.sh ./custom/report.md
```

**输出值**：`PASS` / `FAIL` / `CONDITIONAL` / `SKIPPED` / `UNKNOWN`

**退出码（默认模式）**：

| 退出码 | 含义 |
|--------|------|
| 0 | 通过 或 未命中审查范围 |
| 1 | 不通过 |
| 2 | 有条件通过 |
| 3 | 无法判定 |
| 4 | 报告文件不存在 |

### 5.3 CI 流水线示例

```yaml
steps:
  - name: ViewModel 审查
    script: |
      set -euo pipefail

      REVIEW_SCRIPT=".codebuddy/skills/tools-viewmodel-design-review/scripts/vm_review_tool.sh"
      CHECK_SCRIPT=".codebuddy/skills/tools-viewmodel-design-review/scripts/check_viewmodel_design_report.sh"
      MR_URL='${{ci.mr_url}}'
      REVIEW_QUESTION="请审查该 MR 的 ViewModel 设计是否符合规范。"

      if [[ -z "${MR_URL}" ]] || [[ "${MR_URL}" == "null" ]]; then
        echo "❌ 当前流水线未提供 MR 链接，无法执行 ViewModel 审查"
        exit 1
      fi

      # 1. AI 审查（生成报告）
      bash "${REVIEW_SCRIPT}" \
        --apikey "${VM_REVIEW_API_KEY}" \
        --mr-url "${MR_URL}" \
        --question "${REVIEW_QUESTION}"

      # 2. 检查结果（作为门禁）
      bash "${CHECK_SCRIPT}"
```

**仅按 MR 审查的最简 Shell 版本：**

```bash
set -euo pipefail

MR_URL='${{ci.mr_url}}'
REVIEW_QUESTION="请审查该 MR 的 ViewModel 设计是否符合规范。"

if [[ -z "${MR_URL}" ]] || [[ "${MR_URL}" == "null" ]]; then
  echo "❌ 当前流水线未提供 MR 链接，无法按 MR 增量范围审查"
  exit 1
fi

bash .codebuddy/skills/tools-viewmodel-design-review/scripts/vm_review_tool.sh \
  --apikey "${VM_REVIEW_API_KEY}" \
  --mr-url "${MR_URL}" \
  --question "${REVIEW_QUESTION}"

bash .codebuddy/skills/tools-viewmodel-design-review/scripts/check_viewmodel_design_report.sh
```

---

## 6. 迁移指南

将此 Skill 迁移到新工程时，**只需修改 `config.json`**，无需改动脚本或 SKILL.md。

### 迁移步骤

1. 将整个 `tools-viewmodel-design-review/` 目录复制到新工程的 `.codebuddy/skills/` 下
2. 编辑 `config.json`，按新工程实际情况修改以下字段：

| 字段 | 说明 | 示例 |
|------|------|------|
| `project.name` | 工程名称 | `"MyApp"` |
| `layers.interface.path_marker` | 接口层目录标识 | `"core/"` / `"api/"` |
| `layers.ui.path_marker` | UI 层目录标识 | `"ui/"` / `"presentation/"` |
| `whitelist_types` | 允许出现在接口中的业务类型 | `["IAdItem", "IPageArgs"]` |
| `forbidden_ui_calls` | UI 层禁止调用的业务服务 | `["router.navigate()", "analytics.track()"]` |
| `safe_type_prefixes` | 安全类型前缀（不视为业务模型） | `["IBase", "ICommon"]` |
| `git.target_branch` | 对比的目标分支 | `"origin/main"` |
| `design_spec_path` | 设计规范文档路径（可选） | `"docs/vm-spec.md"` |

3. 验证：运行 `python3 scripts/vm_lint.py`，确认输出 JSON 中 `config_used` 字段反映新配置

### 不同架构的配置示例

**WeSeeCore 工程（当前配置）：**
```json
{
  "project": { "name": "WeSeeCore" },
  "layers": {
    "interface": { "path_marker": "wsCore/" },
    "ui": { "path_marker": "wsCompose/" }
  },
  "git": { "target_branch": "origin/master" },
  "safe_type_prefixes": ["IKmm", "ICompose", "IStruct", "IPopVM", "IBaseVM", "IApp"]
}
```

**标准 Clean Architecture 工程：**
```json
{
  "layers": {
    "interface": { "path_marker": "domain/" },
    "ui": { "path_marker": "presentation/" }
  },
  "git": { "target_branch": "origin/main" }
}
```

**单模块 MVVM 工程：**
```json
{
  "layers": {
    "interface": { "path_marker": "viewmodel/api/" },
    "ui": { "path_marker": "ui/" }
  }
}
```

---

## 7. 设计规范参考

本 Skill 的设计原则基于配置中 `design_spec_path` 指定的规范文档。

当前工程的规范文档位于：`.codebuddy/rules/通用架构规范/viewmodel-design-spec.mdc.md`

核心架构分层（以本工程模块名为准）：
- **接口层 (wsCore)**：纯 KMM，定义 VM 接口、Service / PageFactory / Registry 契约
- **实现层 (wsDrama / wsFeeds / wsUser)**：纯 KMM，提供接口实现类、ViewModel、UseCase、DataRepo
- **UI 层 (wsCompose)**：依赖 Kuikly Compose 框架，实现 UI 组件，只消费 wsCore 的接口
