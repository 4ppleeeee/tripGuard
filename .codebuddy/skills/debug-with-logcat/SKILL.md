---
name: debug-with-logcat
description: Use when 用户报告了 bug，但仅靠静态代码无法定位根因，或用户主动要求"通过日志 debug"。此 skill 在关键路径插入带统一 TAG 的日志，引导用户复现 bug，自动抓取 Android 设备 logcat 写入 `docs/log.txt`，并基于日志迭代分析；若信息不足则增量补日志再复现，形成闭环。
---

# Debug With Logcat Skill — 通过日志定位 Bug 闭环

## 目标

当从代码本身难以推断 bug 根因，或者用户明确要求用日志排查时，按以下闭环推进：

```
1. 静态分析 → 锁定可疑代码路径
2. 在关键位置插入带【统一 TAG】的日志
3. 提示用户："请操作 App 复现 bug，复现完成后告诉我"
4. （等用户确认）→ 自动抓取 logcat → 写入 docs/log.txt
5. 用统一 TAG 过滤分析日志
6. 如果定位不出来 → 在新的可疑点继续加日志 → 回到第 3 步
7. 定位到根因 → 给出修复方案
```

每一轮"加日志 → 复现 → 抓 log → 分析"都共用同一个 `TAG`，便于 grep 过滤、便于多轮迭代不丢失上下文。

---

## 触发条件

满足以下任一条件即应使用此 skill：

- 用户描述："这个 bug 我看不出来原因"、"代码看不出问题"、"加点日志看看"
- 用户明确要求："通过日志 debug"、"用 logcat 排查"、"打印日志定位"
- 用户报告了**运行时才会出现**的偶发性 bug（崩溃、状态错乱、数据为空、UI 不刷新、回调没触发等）
- 阅读静态代码后存在多条可疑路径，无法直接判断哪条命中
- 已经多轮静态分析仍无进展，需要切换到运行时验证

---

## 输入

| 参数 | 说明 | 是否必须 |
|------|------|----------|
| Bug 描述 | 现象、复现步骤、期望与实际表现 | ✅ 必须 |
| 可疑模块/文件 | 用户已知的怀疑范围 | 可选 |
| 设备类型 | Android / iOS / 鸿蒙；本 skill 主要覆盖 Android（iOS 仅指导加日志，logcat 抓取不适用） | 可选 |

---

## 前置条件

### 必须：能抓到 logcat

- 设备已连接：`adb devices` 返回至少一台 device
- 找到 adb：项目默认路径为 `~/Library/Android/sdk/platform-tools/adb`（macOS）
- 目标进程在跑：`com.tencent.weishi`（按实际项目应用包名调整）

### 可选：日志框架已就绪

- commonMain 业务日志统一使用 `BaseBizLog`（详见 `add-logging` skill）
- Android 端会通过 Logger 写到 logcat；release 下 `debug{}` 不输出，`fileLog/error` 输出

---

## 核心约定：统一 TAG

每轮 debug 会话**必须**约定一个唯一的、和 bug 强相关的 TAG，比如：

```
DBG_MineProfile      // 个人页问题
DBG_LoginCallback    // 登录回调问题
DBG_FeedsRefresh     // 信息流刷新问题
```

要求：
- 前缀统一用 `DBG_`，方便和业务日志区分
- 后缀用驼峰，描述本次排查的功能
- **本次 debug 全程只用这一个 TAG**（subTag 可以多样，但 TAG 不变）
- 抓 logcat 时只需 `grep "DBG_XXX"` 就能看到全部相关日志

---

## 执行步骤

### Step 1：静态分析 + 锁定可疑路径

1. 阅读 bug 描述，识别涉及的功能/页面/模块
2. 使用 `codebase_search`、`grep_search`、`view_code_item` 探索相关代码
3. 列出 **3~5 条可疑路径**，比如：
   - 数据请求是否成功？
   - 回调是否被触发？
   - 状态是否被覆盖？
   - 某个 if 分支的条件是否符合预期？
