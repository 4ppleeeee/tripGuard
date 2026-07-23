# SmartRun 安装配置指南

## 1. 申请 Token

访问 https://smartrun.woa.com/token/list 申请个人 Token。

## 2. 配置 MCP

根据使用的 AI 客户端，选择对应的配置方式：

### CodeBuddy / Cursor / Windsurf

配置 `mcp.json`（路径因客户端而异）：
- CodeBuddy: `~/.codebuddy/mcp.json`
- Cursor: `~/.cursor/mcp.json`
- Windsurf: `~/.windsurf/mcp.json`

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

> ⚠️ **`timeout: 600000`（600 秒）不可省略或调小！**
> SmartRun 的 `screenshot_with_analysis` 工具包含 GPU AI 推理（OmniParser），单次调用可能耗时 10~60 秒。
> 如果 timeout 太小（如默认 30 秒），客户端会提前断开连接，导致返回 "AI 分析失败: context canceled" 错误。

### Claude Desktop

配置 `claude_desktop_config.json`（位于 `~/Library/Application Support/Claude/`）：

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

### 其他 MCP 兼容客户端

通用配置格式：
- **Server URL**: `https://smartrun.woa.com/mcp`
- **认证方式**: HTTP Header `Authorization: Bearer <your-token>`
- **超时设置**: **必须** ≥ 600000 毫秒（SmartRun 包含 AI 分析等长耗时操作，默认 30 秒会导致 context canceled 错误）

将 `<your-token>` 替换为申请到的 Token。
