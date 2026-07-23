# 故障排查速查

## 环境问题

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| `nvm: command not found` | nvm 未加载到当前 shell | `source ~/.nvm/nvm.sh`（或 `$NVM_DIR/nvm.sh`）；或重启终端 |
| `npm: command not found` | Node.js 未安装 | 安装 Node.js >= 18（见 ENV-SETUP.md） |
| Deco CLI 安装失败 | npm 源不可达或权限不足 | 检查网络；默认使用 `--registry=https://mirrors.tencent.com/npm/`；或 `sudo npm install -g`；推荐使用 nvm/volta 避免权限问题 |
| Permission error | 系统 npm 需要 sudo | 改用 nvm/volta/fnm 管理的 Node.js |

## 认证问题

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| `未登录` / `未登录 Flowly` | Deco 认证过期或首次使用 | 自动执行 `deco login`；若失败则手动执行 |
| `图片上传失败` + `请检查 Flowly Token` | 登录态（Flowly Token）过期，导致图片无法上传到 CDN | 脚本已内置自动识别：检测到 `Flowly Token` 关键词 → 自动执行 `deco login` → 重试；若自动恢复失败，手动执行 `deco login` 后重新转码 |
| `图片上传失败` 但无 Token 相关提示 | 图片上传服务暂时不可用或网络问题 | 检查网络连接；等待几分钟后重试；确认 Deco 服务状态 |
| 登录超时 | 登录未在限定时间内完成 | 重新执行 `deco login` |
| 浏览器未自动打开 | 系统默认浏览器设置问题 | 从终端输出中手动复制 URL 到浏览器 |
| 重置认证 | 需要清除旧 token | 删除 `~/.deco/config.json` 后重新执行 `deco login` |
| IOA 认证弹窗 / 证书错误 | 腾讯内网 IOA 安全认证拦截了 npm 请求 | 确保 IOA 客户端已登录且证书有效；如果 IOA 证书过期，先更新 IOA 再重试 npm install |
| `UNABLE_TO_VERIFY_LEAF_SIGNATURE` | IOA 证书链不被 Node.js 信任 | 临时方案：`export NODE_TLS_REJECT_UNAUTHORIZED=0` 再重试；长期方案：更新 IOA 证书 |

## URL 问题

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| URL 缺少 `node-id` | 复制了浏览器地址栏的链接 | 在 Figma 中选中元素 → 右键 → Copy link to selection |
| 转码失败（Group 元素） | 选择了 Group 而非 Frame/Component | 选择 Frame 或 Component 再复制链接 |

## 转码问题

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| 本地模式连接超时 | Figma Desktop 未启动或 MCP 未开启 | 启动 Figma Desktop 并启用 Dev Mode |
| `ECONNREFUSED` on 3845 | Figma Desktop 未运行或未打开设计文件 | 启动 Figma Desktop 并打开目标设计文件 |
| 输出目录为空 | 转码中断或网络异常 | 重新执行转码命令 |
| 转码耗时较长（30s~2min） | 正常行为，尤其是远程模式或复杂设计稿 | 脚本内置心跳（每 15 秒输出 `⏳ 转码进行中...`），看到心跳就耐心等待 |
| 长时间无心跳输出（超过 1min） | deco 进程可能异常退出 | 检查 deco 进程是否存在：`ps aux \| grep deco`，必要时手动终止后重试 |
| 远程模式连接超时 | Relay Server 不可达 | 检查网络连接 |
| 转码成功但退出码非 0 | 非关键步骤（如产物目录查找）命令返回非 0 | 以输出内容判断成败：有 `✓ 生成成功` 或 `✔ 转码完成` 即为成功，忽略退出码 |
| Agent 将成功执行误判为 disapprove | Agent 只看了退出码没看输出内容 | 只有退出码非 0 **且完全没有任何输出**（0 字节）才是 disapprove；有输出就一定不是 |
| Agent 在脚本失败后重试/绕过脚本 | Agent 违反行为约束 | 脚本失败后 Agent 必须立即停止，输出错误报告，等待用户指示。禁止重试、禁止执行 `deco --version`/`nc` 等诊断命令、禁止绕过脚本直接调用 `deco` |
| `图片上传失败，无法继续打包` | 登录态过期（Flowly Token 失效）或网络/服务问题 | 脚本已内置自动识别：如果错误信息含 `Flowly Token` 则自动 `deco login` 重试；否则报告错误并停止 |

## 跨平台问题

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| `sed: invalid option -- ''` (Linux) | macOS BSD sed 和 GNU sed 的 `-i` 语法不同 | 脚本已内置 `portable_sed_i` 自动适配，如手动操作：macOS 用 `sed -i ''`，Linux 用 `sed -i` |
| `nc: command not found` | 系统未安装 netcat | 脚本会回退到 `/dev/tcp` 或 `curl`；或手动安装：`apt install netcat-openbsd`（Linux） |
| 产物目录找不到 | Node.js 管理工具（volta/nvm/系统）的全局包路径各不相同 | 脚本内置多路径探测；也可从转码日志中直接复制路径 |

## 快速诊断

**脚本方式**（推荐）：

```bash
# 一键环境诊断
bash scripts/deco-env-check.sh

# 预览产物集成（不实际操作）
bash scripts/deco-integrate.sh --dry-run
```

**手动命令**：

```bash
# 检查 Node.js 版本
node -v

# 检查 Deco CLI 版本
deco --version

# 检查 Figma MCP 是否可用（用 TCP 探测）
nc -z -w 3 127.0.0.1 3845 && echo "MCP 可用" || echo "MCP 不可用"

# 查看 Deco 输出目录（路径因 Node.js 管理工具而异）
cat /tmp/deco-latest-output 2>/dev/null || echo "无缓存，请先执行转码"
```
