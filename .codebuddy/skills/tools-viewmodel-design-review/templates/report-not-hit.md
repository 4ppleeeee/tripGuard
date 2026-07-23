## ViewModel 设计审查报告

### 审查范围
- **当前分支**：`{{CURRENT_BRANCH}}`
- **目标分支**：`{{TARGET_BRANCH}}`
- **本次 MR 修改文件**：
{{CHANGED_FILES}}

---

### 一、结论
- **判定**：未命中审查范围
- **总分**：N/A

---

### 二、说明
本次 MR 修改的文件未涉及 ViewModel 相关代码（接口定义、实现类或 Compose UI 层），无需进行 ViewModel 架构审查。

**未命中原因**：本次修改不包含以下类型的文件变更：
- `wsCore/` 中的 ViewModel 接口定义
- 业务模块（`wsDrama/` / `wsFeeds/` / `wsUser/`）中的 ViewModel 实现类
- `wsCompose/` 中消费 ViewModel 的 UI 组件

---

### 三、建议
如需对已有 ViewModel 进行架构审查，请在提交中包含相关文件的修改，或手动指定需要审查的 ViewModel 接口名称。
