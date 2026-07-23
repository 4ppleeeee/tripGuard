---
name: smartrun-upload
description: |
  SmartRun Upload MCP Server - 本地文件上传到 SmartRun COS 服务。通过本地 MCP 服务将文件上传到云端，获取可外网访问的 URL。
  本 Skill 也会被 smartrun Skill 在需要上传本地文件时自动调用（如用户提供本地 APK 路径需要安装到设备）。

  This skill should be used when:
  - User needs to upload local files to COS ("上传文件到 COS", "把文件传到云端")
  - User needs a public URL for a local file ("获取文件下载链接", "生成外网链接")
  - User wants to upload APK/IPA for SmartRun device installation ("上传安装包", "传 APK")
  - User needs to upload screenshots or images ("上传截图", "把图片传上去")
  - User provides a local file path during SmartRun device operations (auto-triggered by smartrun Skill)

  Trigger phrases: "上传文件", "上传到 COS", "传到云端", "生成下载链接", "上传安装包",
  "上传 APK", "上传 IPA", "文件上传", "本地文件上传", "上传截图"
---

# SmartRun Upload 文件上传 Skill

## 概述

本地运行的 MCP 服务，将文件上传到 SmartRun COS 云存储，返回可外网访问的 URL。

**核心用途**：
- 将本地 APK/IPA 上传获取 URL → 配合 smartrun Skill 的 `install_app` 安装到设备
- 将本地文件/截图上传获取外网分享链接

---

## 工具：upload_file

### 参数

| 参数 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `file_path` | string | 与 base64_content 二选一 | 本地文件路径（支持 `~` 展开），如 `/Users/xxx/Desktop/app.apk` |
| `base64_content` | string | 与 file_path 二选一 | Base64 编码的文件内容（适合截图等无本地路径的场景） |
| `filename` | string | base64_content 时必填 | 文件名，如 `screenshot.png`（file_path 模式自动从路径提取） |
| `client_id` | string | 否 | 客户端 ID，默认 `default_client` |
| `task_id` | string | 否 | 任务 ID，默认 `default_task` |
| `device_id` | string | 否 | 设备 ID，默认 `default_device`。**如果已通过 smartrun 分配了设备，应传入 assign_device 返回的 device_id** |

### 返回值

成功：
```json
{
  "success": true,
  "file_url": "https://cos.example.com/path/to/file.apk",
  "filename": "app.apk",
  "message": "文件上传成功"
}
```

失败：
```json
{
  "success": false,
  "error": "错误描述",
  "error_code": "ERR_XXXX"
}
```

**关键**：成功后的 `file_url` 可直接作为 smartrun `install_app` 的 `app_url` 参数。

---

## 与 smartrun Skill 协作

### 典型流程：上传本地安装包 → 安装到设备

```
1. [smartrun-upload] upload_file(file_path="~/Desktop/app.apk")
   → file_url = "https://cos.example.com/app.apk"

2. [smartrun] assign_device(platform="android")
   → device_id, session_id

3. [smartrun] install_app(device_id, session_id, app_url=file_url)
```

数据流：**upload_file().file_url → install_app(app_url)**

### 何时自动触发

当用户在 smartrun 操作中提供了本地文件路径（如 `~/Desktop/app.apk`），应自动调用 `upload_file` 上传，而非将本地路径直接传给 `install_app`（远程服务无法访问本地文件系统）。

### device_id 传递

如果上传发生在已分配 SmartRun 设备的上下文中，应将 `assign_device` 返回的 `device_id` 传给 `upload_file`：

```
upload_file(file_path="~/app.apk", device_id=当前设备的device_id)
```

---

## 错误处理

| 错误码 | 说明 | AI 应采取的行动 |
|--------|------|----------------|
| `ERR_6002` | 参数错误 | 检查：file_path 和 base64_content 必须二选一；base64 模式需提供 filename |
| `ERR_6010` | 未配置 Token | 提示用户在 MCP 配置的 env 中添加 `SMARTRUN_AUTH_TOKEN`，Token 与 SmartRun 共用，前往 https://smartrun.woa.com/token/list 申请 |
| `ERR_6011` | 上传失败 | 告知用户上传失败，检查网络连接和文件是否有效 |
| `ERR_6012` | 上传超时 | 告知用户文件可能过大或网络慢，建议稍后重试 |
| `ERR_1005` | 文件不存在 | 请用户确认文件路径是否正确 |

---

## 安装配置

> 详细步骤请参考 [references/setup-guide.md](references/setup-guide.md)。

快速概要：
- 下载对应平台的二进制包并解压
- 在 MCP 配置中添加 `smart-run-upload` 服务，设置 `SMARTRUN_AUTH_TOKEN` 环境变量（与 SmartRun 共用同一套 Token，前往 https://smartrun.woa.com/token/list 申请）
