---
name: kuikly-troubleshooting-perf-issues
description: 分析和解决 Kuikly、Kotlin Native 性能问题。包括：(1) 页面性能问题：通过 3 步流程诊断（确认问题场景、静态代码分析、运行时日志分析）；(2) 安装包优化：符号内部化、编译选项优化；(3) 编译效率优化：Debug/Release构建优化、增量编译优化。适用场景：页面启动慢、卡顿、延迟、性能优化、加载慢、白屏时间长、滚动卡、响应慢、首屏耗时、包体积大、编译慢、编译耗时。
---

# Kuikly性能问题优化

## 🚨 强制阅读：核心原则

> **CRITICAL**: 

1. 务必做到实事求是，在经过分析仍然不清楚是什么原因的情况下，请直接告知用户实情，给出一般性指引！
2. 在总结的时候，如果并不能非常确定会有多大提升，请避免直接给出估算的数值！


## 使用事件上报（必须执行）

**每次 Skill 被调用时，必须首先执行：**

```bash
bash "${SKILL_DIR}/scripts/beacon-report.sh" &
```

## 问题分类与导航

根据用户描述的问题类型，阅读对应的执行指南进行诊断和解决：

### 1. 页面性能问题

**适用场景**：页面启动慢、卡顿、白屏时间长、滚动卡、响应慢、首屏耗时高

**执行指南**：[perf-execution-guide.md](references/perf-execution-guide.md)

工作流程概要：
1. **确认问题场景** → 明确页面名称、运行模式（内置/JS动态化）
2. **静态代码分析** → 找到页面文件，匹配已知问题模式（通用场景问题 / JS动态化场景问题）
3. **运行时日志分析** → 如静态分析无法定位，引导用户获取事件日志进行分析

覆盖的典型问题：
- 通用场景：生命周期阻塞、首屏过量数据、过度日志、同步Module耗时、attr逻辑过多、数据更新不合理、Observable泄漏
- JS动态化：JSON解析慢、Range比较慢、集合操作慢

### 2. 安装包优化（包体积优化）

**适用场景**：安装包大、包体积大、动态库/静态framework体积优化

**执行指南**：[package-size-execution-guide.md](references/package-size-execution-guide.md)

优化方向：
- **符号内部化**：使用 `kuikly-internalizing-kotlin-native-symbols` skill
- **编译选项优化**：`-Os`、`--gc-sections`、`--pack-dyn-relocs=relr` 等

### 3. 编译效率优化

**适用场景**：编译慢、编译耗时长、构建效率低

**执行指南**：[build-speed-execution-guide.md](references/build-speed-execution-guide.md)

优化方向：
- **Debug Build**：禁用LTO（降80%）、减少内联（降79%）、llvmOptLevel/llvmSizeLevel、增量编译、mold链接器
- **Release Build**：禁用特定Phase（降30%～50%）、LLVM GlobalOpt优化（降70%）
- **使用层面**：预置依赖、避免过多导出

## 参考文档

| 文件 | 说明 |
|------|------|
| [references/perf-execution-guide.md](references/perf-execution-guide.md) | 页面性能问题诊断执行指南 |
| [references/package-size-execution-guide.md](references/package-size-execution-guide.md) | 安装包优化执行指南 |
| [references/build-speed-execution-guide.md](references/build-speed-execution-guide.md) | 编译效率优化执行指南 |
| [references/generic-perf-troubleshooting.md](references/generic-perf-troubleshooting.md) | 通用性能排查工具指引（Profile工具使用等） |
| [references/PageCreateTrace.kt](references/PageCreateTrace.kt) | 事件名称枚举定义代码文件 |
| [references/Shrinker.md](references/Shrinker.md) | Shrinker 符号收缩插件详细配置 |
