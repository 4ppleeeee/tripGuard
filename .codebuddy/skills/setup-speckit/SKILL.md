---
name: setup-speckit
description: Use when 用户要检查、安装或初始化 GitHub Spec Kit（specify CLI / .specify），或在执行 `/speckit.*` 前需要确认环境就绪。
---

# SpecKit 环境检测与初始化

## 目标
检测当前项目是否已安装并初始化 GitHub Spec Kit（spec-driven development 工具），若未安装则自动完成安装和初始化。确保后续可以使用 `/speckit.specify`、`/speckit.plan`、`/speckit.tasks`、`/speckit.implement` 等斜杠命令。

---

## 触发条件
- "检查 speckit"、"安装 speckit"、"初始化 speckit"
- "speckit 环境"、"spec-driven"
- 任何 `/speckit.*` 命令执行前，若检测到环境未就绪

---

## 执行步骤

### Step 1：检测 uv 工具

```
执行: which uv && uv --version
  ├── ✅ 已安装 → 继续 Step 2
  └── ❌ 未安装 → 安装 uv：
       curl -LsSf https://astral.sh/uv/install.sh | sh
       source $HOME/.local/bin/env
       验证: uv --version
```

### Step 2：检测 specify CLI

```
执行: which specify && specify --version 2>/dev/null
  ├── ✅ 已安装 → 继续 Step 3
  └── ❌ 未安装 → 安装 specify-cli：
       uv tool install specify-cli --from git+https://github.com/github/spec-kit.git
       验证: specify --version
```

> **升级已有安装**：若已安装但版本过旧，执行：
> ```bash
> uv tool install specify-cli --force --from git+https://github.com/github/spec-kit.git
> ```

### Step 3：检测项目初始化状态

```
检查: 项目根目录下是否存在 .specify/ 目录？
  ├── ✅ 存在 → 读取 .specify/init-options.json，检查配置：
  │     ├── ai 字段是否为 "codebuddy" → ✅ 配置正确
  │     └── ai 字段不是 "codebuddy" → ⚠️ 提示：当前配置的 AI 为 {ai}，建议重新初始化
  │
  └── ❌ 不存在 → 初始化 speckit：
        cd {项目根目录}
        specify init . --ai codebuddy
        验证: ls .specify/
```

### Step 4：运行环境检查

```bash
cd {项目根目录}
specify check
```

确认输出中包含：
- `● Git version control (available)` ✅
- `● CodeBuddy (available)` ✅
- `Specify CLI is ready to use!` ✅

### Step 5：检测宪法文件

```
检查: .specify/memory/constitution.md 是否存在？
  ├── ✅ 存在 → 宪法已就绪，环境检测全部通过
  └── ❌ 不存在 → 提示用户执行 /speckit.constitution 创建宪法
```

---

## 输出

环境检测完成后，输出状态汇总：

```
✅ SpecKit 环境检测通过

| 检测项 | 状态 | 版本/路径 |
|--------|------|----------|
| uv | ✅ | {version} |
| specify CLI | ✅ | {version} |
| 项目初始化 | ✅ | .specify/ |
| AI 配置 | ✅ | codebuddy |
| 宪法文件 | ✅ / ⚠️ | .specify/memory/constitution.md |

可以使用以下 SpecKit 命令：
- /speckit.constitution  — 创建/更新项目宪法
- /speckit.specify       — 定义需求规格
- /speckit.plan          — 创建技术实施计划
- /speckit.tasks         — 生成任务列表
- /speckit.implement     — 执行实施
```

---

## 错误处理

| 错误 | 原因 | 解决方案 |
|------|------|---------|
| `uv: command not found` | uv 未安装 | `curl -LsSf https://astral.sh/uv/install.sh \| sh` |
| `specify: command not found` | specify-cli 未安装 | `uv tool install specify-cli --from git+https://github.com/github/spec-kit.git` |
| `specify init` 失败 | 目录权限或已初始化 | 检查 `.specify/` 是否已存在 |
| GitHub 网络超时 | 网络不可达 | 检查代理设置，或使用 `--offline` 模式 |
| `specify check` 显示 CodeBuddy 未检测到 | CodeBuddy 未在 PATH 中 | 确认 CodeBuddy IDE 已安装并运行 |

---

## 示例调用

**用户输入：**
> 检查下 speckit 有没有安装

**执行流程：**
1. `which uv` → `/Users/xxx/.local/bin/uv` ✅
2. `which specify` → `/Users/xxx/.local/bin/specify` ✅
3. 检查 `.specify/` → 存在，`init-options.json` 中 `ai=codebuddy` ✅
4. `specify check` → Git ✅, CodeBuddy ✅
5. 检查 `constitution.md` → 存在 ✅
6. 输出汇总：全部通过
