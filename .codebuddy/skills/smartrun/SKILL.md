---
name: smartrun
description: |
  SmartRun 移动设备远程控制与自动化测试平台。通过 MCP 协议提供 Android、iOS、鸿蒙设备的远程控制能力。
  需要上传本地文件（如 APK/IPA 安装包、测试资源等）时，请配合使用 smartrun-upload Skill 的 upload_file 工具将文件上传到 COS 获取外网 URL。
  
  This skill should be used when:
  - User requests real device testing ("在真机上测试", "需要一台手机")
  - User needs mobile UI automation ("打开微信截图", "点击登录按钮")
  - User wants to install apps on devices ("安装 APK", "装个应用")
  - User needs cross-device verification ("Android 和 iOS 分别测试")
  - User requests remote debugging ("获取设备日志", "崩溃日志")
  - User wants GUI Agent automation ("帮我操作手机完成任务")
  
  Trigger phrases: "真机测试", "手机上测试", "分配设备", "移动设备", "安卓/iOS 设备", 
  "截图", "点击", "滑动", "安装应用", "设备日志", "GUI 自动化"
---

# SmartRun 移动设备自动化 Skill

## 入群体验

点击链接或者扫码进群，立即体验：

[点击加入体验群](https://nops.woa.com/pigeon/v1/tools/add_chat?chatId=ww142644362860189)

![SmartRun 体验群二维码]( https://flowly-cdn.gtimg.com/smartrun/assets/qrcode_experience_group.png)

## 核心原则

> **🚨 所有设备操作必须通过 SmartRun MCP 工具完成。**
>
> **坐标定位规则**：需要点击/滑动 UI 元素时，**必须**调用 `screenshot_with_analysis` 获取服务端返回的精确坐标，**严禁**自行看截图估算坐标或用 shell 模拟点击。
>
> - `screenshot_with_analysis` → 服务端视觉模型分析，返回精确像素坐标 ✅
> - 自己看截图估算坐标 → 误差几十~上百像素，操作失败 ❌
>
> **⚠️ `screenshot_with_analysis` 耗时较长**（10~60 秒，含 AI 推理），请注意：
> - 如果返回 **"AI 分析失败: context canceled"**，说明 **MCP 客户端的 HTTP 超时时间太短**，
>   AI 推理还没完成客户端就断开了。此时应提示用户检查 MCP 配置中 `"timeout"` 是否 ≥ 600000（600 秒），
>   参考 [安装配置](#安装配置) 部分。
> - 如果返回其他"AI 分析失败"但错误信息中包含截图 URL，说明截图成功但 AI 推理异常：
>   1. 先重试一次
>   2. 仍失败则降级：用 `screenshot` 截图展示给用户，请用户协助定位目标元素
>   3. 连续 3 次失败时调用 `take_over` 请求人工接管
> - 如果只需要展示画面给用户（不需要元素坐标），直接用更快的 `screenshot`
>
> **操作-确认循环**：每次关键操作后，都应确认结果再继续：
> ```
> 执行操作（tap/install_app/launch_app 等）
>   → wait(duration=1000~3000)
>   → screenshot_with_analysis() 确认界面状态
>   → 根据结果决定下一步
> ```
>
> **投屏 URL**：`assign_device` 成功后会返回投屏 URL，**必须立即将投屏 URL 展示给用户**，让用户可以实时观看设备画面。
>
> **设备释放**：操作完成后**必须**调用 `release_device` 释放设备资源。

---

## 决策流程

根据用户意图，按以下决策路径执行：

### 用户需要安装应用

```
用户需要安装应用？
├── 用户提供了外网可访问的 URL
│   → install_app(app_url=URL)
│
├── 用户提供了本地文件路径（如 ~/Desktop/app.apk）
│   → [smartrun-upload] upload_file(file_path=路径) 获取 file_url
│   → install_app(app_url=file_url)
│   ⚠️ 禁止将本地路径直接传给 install_app，远程服务无法访问本地文件系统
│
├── Android 设备 + 公开应用（如微信、QQ、抖音）
│   → open_url("market://details?id=包名") 通过应用宝安装
│   → 用 GUI 操作点击"安装"按钮
│
└── 用户未提供任何信息
    → 询问用户：提供本地文件路径、外网下载链接、或应用名称
```

### 用户需要 GUI 自动化操作

```
GUI 自动化操作流程：
1. assign_device → 获取 device_id, session_id, 投屏 URL → 将投屏 URL 展示给用户
2. screenshot_with_analysis(target_text="目标文字", timeout=120) → 获取元素坐标列表
   ⚠️ 此步骤耗时较长（10~60s），如果 AI 分析失败但返回了截图 URL，可降级用 screenshot + 询问用户
3. 从返回列表中找到目标元素，用其精确坐标执行 tap/swipe/input_text
4. wait(duration=1000~3000) → 等待界面响应
5. screenshot_with_analysis(timeout=120) → 确认操作结果，获取最新界面状态
6. 重复步骤 2-5 直到完成任务
7. release_device
```

### 用户需要调试/查日志

```
调试流程：
1. assign_device → 获取设备, 投屏 URL → 将投屏 URL 展示给用户
2. launch_app → 启动目标应用
3. 复现问题操作...
4. get_device_logs(level="E") → 获取错误日志
5. screenshot → 截图记录现场
6. release_device
```

---

## 端到端示例

### 示例：在真机上测试本地 APK 并截图

```
用户: 帮我在 Android 真机上测试 ~/Desktop/myapp.apk，打开后截个图

执行流程:
1. [smartrun-upload] upload_file(file_path="~/Desktop/myapp.apk")
   → 返回 file_url = "https://cos.example.com/myapp.apk"

2. [smartrun] assign_device(platform="android")
   → 返回 device_id, session_id, 投屏 URL
   → **将投屏 URL 展示给用户**

3. [smartrun] install_app(device_id, session_id, app_url=file_url)
   → 等待安装完成

4. [smartrun] wait(device_id, session_id, duration=3000)

5. [smartrun] launch_app(device_id, session_id, package_name="从安装结果获取")

6. [smartrun] wait(device_id, session_id, duration=2000)

7. [smartrun] screenshot(device_id, session_id)
   → 将截图返回给用户

8. [smartrun] release_device(session_id)
```

### 示例：GUI 自动化打开微信发朋友圈

```
用户: 帮我在真机上打开微信，发一条朋友圈

执行流程:
1. assign_device(platform="android") → device_id, session_id, 投屏 URL
   → **将投屏 URL 展示给用户**
2. launch_app(device_id, session_id, package_name="com.tencent.mm")
3. wait(device_id, session_id, duration=3000)
4. screenshot_with_analysis(device_id, session_id) → 确认微信已启动，获取界面元素
   ⚠️ 如果出现登录页面 → 截图发给用户，请求协助登录（见"应用登录处理"）
5. screenshot_with_analysis(device_id, session_id, target_text="发现")
   → 从返回列表找到"发现"tab 的坐标
6. tap(device_id, session_id, x=精确坐标, y=精确坐标)
7. wait → screenshot_with_analysis(target_text="朋友圈")
   → 找到"朋友圈"入口坐标
8. tap → wait → screenshot_with_analysis → ... 持续操作直到完成
9. release_device(session_id)
```

---

## 核心工具速查

### 设备管理

| 工具 | 功能 | 关键参数 |
|------|------|----------|
| `assign_device` | 分配设备 | platform(android/ios/harmony), brand, model, duration(秒，默认1800)。**返回值含投屏 URL，必须展示给用户** |
| `release_device` | 释放设备 | session_id (必填) |
| `get_device_info` | 获取设备信息 | device_id |
| `get_installed_apps` | 获取已安装应用列表 | device_id, session_id, include_system |
| `get_device_logs` | 获取设备日志 | device_id, session_id, tag, level, lines |

### 应用管理

| 工具 | 功能 | 关键参数 |
|------|------|----------|
| `install_app` | 安装应用 | device_id, session_id, **app_url** (必须是外网可访问的 URL) |
| `uninstall_app` | 卸载应用 | device_id, session_id, package_name |
| `launch_app` | 启动应用 | device_id, session_id, package_name, activity |
| `stop_app` | 停止应用 | device_id, session_id, package_name |

> 不确定 package_name？先调用 `get_installed_apps` 查询设备上已安装的应用列表。

### UI 交互

| 工具 | 功能 | 关键参数 |
|------|------|----------|
| `tap` | 点击 | device_id, session_id, x, y |
| `double_tap` | 双击 | device_id, session_id, x, y |
| `long_press` | 长按 | device_id, session_id, x, y, duration(ms) |
| `swipe` | 滑动 | device_id, session_id, start_x, start_y, end_x, end_y, duration |
| `input_text` | 输入文本 | device_id, session_id, text |
| `press_back` | 返回键 | device_id, session_id |
| `press_home` | Home 键 | device_id, session_id |
| `open_url` | 打开 URL Scheme | device_id, session_id, url |

### 屏幕与分析

| 工具 | 功能 | 使用场景 |
|------|------|----------|
| `screenshot_with_analysis` | 截图 + 服务端 AI 分析元素坐标 | **定位 UI 元素时必须使用此工具**。⚠️ 包含 AI 推理，**耗时 10~60 秒**，建议 timeout 设为 120。如果返回"AI 分析失败"但截图 URL 存在，可降级使用 `screenshot` |
| `screenshot` | 仅截图（快速，通常 3~5 秒） | 给用户展示当前画面（不要用于自行分析元素位置） |
| `wait` | 等待指定时间 | 操作后等待界面响应，duration 单位为毫秒 |

### 高级功能

| 工具 | 功能 | 关键参数 |
|------|------|----------|
| `shell` | 执行 Shell 命令 | device_id, session_id, command。**Android 支持任意 adb shell 命令；iOS 仅支持 13 个 libimobiledevice 命令**（ideviceinfo、idevicesyslog、ideviceinstaller 等，详见 [references/shell-commands.md](references/shell-commands.md)） |
| `get_tunnel` | 创建 TCP 隧道 | device_id, session_id, remote_port |
| `take_over` | 请求人工接管 | device_id, session_id, reason |

---

## 文件上传（smartrun-upload Skill）

SmartRun 远程服务**无法访问用户本地文件系统**。需要上传本地文件时，使用 `smartrun-upload` Skill 的 `upload_file` 工具：

| 参数 | 说明 |
|------|------|
| `file_path` | 本地文件路径（与 base64_content 二选一） |
| `base64_content` | Base64 编码内容（与 file_path 二选一，需同时提供 filename） |
| `filename` | 文件名（base64 时必填） |
| `client_id` | 客户端 ID（可选，有 SmartRun session 时建议传入） |
| `task_id` | 任务 ID（可选） |
| `device_id` | 设备 ID（可选，有 SmartRun 设备时传入 assign_device 返回的 device_id） |

**返回值**：`file_url`（外网可访问的 URL），可直接用于 `install_app` 的 `app_url` 参数。

**关键数据流**：`upload_file().file_url` → `install_app(app_url=file_url)`

---

## 关键场景处理

### 应用登录

AI 无法自动完成登录验证，需要用户介入：

1. 调用 `screenshot` 获取登录界面截图，返回给用户
2. 询问用户选择登录方式：
   - **账号密码**：用户提供后，用 `input_text` 输入
   - **验证码**：用户提供手机收到的验证码
   - **扫码**：将二维码截图发给用户扫码
3. 多次失败时 → 调用 `take_over` 请求人工接管

### 应用安装（Android 优先从应用宝）

对于 Android 公开应用，**优先通过应用宝安装**，无需安装包：

```
open_url("market://details?id=com.tencent.mm")  # 应用详情页
open_url("market://search?q=抖音")               # 搜索应用
```

应用宝不可用时（iOS、企业内部应用、特定版本）→ 按"决策流程"中的安装分支处理。

---

## 常用应用包名速查

| 应用 | Android 包名 | iOS Bundle ID | 鸿蒙 Bundle Name |
|------|-------------|---------------|-----------------|
| 微信 | `com.tencent.mm` | `com.tencent.xin` | `com.tencent.wechat` |
| QQ | `com.tencent.mobileqq` | `com.tencent.mqq` | `com.tencent.mqq` |
| 应用宝 | `com.tencent.android.qqdownloader` | - | - |
| 抖音 | `com.ss.android.ugc.aweme` | `com.ss.iphone.ugc.Aweme` | `com.ss.hm.ugc.aweme` |
| 支付宝 | `com.eg.android.AlipayGphone` | `com.alipay.iphoneclient` | `com.alipay.mobile.client` |
| 淘宝 | `com.taobao.taobao` | `com.taobao.taobao4iphone` | `com.taobao.taobao4hmos` |
| 京东 | `com.jingdong.app.mall` | `com.360buy.jdmobile` | `com.jd.hm.mall` |
| 美团 | `com.sankuai.meituan` | `com.meituan.imeituan` | `com.sankuai.hmeituan` |
| 小红书 | `com.xingin.xhs` | `com.xingin.discover` | `com.xingin.xhs_hos` |
| 高德地图 | `com.autonavi.minimap` | `com.autonavi.amap` | `com.amap.hmapp` |
| 百度地图 | `com.baidu.BaiduMap` | `com.baidu.map` | `com.baidu.hmmap` |
| bilibili | `tv.danmaku.bili` | `tv.danmaku.bilianime` | `yylx.danmaku.bili` |

> 不确定包名时，先在设备上调用 `get_installed_apps` 查询。
> 完整三平台映射表（含 100+ 应用）参见 [references/app-mapping.md](references/app-mapping.md)。

---

## GUI 自动化行为规则

> 完整版本参见 [references/system-prompts.md](references/system-prompts.md)。

### 通用规则

1. **检查目标应用**：操作前先确认当前是否在目标应用中，不在则 `launch_app` 启动。启动需要包名，参见 [应用映射表](references/app-mapping.md)；不确定时先调 `get_installed_apps` 查询。

2. **处理无关页面**：进入无关页面时先 `press_back`；无效则用 `screenshot_with_analysis(target_text="返回")` 找返回按钮坐标并点击。iOS 无系统返回键，`press_back` 通过边缘滑动模拟，可能不稳定。

3. **等待与重试**：页面未加载时 `wait(duration=2000)`，最多连续 3 次；仍未加载则 `press_back` 重新进入。网络错误时找"重新加载"按钮点击。

4. **滑动查找**：当前页面找不到目标内容时 `swipe` 滑动，每次滑动后 `screenshot_with_analysis` 检查。滑到底部/顶部时反向滑动。

5. **避免死循环**：同一操作连续执行 3 次无效果时改变策略。搜索无结果时返回上一级换词搜索，最多重试 3 次后报告用户。

6. **操作验证**：每次操作后 `screenshot_with_analysis` 确认生效。未生效时 `wait` 后重试，调整坐标，连续失败则跳过并说明。

7. **遵循用户意图**：搜索词可灵活调整（如去掉"群"字、换近义词）。筛选条件无精确匹配时适当放宽。

8. **完成前检查**：结束任务前检查是否完整准确完成，有错选/漏选/多选则返回纠正，最终报告结果并 `release_device`。

### 场景规则

- **购物车**：已有商品被选中时先全选→取消全选→再选目标商品
- **外卖**：店铺购物车有其他商品时先清空；多个外卖尽量同店购买
- **登录处理**：检测到登录页面时截图发用户→询问登录方式→协助输入；多次失败调 `take_over`
- **日期选择**：滑动方向与目标日期越来越远时立即反向

---

## 故障排查

| 问题 | 可能原因 | 解决方案 |
|------|----------|----------|
| 分配设备失败 | 无空闲设备 | 稍后重试或放宽筛选条件（去掉 brand/model 限制） |
| 操作超时 | 网络延迟或设备响应慢 | 增加 timeout 参数值 |
| `screenshot_with_analysis` 返回"AI 分析失败: context canceled" | **MCP 客户端的 HTTP 超时时间太短**，AI 推理还没完成客户端就断开了 | **根因是客户端配置**：提示用户检查 MCP 配置中的 `timeout` 字段，必须 ≥ 600000 毫秒（600 秒）。参考 [references/setup-guide.md](references/setup-guide.md) |
| `screenshot_with_analysis` 返回"AI 分析失败"（非 context canceled） | AI 模型推理失败或 OmniParser 服务异常 | **降级方案**：1) 重试一次；2) 如果错误信息中包含截图 URL，改用 `screenshot` 获取画面 + 让用户协助定位；3) 连续失败时用 `take_over` 请求人工接管 |
| 应用安装失败 | app_url 无法访问 | 确认 URL 可从外网直接下载；本地文件需先 upload_file |
| 点击无响应 | 坐标不准确 | 重新调用 `screenshot_with_analysis` 获取最新坐标 |
| Token 无效 | Token 过期或错误 | 提示用户重新申请：https://smartrun.woa.com/token/list |
| 登录失败 | 验证码/二维码过期 | 重新截图获取最新验证信息；多次失败用 `take_over` |
| 本地文件传不上去 | smartrun-upload 未配置 | 提示用户参考 references/setup-guide.md 配置 |

