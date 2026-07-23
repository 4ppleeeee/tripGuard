# SmartRun Upload 安装配置指南

## 前置条件

SmartRun Upload 与 SmartRun 共用同一套认证 Token。请先访问 https://smartrun.woa.com/token/list 申请个人 Token。

## 1. 下载二进制包

下载对应平台的 zip 包：

| 平台 | 下载链接 |
|------|---------|
| macOS (Apple Silicon) | [smartrun-upload-mcp-darwin-arm64.zip](https://flowly-cdn.gtimg.com/smartrun/uploader/1.0.2/smartrun-upload-mcp-darwin-arm64.zip) |
| macOS (Intel) | [smartrun-upload-mcp-darwin-x86_64.zip](https://flowly-cdn.gtimg.com/smartrun/uploader/1.0.2/smartrun-upload-mcp-darwin-x86_64.zip) |
| Linux (x86_64) | [smartrun-upload-mcp-linux-x86_64.zip](https://flowly-cdn.gtimg.com/smartrun/uploader/1.0.2/smartrun-upload-mcp-linux-x86_64.zip) |
| Windows (x86_64) | [smartrun-upload-mcp-windows-x86_64.exe.zip](https://flowly-cdn.gtimg.com/smartrun/uploader/1.0.2/smartrun-upload-mcp-windows-x86_64.exe.zip) |

```bash
# 解压（以 macOS Apple Silicon 为例）
unzip smartrun-upload-mcp-darwin-arm64.zip -d /opt/
chmod +x /opt/smartrun-upload-mcp-darwin-arm64/smartrun-upload-mcp-darwin-arm64
```

## 2. 配置 MCP

根据使用的 AI 客户端，选择对应的配置方式：

### CodeBuddy / Cursor / Windsurf / VS Code

配置 `mcp.json`（路径因客户端而异）：
- Cursor: `~/.cursor/mcp.json`
- CodeBuddy: `~/.codebuddy/mcp.json`
- Windsurf: `~/.windsurf/mcp.json`

```json
{
  "mcpServers": {
    "smart-run-upload": {
      "command": "/opt/smartrun-upload-mcp-darwin-arm64/smartrun-upload-mcp-darwin-arm64",
      "env": {
        "SMARTRUN_AUTH_TOKEN": "<your-token>"
      },
      "timeout": 600000
    }
  }
}
```

将 `<your-token>` 替换为在 https://smartrun.woa.com/token/list 申请到的 Token（与 SmartRun MCP 使用同一个 Token）。

### Claude Desktop

编辑 `~/Library/Application Support/Claude/claude_desktop_config.json`：

```json
{
  "mcpServers": {
    "smart-run-upload": {
      "command": "/opt/smartrun-upload-mcp-darwin-arm64/smartrun-upload-mcp-darwin-arm64",
      "env": {
        "SMARTRUN_AUTH_TOKEN": "<your-token>"
      }
    }
  }
}
```

### Windows

```json
{
  "mcpServers": {
    "smart-run-upload": {
      "command": "C:\\tools\\smartrun-upload-mcp-windows-x86_64.exe\\smartrun-upload-mcp-windows-x86_64.exe",
      "env": {
        "SMARTRUN_AUTH_TOKEN": "<your-token>"
      },
      "timeout": 600000
    }
  }
}
```

> 将可执行文件路径替换为解压后的实际路径。

## 环境变量

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| `SMARTRUN_AUTH_TOKEN` | **是** | 无 | 认证 Token，与 SmartRun 共用同一套 Token，前往 https://smartrun.woa.com/token/list 申请 |
| `SMARTRUN_API_URL` | 否 | `https://client.smartrun.tds.qq.com/api/files/upload` | COS 上传 API 地址 |
