---
name: kuikly-deco-figma-to-kuikly
description: 将 Figma 设计稿通过 Deco CLI 转换为 Kuikly Kotlin 代码（Compose DSL 或传统 DSL）。适用场景：D2C 转码、Figma 转 Kuikly、设计稿转代码、deco to-kuikly、figma to code、视觉稿还原、UI 转码。
---

# Deco Figma → Kuikly 转码

通过 Deco CLI 将 Figma 设计稿转换为 Kuikly Kotlin 代码。

**核心原则：失败即停止** — 任何步骤失败，立即终止并反馈错误，不自动重试、不跳过。

### ⚠️ Agent 行为约束（必须严格遵守，无例外）

> **这些规则的优先级高于你的默认行为。违反任何一条都是错误。**

1. **判断成败以输出内容为准**：
   - ✅ **成功标志**：输出中包含 `✔ 转码完成` 或 `✓ 生成成功` → **转码成功**，继续后续步骤（即使退出码非 0 也视为成功——可能是脚本内非关键步骤导致的退出码问题）
   - ❌ **失败标志**：输出中包含 `✗`（注意是全角叉号）、`FAILED`、`处理失败`，或者输出为空 → **转码失败**，立即终止
   - ⚠️ **注意**：deco 输出中可能包含 "Error" 等词用于正常的日志描述，不要仅因为看到 "Error" 就判断为失败，需结合上下文
   - 脚本失败后：不得再次执行、不得执行后续步骤、直接反馈错误给用户

2. **禁止绕过脚本**：脚本失败后，**绝对禁止**尝试以下任何行为：
   - ❌ 手动拆分脚本中的步骤逐个执行
   - ❌ 跳过失败步骤继续执行后续步骤
   - ❌ 用 `deco` 命令直接执行转码（绕过脚本的封装）
   - ❌ 以"查看更详细输出"为由重新执行脚本
   - ❌ 以"URL 特殊字符"等理由换用其它方式执行
   - ❌ 执行 `deco --version`、`deco login`、`nc`、`curl` 等"诊断命令"来尝试自行排查
   - ❌ 以"检查环境/脚本是否存在"为由执行 `ls`、`cat`、`which` 等命令
   - 唯一允许的自动恢复是脚本**内部**的「登录态过期 → `deco login` → 重试」机制（已内置在脚本中，不需要 Agent 手动触发）

3. **禁止自动重试**（零容忍）：脚本失败后不得以任何形式重新执行，包括但不限于：
   - ❌ 加 `2>&1` 或 `2>&1 | cat` 重跑
   - ❌ 换引号/转义方式重跑
   - ❌ 拆分为子命令重跑
   - ❌ 以"需要 approval"为由要求用户批准后重跑
   - ❌ 以"命令被拒绝了"为由再次尝试
   - **唯一允许重新执行的情况**：用户**明确指示**你重新执行（如"再跑一次"、"重试"）

4. **反馈格式**：终止时必须告知用户以下三项，然后**停止一切操作，等待用户指示**：
   - (a) 失败的具体步骤名称
   - (b) 脚本的完整错误输出
   - (c) 建议的修复方式（参考 [TROUBLESHOOTING.md](TROUBLESHOOTING.md)）

5. **识别用户拒绝（disapprove）— 必须同时满足两个条件**：
   - 条件 A：退出码非 0
   - 条件 B：**完全没有任何 stdout/stderr 输出**（0 字节）
   - ⚠️ **只要有任何输出**（哪怕只有一行），就**绝对不是 disapprove**，必须按正常执行结果处理
   - 两个条件都满足时：告知用户"命令未被执行（可能被拒绝），是否需要重新执行？"，等待确认
   - 特别注意：如果输出中有脚本的 Step 1~5 日志，说明脚本确实执行了，**不管退出码是什么**都应按输出内容判断成败

6. **转码耗时正常**：`deco-convert.sh` 内置心跳机制（每 15 秒输出 `⏳ 转码进行中...`）。转码通常需要 30s~2min，看到心跳输出说明任务正常进行：
   - 不得因"等待时间过长"而中断脚本
   - 不得因"一段时间没有新输出"而认为脚本卡死
   - 只要有心跳输出，就耐心等待直到完成