4. 把这些可疑点整理成一个简短列表展示给用户确认（可选）

### Step 2：约定 TAG + 插入日志

1. 生成本次 debug 的统一 TAG，例如 `DBG_MineWorks`
2. 在第 1 步识别的每个可疑点上插入日志，**遵循 `add-logging` skill 的 BaseBizLog 规范**
3. 关键：**所有新增日志的 BaseBizLog 对象使用统一的 debug TAG**，便于 grep 过滤

#### 推荐写法 A：临时注入一个 debug 专用 logger

直接在被排查的文件顶部新增一个临时 logger（debug 完成后会一并删除）：

```kotlin
// === DEBUG ONLY: 排查 MineProfile 加载异常 ===
private object DbgMineWorksLog : BaseBizLog("DBG_MineWorks")
```

然后在每个可疑点：

```kotlin
DbgMineWorksLog.debug("Load") { "loadData() 入参 userId=$userId, page=$page" }

DbgMineWorksLog.fileLog("Response", "网络回调 succeed=${rsp.succeed} dataSize=${rsp.list.size}")

DbgMineWorksLog.debug("State") {
    "状态变更前 isLoading=${_isLoading.value} list.size=${_list.value.size}"
}

DbgMineWorksLog.error("Catch", "捕获异常: ${e.message}", e)
```

#### 推荐写法 B：临时全部用 fileLog（保证 release 包也能抓到）

如果当前是 release 包 / 灰度包不便切 debug，把可疑点全部用 `fileLog` 打：

```kotlin
DbgMineWorksLog.fileLog("Load", "loadData userId=$userId page=$page")
DbgMineWorksLog.fileLog("Response", "rsp.succeed=${rsp.succeed} size=${rsp.list.size}")
```

#### 关键日志点清单（按需勾选）

| 类别 | 例子 |
|------|------|
| 方法入口/出口 | 入参、返回值 |
| 分支决策 | 每个 if/when 的条件值和走向 |
| 异步回调 | 回调是否触发？成功还是失败？耗时？ |
| 网络请求 | URL、参数、HTTP code、解析后的关键字段 |
| 状态变更 | StateFlow 旧值/新值、列表 size 前后对比 |
| 异常捕获 | 所有 catch 必须打 error + Throwable |
| 生命周期 | onCreate / onResume / onDestroy 触发 |
| 用户交互 | onClick / onScroll 是否触发 |

### Step 3：提示用户复现

日志加完后，**必须暂停**，以下面这种格式明确告知用户：

```
✅ 已在以下位置加好日志，统一 TAG = `DBG_MineWorks`：
  1. MineProfileTabDataRepo.loadData() — 入参 + 返回
  2. MineProfileWorksCellVM.update() — 状态变更
  3. ProfileFeedsDataSource.fetch() — 网络回调

📱 请操作 App 复现 bug：
  1. 打开个人页
  2. 切换到"作品"Tab
  3. 看到列表为空 / 异常时，告诉我"复现完成"

我会自动抓取 logcat 并分析。
```

**禁止**在用户没回复"复现完成"之前自己抓 logcat（因为日志可能还没产生）。

### Step 4：抓取 logcat 并写入 docs/log.txt

收到用户的"复现完成"信号后，按以下步骤抓日志：

#### 4.1 确认设备 + 目标进程 PID

```bash
~/Library/Android/sdk/platform-tools/adb devices -l
~/Library/Android/sdk/platform-tools/adb shell "ps -A | grep com.tencent.weishi"
```

记下 pid（如 `7809`）。

#### 4.2 抓取并写入 docs/log.txt

**方式一：抓最近全量日志（适合刚复现完）**

```bash
mkdir -p /Users/xiexie/StudioProjects/wesee-core/docs

~/Library/Android/sdk/platform-tools/adb logcat -d \
  --pid=<PID> \
  -v time \
  -t 5000 \
  > /Users/xiexie/StudioProjects/wesee-core/docs/log.txt
```

