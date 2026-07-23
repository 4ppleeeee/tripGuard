---
description: 
alwaysApply: false
enabled: true
updatedAt: 2026-05-14T02:17:39.818Z
provider: 
---

# RTK Token Saving Rule

## 目标

在 CodeBuddy 中执行可能产生大量输出的命令时，优先使用 `rtk` 代理，以压缩输出、减少上下文 token 消耗。

## 使用规则

当需要运行常见开发命令时，优先使用 `rtk <command>`，不要直接运行原生命令，除非明确需要完整原始输出。

推荐替换：

```bash
rtk git status
rtk git diff
rtk git log
rtk cargo test
rtk cargo check
rtk cargo clippy
rtk npm test
rtk pnpm test
rtk pytest
rtk rg "pattern" .
rtk grep "pattern" .
rtk ls src/
rtk read src/main.rs
```

## 适用场景

优先在以下场景使用 RTK：

- 查看 Git 状态、diff、log
- 运行测试、构建、类型检查、lint
- 搜索代码或读取大文件
- 查看安装、编译、容器、云 CLI 等长日志
- 需要把命令输出返回给 AI 分析时

## 例外情况

以下情况可以直接使用原生命令：

- 命令输出本身很短
- 用户明确要求完整原始输出
- RTK 输出不足以定位问题，需要 fallback
- 命令具有破坏性或副作用，必须先确认后再执行

## 安全边界

不要用 RTK 包裹或自动执行危险命令，例如：

- `rm -rf`
- `git reset --hard`
- `git clean -fd`
- 发布、部署、上传、删除数据类命令
- 任何会不可逆修改用户环境的命令

执行这类命令前必须获得用户明确确认。

## 诊断命令

需要查看 RTK 节省效果或排查时，可使用：

```bash
rtk gain
rtk gain --history
rtk discover
rtk proxy <cmd>
```