7. **脚本失败后的完整行为规范**：
   - 输出错误报告（按第 4 条格式）
   - **立即停止**，不再调用任何工具（tool call）
   - 不尝试诊断原因（脚本输出中已包含诊断信息）
   - 不尝试执行替代方案
   - 等待用户的下一条指令

## 脚本工具

本 Skill 提供三个可执行脚本，位于 `scripts/` 目录下：

| 脚本 | 功能 | 用法 |
|------|------|------|
| `deco-env-check.sh` | 环境检查（Node.js/npm/Deco CLI 检查与自动安装/更新） | `bash scripts/deco-env-check.sh` |
| `deco-convert.sh` | **一键转码**（含环境检查、URL 校验、DSL 推断、模式探测、转码执行） | `bash scripts/deco-convert.sh "<figma-url>" [--dsl compose\|traditional] [--remote]` |
| `deco-integrate.sh` | 产物集成（查找产物、复制 .kt 文件、提示资源和路由注册） | `bash scripts/deco-integrate.sh [产物目录] [--project-dir <path>] [--pages-dir <path>] [--dry-run]` |

> **推荐用法**：执行 `deco-convert.sh` 完成转码，然后执行 `deco-integrate.sh` 集成产物。
>
> ⚠️ **跨平台说明**：脚本自动检测操作系统（macOS / Linux / Windows WSL），适配 `sed`、`mktemp`、`nc` 等命令差异，无需手动区分。

## 工作流程

复制此清单跟踪进度：

```
转码进度：
- [ ] Step 1: 环境检查（Node.js / npm / Deco CLI）
- [ ] Step 2: 校验 Figma URL
- [ ] Step 3: 判断目标 DSL 类型
- [ ] Step 4: 判断转码模式（本地/远程）
- [ ] Step 5: 执行转码
- [ ] Step 6: 集成产物到项目
```

> 💡 **快捷方式**：Step 1-5 可直接运行 `bash scripts/deco-convert.sh "<figma-url>"`，Step 6 可运行 `bash scripts/deco-integrate.sh`

---

## Step 1: 环境检查

依次检查三项依赖，详细安装步骤见 [ENV-SETUP.md](ENV-SETUP.md)。

**脚本方式**（推荐）：

```bash
bash scripts/deco-env-check.sh
```

**手动方式**：

```bash
# 1. Node.js >= 18
node -v

# 2. npm
npm -v

# 3. Deco CLI
deco --version
```

Deco CLI 安装/更新：

```bash
npm install -g @tencent/deco --registry=https://mirrors.tencent.com/npm/
```

**每日更新控制**：检查 `~/.deco/.last_update` 文件，日期为今天则跳过更新，否则执行更新后写入当天日期。

---

## Step 2: 校验 Figma URL

URL **必须**包含 `node-id` 参数：

```
https://www.figma.com/design/{fileKey}/{fileName}?node-id={nodeId}
```

缺失 `node-id` → ❌ 停止，提示用户：在 Figma 中选中目标 Frame/Component（不要选 Group）→ 右键 → **Copy link to selection**。

---

## Step 3: 判断目标 DSL 类型

按优先级确定：

| 优先级 | 依据 | 结果 |
|--------|------|------|
| 1 | 用户明确指定 | 「Compose」→ `to-kuikly`；「DSL / 传统 DSL」→ `to-kuikly-dsl` |
| 2 | 工程结构推断 | 存在 `@Composable` → Compose；存在 `attr {` / `event {` → 传统 DSL |
| 3 | 两种都有 | 默认 Compose（`to-kuikly`） |
| 4 | 两种都没有 | 默认传统 DSL（`to-kuikly-dsl`） |

> 使用 `deco-convert.sh --dsl compose` 或 `--dsl traditional` 可跳过自动推断。

---

## Step 4: 判断转码模式

探测本地 Figma Desktop MCP（端口 3845）：

```bash
# 用 TCP 连通性检测，避免 HTTP 层面的各种陷阱
nc -z -w 3 127.0.0.1 3845
```

| 结果 | 模式 | 参数 |
|------|------|------|
| 端口可达（nc 返回 0） | 本地模式 | 无需额外参数 |
| 端口不可达（nc 返回非 0） | 远程模式 | 加 `--remote` |