**方式二：只抓本次 TAG 相关日志（推荐，更聚焦）**

```bash
~/Library/Android/sdk/platform-tools/adb logcat -d \
  --pid=<PID> \
  -v time \
  -t 5000 \
  | grep "DBG_MineWorks" \
  > /Users/xiexie/StudioProjects/wesee-core/docs/log.txt
```

**方式三：抓全量 + 同时保留过滤版**

```bash
~/Library/Android/sdk/platform-tools/adb logcat -d \
  --pid=<PID> \
  -v time \
  -t 8000 \
  > /Users/xiexie/StudioProjects/wesee-core/docs/log.txt

grep "DBG_MineWorks" /Users/xiexie/StudioProjects/wesee-core/docs/log.txt \
  > /Users/xiexie/StudioProjects/wesee-core/docs/log_filtered.txt
```

#### 4.3 验证日志写入成功

```bash
wc -l /Users/xiexie/StudioProjects/wesee-core/docs/log.txt
grep -c "DBG_MineWorks" /Users/xiexie/StudioProjects/wesee-core/docs/log.txt
```

- 如果命中 0 行 → 说明日志没打出来（可能是包没重装、debug{}+release 包、TAG 拼错、流程没走到），告诉用户排查
- 如果命中 < 5 行 → 信息可能不足，下一步可能要补日志
- 如果命中较多 → 进入分析

### Step 5：分析日志

1. 用 `read_file` 读取 `docs/log.txt`（必要时 offset+limit 分段读）
2. 用 `grep_search` 在 `docs/log.txt` 内搜索关键 SubTag
3. 按时间顺序梳理日志，回答以下问题：
   - 期望执行的方法**是否被调用**了？
   - 入参是否符合预期？
   - 分支走的是哪一支？
   - 异步回调**是否触发**？耗时多久？
   - 状态变更是否符合预期？
   - 是否有异常被打印？
4. 找到与"期望行为"不符的最早一条日志 → 这就是 bug 现场

### Step 6：判断信息是否充分

| 情况 | 行动 |
|------|------|
| 已能直接定位根因 | 输出诊断结论 + 修复方案，进入 Step 7 |
| 大致缩小到某个方法但不知具体原因 | 在该方法内部补更细的日志（中间变量、循环每次迭代等），回到 Step 3 |
| 期望的日志根本没打出来 | 说明那条路径没走到，沿着调用链向上加日志（在调用方打），回到 Step 3 |
| 日志显示有异常但堆栈不全 | 把对应 catch 改为 `error("...", e)` 带上 throwable，回到 Step 3 |

每一轮迭代仍然**沿用同一个 TAG**，新加的日志直接续在原文件中。

### Step 7：输出诊断结论 + 清理日志

#### 7.1 输出诊断结论

```markdown
## Bug 诊断结论

### 根因
{一句话描述}

### 关键日志证据
（从 docs/log.txt 摘取的关键几行）
```
05-12 10:47:06.136 I/... [DBG_MineWorks/Load] loadData userId=12345 page=0
05-12 10:47:06.500 I/... [DBG_MineWorks/Response] rsp.succeed=true size=0   ← 这里 size 不应为 0
05-12 10:47:06.501 I/... [DBG_MineWorks/State] 状态变更前 isLoading=true list.size=0
```

### 修复方案
{具体修改代码}

### 影响面
{是否影响其他模块}
```

#### 7.2 清理临时调试日志

询问用户："是否清理本次添加的所有 `DBG_MineWorks` 临时日志？"

得到确认后：
1. `grep_search` 找到所有 `DbgMineWorksLog`、`DBG_MineWorks` 的引用
2. 逐文件删除新增的 logger 对象 + 所有调用
3. 删除 `docs/log.txt`（可选）或保留作为本次 bug 排查归档

---

## adb 命令快速备忘

