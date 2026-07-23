# SmartRun - 移动设备远程控制与自动化测试 Skill

SmartRun 是一个 [Cursor Agent Skill](https://docs.cursor.com/agent-skills)，通过 MCP（Model Context Protocol）协议为 AI 提供 Android、iOS、鸿蒙设备的远程控制与自动化测试能力。

## 功能概览

- **设备管理** — 分配/释放远程真机设备，支持按平台、品牌、型号筛选
- **应用管理** — 安装、卸载、启动、停止应用
- **UI 自动化** — 点击、滑动、输入文本、截图，配合服务端视觉模型精准定位 UI 元素
- **调试支持** — 获取设备日志、执行 Shell 命令、创建 TCP 隧道
- **投屏观看** — 分配设备后自动返回投屏 URL，实时查看设备画面
- **人工接管** — 遇到无法自动处理的场景时请求人工介入

## 支持平台

| 平台 | `platform` 参数 |
|------|-----------------|
| Android | `android` |
| iOS | `ios` |
| 鸿蒙 | `harmony` |

## 快速开始

### 1. 申请 Token

前往 [SmartRun Token 管理页](https://smartrun.woa.com/token/list) 申请个人 Token。

### 2. 配置 MCP

在 AI 客户端的 MCP 配置文件中添加 SmartRun 服务：

**Cursor** (`~/.cursor/mcp.json`)：

```json
{
  "mcpServers": {
    "SmartRun": {
      "url": "https://smartrun.woa.com/mcp",
      "headers": {
        "Authorization": "Bearer <your-token>"
      },
      "timeout": 600000
    }
  }
}
```

将 `<your-token>` 替换为你申请到的 Token。

> 也支持 CodeBuddy、Windsurf、Claude Desktop 等 MCP 兼容客户端，详见 [references/setup-guide.md](references/setup-guide.md)。

### 3. 添加 Skill

将本目录作为 Cursor Agent Skill 添加到项目或全局配置中即可。

## 核心工具

| 工具 | 功能 |
|------|------|
| `assign_device` | 分配一台远程真机设备 |
| `release_device` | 释放设备 |
| `install_app` | 安装应用（需外网可访问的 URL） |
| `launch_app` / `stop_app` | 启动/停止应用 |
| `tap` / `swipe` / `input_text` | UI 交互操作 |
| `screenshot` | 截取设备屏幕 |
| `screenshot_with_analysis` | 截图 + 服务端 AI 分析元素坐标 |
| `get_device_logs` | 获取设备日志 |
| `shell` | 执行 Shell 命令（adb / libimobiledevice 等） |
| `get_tunnel` | 创建 TCP 隧道 |
| `take_over` | 请求人工接管 |

## 配合 smartrun-upload 使用

SmartRun 远程服务无法访问用户本地文件系统。当需要安装本地 APK/IPA 时，需先通过 [smartrun-upload](../smartrun-upload/) 的 `upload_file` 工具上传文件获取外网 URL，再调用 `install_app` 安装：

```
upload_file(file_path="~/Desktop/app.apk")  →  file_url
install_app(app_url=file_url)
```

## 目录结构

```
smartrun/
├── SKILL.md                        # Skill 定义（AI 读取的核心文档）
├── README.md                       # 本文件
└── references/
    ├── setup-guide.md              # 详细安装配置指南
    ├── shell-commands.md           # Shell 命令示例（adb/iOS 等）
    ├── system-prompts.md           # GUI 自动化系统提示词
    └── app-mapping.md              # 三平台应用名称映射表
```

## 相关链接

- [SmartRun 平台](https://smartrun.woa.com)
- [Token 申请](https://smartrun.woa.com/token/list)
- [smartrun-upload Skill](../smartrun-upload/) — 本地文件上传服务