---

## 安装配置

> 详细的安装配置步骤请参考 [references/setup-guide.md](references/setup-guide.md)。

快速概要：
- **SmartRun MCP**：远程服务，URL `https://smartrun.woa.com/mcp`，需 Bearer Token 认证
- **SmartRun Upload MCP**（可选，独立 Skill）：本地 stdio 服务，用于上传本地文件，安装配置请参考 `smartrun-upload` Skill

> ⚠️ **关键配置：MCP 客户端 timeout 必须 ≥ 600000 毫秒（600 秒）**
>
> SmartRun 的 `screenshot_with_analysis` 包含 GPU AI 推理，单次调用可能耗时 10~60 秒。
> 如果 MCP 客户端的 HTTP timeout 太小（如默认 30 秒），会导致客户端提前断开连接，
> 服务端返回 "AI 分析失败: context canceled" 错误。
>
> 如果遇到此错误，请提示用户检查 MCP 配置中的 `"timeout": 600000` 是否正确设置。

---

## 参考文档

- [references/system-prompts.md](references/system-prompts.md) — SmartRun GUI 自动化系统提示词
- [references/app-mapping.md](references/app-mapping.md) — 三平台应用名称映射表（Android / iOS / 鸿蒙）
- [references/setup-guide.md](references/setup-guide.md) — MCP 安装配置指南
- [references/shell-commands.md](references/shell-commands.md) — Shell 命令参考