```bash
# 默认 adb 路径
ADB=~/Library/Android/sdk/platform-tools/adb

# 列出设备
$ADB devices -l

# 查包名进程
$ADB shell "ps -A | grep com.tencent.weishi"

# 清空 logcat 缓冲（让用户复现前用，避免历史日志干扰）
$ADB logcat -c

# 抓最近 N 行（仅指定进程）
$ADB logcat -d --pid=<PID> -v time -t 5000 > docs/log.txt

# 抓最近 N 行 + 过滤 TAG
$ADB logcat -d --pid=<PID> -v time -t 5000 | grep "DBG_XXX" > docs/log.txt

# 实时跟随（一般本 skill 不用，用 -d 一次性抓更适合分析）
$ADB logcat --pid=<PID> -v time | grep "DBG_XXX"

# 看指定级别（W 以上）
$ADB logcat -d --pid=<PID> -v time *:W > docs/log.txt
```

---

## 完整对话示例

> 用户：个人页"作品"Tab 一进去就空了，刷新也没用，看代码看不出来，加点日志查一下。

**Agent**：

1. （静态分析）我看了一下，可疑点有：
   - `MineProfileTabDataRepo.loadFirst()` 是否被触发
   - `ProfileFeedsDataSource.fetch()` 网络请求结果
   - `MineProfileWorksCellVM` 的 list state 是否被覆盖

2. 我会用统一 TAG `DBG_MineWorks` 在这 3 个位置插入日志。

[执行编辑：在 3 个文件中插入临时 logger 和日志]

3. ✅ 日志已加好。请操作 App：
   - 进入个人页 → 切到"作品"Tab → 看到空列表后告诉我"复现完成"
   - 复现前我先帮你清空 logcat 缓冲，避免历史日志干扰

[执行：`adb logcat -c`]

> 用户：复现完成

**Agent**：

[执行：`adb logcat -d --pid=7809 -t 5000 | grep DBG_MineWorks > docs/log.txt`]

[执行：`wc -l docs/log.txt` → 23 行]

[执行：`read_file docs/log.txt`]

分析结果：
- `Load loadFirst()` 触发了 ✅
- `Response rsp.succeed=true size=20` ✅，网络数据是 OK 的
- 但是 `State 状态变更前 isLoading=true list.size=0` 之后**没有"状态变更后"日志** ❌

→ 说明状态更新被某个 if 短路了。继续加日志看 if 分支……

[回到 Step 3，补一轮日志，再让用户复现一次]

---

## 反模式清单

- ❌ **没约定 TAG 就开始加日志** → 抓 logcat 时根本过滤不出来
- ❌ **TAG 取得太短/和业务重名** → 比如 `Test`、`Log`、`Debug`，会和系统/其他模块日志混在一起
- ❌ **不暂停就抓 logcat** → 用户都还没复现 bug，抓出来全是无关日志
- ❌ **抓全量不过滤** → docs/log.txt 几万行，agent 自己也看花眼
- ❌ **只用 `debug{}` 又跑 release 包** → 日志根本不输出
- ❌ **找到根因不清理临时日志** → 临时日志污染代码库
- ❌ **每轮都换新 TAG** → 没法把多轮日志拼在一起看
- ❌ **不写到 docs/log.txt 直接在终端 cat** → 日志太长 agent 上下文截断，且无法多次回看
- ❌ **没确认设备连着就发命令** → adb 报 "no devices" 浪费一轮
- ❌ **加日志时偷懒只在入口加** → 没覆盖分支/回调/异常，定位不到具体环节

---

## 关联 Skill

- `add-logging`：本 skill 的日志写法严格遵循 `add-logging` 的 `BaseBizLog` 规范（debug{} / fileLog() / error() 三档）
- `analyze-recomposition-log`：如果 bug 是 Compose 重组性能问题，先走那个 skill
- `perf-tencent-news-crash`：如果是稳定性 crash，先看 crash 平台是否已有堆栈