> ⚠️ **不要**用 `curl` 探测 `/sse` 端点（SSE 流式端点 + curl -w 组合会产生异常值）。
> 使用 `deco-convert.sh --remote` 可强制远程模式。

---

## Step 5: 执行转码

**脚本方式**（推荐，Step 1-5 一步到位）：

```bash
# 自动推断 DSL + 自动探测模式
bash scripts/deco-convert.sh "<figma-url>"

# 指定 DSL 类型
bash scripts/deco-convert.sh "<figma-url>" --dsl compose
bash scripts/deco-convert.sh "<figma-url>" --dsl traditional

# 强制远程模式
bash scripts/deco-convert.sh "<figma-url>" --remote
```

> ⏳ **心跳机制**：转码通常需要 30s~2min。脚本内置心跳输出（每 15 秒打印 `⏳ 转码进行中... 已耗时 Xs`），并且 deco 命令的输出会**实时流式显示**。看到心跳信号说明任务正在正常执行，**不要因为"耗时较长"而中断或重试**。

**手动方式**（根据 Step 3 + Step 4 组合命令）：

```bash
# Compose + 本地
deco to-kuikly "<figma-url>"

# Compose + 远程
deco to-kuikly "<figma-url>" --remote

# 传统 DSL + 本地
deco to-kuikly-dsl "<figma-url>"

# 传统 DSL + 远程
deco to-kuikly-dsl "<figma-url>" --remote
```

### 错误处理

> `deco` 即使失败也可能返回 exit code 0，必须通过**输出内容匹配**判断成功与否。
> `deco-convert.sh` 已内置此检测逻辑。

| 输出特征 | 处理 |
|---------|------|
| 包含 `"未登录"` / `"Flowly Token"` / `"token.*过期"` | 登录态过期 → 脚本自动执行 `deco login` → 重新转码 |
| 包含 `"图片上传失败"` + `"Flowly Token"` | 登录态过期导致图片上传失败 → 同上，脚本自动识别并处理 |
| 包含 `✗`、`FAILED`、`处理失败` | ❌ 停止，反馈完整错误信息 |

**登录态过期自动恢复**（`deco-convert.sh` 已内置）：

1. 执行 `deco login`，等待用户在浏览器完成授权
2. 登录成功 → 重新执行转码命令
3. 登录失败 → ❌ 停止，告知用户手动执行 `deco login`

---

## Step 6: 集成产物到项目

**脚本方式**（推荐）：

```bash
# 自动查找最新产物并集成（自动探测项目页面目录）
bash scripts/deco-integrate.sh

# 先预览再执行
bash scripts/deco-integrate.sh --dry-run

# 指定产物目录和页面目标目录
bash scripts/deco-integrate.sh /path/to/artifact --pages-dir src/commonMain/kotlin/com/example/pages/
```

> 脚本自动完成以下操作（agent 无需手动处理）：
> - **自动探测页面目录**：扫描项目中的 `.kt` 文件，查找包含 `@Composable` 或 `attr {` 的目录作为目标
> - **文件重命名**：去掉 Deco 输出的日期后缀（如 `Foo_2026-03-16_12-43.kt` → `Foo.kt`）
> - **package 修复**：将 Deco 默认 package 替换为目标目录对应的 package（从路径中自动推断）
> - **路由检测**：检测 `@Page` 注解并提示路由信息

**手动方式**：

转码产物位于 Deco CLI 输出目录下的最新子目录（脚本自动探测路径）。详细步骤见 [INTEGRATION.md](INTEGRATION.md)。

快速参考：

1. 将 `.kt` 文件复制到项目中存放页面代码的目录
2. 将 `assets/` 图片复制到项目资源目录（如代码引用的是 COS URL 则无需复制）
3. 如果生成代码不含 `@Page` 注解，需在路由配置中注册新页面

---

## 参考文档

- **环境检查详情**：[ENV-SETUP.md](ENV-SETUP.md) — Node.js 安装、Deco CLI 管理
- **产物集成指南**：[INTEGRATION.md](INTEGRATION.md) — 代码放置、路由注册
- **故障排查**：[TROUBLESHOOTING.md](TROUBLESHOOTING.md) — 常见错误与解决方案
- **脚本目录**：[scripts/](scripts/) — 可执行的自动化脚本
