# `.claude/` 目录约定

本目录给 **Claude Code** 用。为了让 `.claude/` 和 `.codebuddy/`（CodeBuddy 用）共享同一份真源，以下三个入口都是**相对路径的目录软链**：

```
.claude/agents    → ../.codebuddy/agents
.claude/commands  → ../.codebuddy/commands
.claude/skills    → ../.codebuddy/skills
```

## 真源在哪里改

所有 agent / command / skill **只在 `.codebuddy/` 里维护**。改完对两个工具同时生效，不需要重复拷贝。`.codebuddy/rules/` 和 `.codebuddy/teams/` 是 CodeBuddy 专属概念，Claude Code 不读，也不映射。

## Clone 后注意事项

- **macOS / Linux**：开箱即用，无需操作。
- **Windows**：git 默认不会把 symlink 还原成链接。解决其一：
  1. 启用「开发者模式」后全局开 `git config --global core.symlinks true`，再重新 `git checkout` 一次；或
  2. 手动跑：`rm .claude/agents .claude/commands .claude/skills && cmd /c "mklink /D .claude\agents ..\.codebuddy\agents"`（commands/skills 同理）

检验是否生效：
```bash
ls .claude/skills | head   # 应该列出和 .codebuddy/skills 一样的内容
```

## 本地配置

`.claude/settings.local.json` 已加入 `.gitignore`，每人本地独立积累权限白名单，不互相干扰。如有需要共享给全团队的 Claude Code 配置，放到 `.claude/settings.json`（可提交）。
