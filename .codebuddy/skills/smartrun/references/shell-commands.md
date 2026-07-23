# Shell 命令参考

## 概述

`shell` 工具允许在设备上执行系统命令，不同平台支持不同的命令集。

## ⚠️ 平台限制

### Android

**无特殊限制**，可执行任意 `adb shell` 命令（如 `pm`、`am`、`getprop`、`logcat`、`ls`、`cat` 等）。

### iOS

**仅支持以下 13 个 libimobiledevice 命令**，不接受任意 shell 命令：

| 命令 | 用途 |
|------|------|
| `idevice_id` | 获取设备 UDID |
| `ideviceinfo` | 获取设备详细信息 |
| `idevicename` | 获取/设置设备名称 |
| `idevicesyslog` | 查看系统日志 |
| `idevicescreenshot` | 截图 |
| `idevicedate` | 获取/设置设备日期 |
| `idevicediagnostics` | 设备诊断（重启、关机等） |
| `idevicepair` | 设备配对管理 |
| `idevicebackup2` | 备份与恢复 |
| `ideviceinstaller` | 应用安装/卸载/列表 |
| `ideviceprovision` | 描述文件管理 |
| `ideviceimagemounter` | 挂载开发者镜像 |
| `idevicedebug` | 调试相关 |

> **重要**：在 iOS 设备上执行 `ls`、`cat`、`ps` 等通用 shell 命令会失败，必须使用上述白名单中的命令。

---

## Android 设备

执行 `adb shell` 命令：

```bash
# 获取设备信息
shell(command="getprop ro.product.model")
shell(command="getprop ro.build.version.release")

# 查看进程
shell(command="ps -A | grep com.tencent")

# 获取屏幕信息
shell(command="wm size")
shell(command="wm density")

# 查看已安装应用
shell(command="pm list packages -3")  # 第三方应用
shell(command="pm list packages")      # 所有应用

# 清除应用数据
shell(command="pm clear com.example.app")

# 启动 Activity
shell(command="am start -n com.example.app/.MainActivity")

# 发送广播
shell(command="am broadcast -a android.intent.action.BOOT_COMPLETED")

# 查看日志
shell(command="logcat -d -t 100 *:E")  # 最近 100 条错误日志
```

## iOS 设备

执行 `libimobiledevice` 命令：

```bash
# 获取设备信息
shell(command="ideviceinfo")
shell(command="ideviceinfo -k ProductType")
shell(command="ideviceinfo -k ProductVersion")

# 获取设备名称
shell(command="idevicename")

# 查看系统日志
shell(command="idevicesyslog")

# 截图（底层实现）
shell(command="idevicescreenshot")

# 获取设备时间
shell(command="idevicedate")

# 设备诊断
shell(command="idevicediagnostics diagnostics All")

# 重启设备（需谨慎）
shell(command="idevicediagnostics restart")

# 查看已安装应用
shell(command="ideviceinstaller -l")

# 查看证书
shell(command="ideviceprovision list")
```

## 常用场景

### 1. 获取设备详细信息

```python
# Android
info = shell(command="getprop | grep -E 'model|brand|version'")

# iOS
info = shell(command="ideviceinfo -k ProductType && ideviceinfo -k ProductVersion")
```

### 2. 检查应用是否安装

```python
# Android
result = shell(command="pm list packages | grep com.tencent.mm")

# iOS
result = shell(command="ideviceinstaller -l | grep com.tencent.xin")
```

### 3. 强制停止应用

```python
# Android
shell(command="am force-stop com.example.app")
```

### 4. 抓取崩溃日志

```python
# Android - 获取最近的崩溃日志
shell(command="logcat -d -b crash -t 200")

# iOS - 实时日志流（需要后台处理）
shell(command="idevicesyslog | head -500")
```

## 注意事项

1. **权限限制**: 某些命令需要 root/越狱权限，普通设备可能无法执行
2. **命令超时**: 长时间运行的命令可能超时，建议加 `-t` 或 `head` 限制
3. **输出截断**: 大量输出可能被截断，建议使用 `grep` 过滤或 `head/tail` 限制
