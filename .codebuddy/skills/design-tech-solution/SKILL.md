---
name: design-tech-solution
description: Use when 用户已经有需求文档、上报文档和接口协议，准备为后续 Mock、UI、接口层与 ViewModel 实现编写技术方案文档。
---

# 技术方案设计

## 目标

根据评审通过的需求文档、上报需求文档、接口协议文档，结合项目存量代码，按照 AI Coding 工作流中各开发步骤（Mock
数据、设计稿还原、数据接口开发、ViewModel 实现编码），分步骤输出详细技术方案文档，包括涉及的类设计、代码位置、数据结构、方法签名等。

---

## 触发条件

用户提供以下输入时触发本 skill：

- 评审通过的需求文档（`docs/component` 目录下的 `.md` 文件）
- 上报需求文档（`_report.md`）
- 接口协议文档（`_protocol.md`）

---

## 输入

| 参数       | 说明                              | 是否必须   |
|----------|---------------------------------|--------|
| 需求文档     | 评审通过的需求文档，包含页面清单、组件清单、交互逻辑、数据需求 | ✅ 必须   |
| 上报需求文档   | 上报事件清单、字段映射、触发条件                | ✅ 必须   |
| 接口协议文档   | 接口定义、字段说明、数据类型、错误码              | ✅ 必须   |
| 已有 VM 代码 | 已有页面的 ViewModel 代码              | 迭代模式必须 |

---

## 模式判断

```
CHECK: 当前页面目录下是否存在 diff/ 子目录，且其中包含 {页面名下划线}_diff.md？
  ├── YES → ✏️ 迭代模式：结合基线文档 + diff 文档，只输出变更部分的技术方案
  └── NO  → 🆕 新建模式：全量生成完整技术方案文档
```

---

## 输出

技术方案文档（Markdown 格式），保存到与需求文档相同的目录：

- **新建模式**：`docs/component/{模块名}/{页面名驼峰}/{页面名下划线}_tech_solution.md`
- **迭代模式**：`docs/component/{模块名}/{页面名驼峰}/diff/{页面名下划线}_tech_solution_diff.md`

文档包含以下章节：

1. ViewModel 接口设计（**必须严格遵循 `design-viewmodel-interface` skill 的规范**）
2. Mock 数据方案
3. 设计稿还原方案
4. 数据接口层方案
5. ViewModel 实现方案

---

## 执行步骤

### Step 0：模式判断

先检查当前页面目录下是否存在 `diff/` 子目录，且其中包含 `{页面名下划线}_diff.md`：

- **新建模式**：diff 目录或 diff 需求文档不存在，直接执行 Step 1
- **迭代模式**：diff 目录及 diff 需求文档均存在，先读取基线技术方案文档（若存在），再结合 diff
  需求分析变更部分，只输出变更章节

---

### Step 1：读取所有输入文档

并行读取以下文档：

1. **需求文档**：`docs/component/{模块名}/{页面名驼峰}/{页面名下划线}.md`
    - 提取：组件清单、互动组件、动态组件、页面生命周期、异常场景
2. **上报需求文档**：`docs/component/{模块名}/{页面名驼峰}/{页面名下划线}_report.md`
    - 提取：上报事件清单、字段映射、触发时机
3. **接口协议文档**：`docs/component/{模块名}/{页面名驼峰}/{页面名下划线}_protocol.md`
    - 提取：接口列表、请求/响应字段、错误码

---

### Step 2：ViewModel 接口设计（严格遵循 design-viewmodel-interface skill）

> ⚠️ **强制要求**：本步骤的 VM 接口设计**必须严格调用并遵循 `design-viewmodel-interface` skill 的全部规范**，不得自行发挥或简化。

**执行方式**：调用 `design-viewmodel-interface` skill，将 Step 1 中提取的需求文档路径作为输入传入。

**核心约束（来自 design-viewmodel-interface）**：

1. **面向 UI 设计，而非面向业务设计**：VM 接口只暴露 UI 可直接消费的基础类型属性和 `onXxx()` 交互方法
2. **三层架构分离**：接口层（wsCore）→ 实现层（业务模块）→ UI 层（wsCompose），代码比例 1:8:1
3. **VM 接口 4 要素**：固定属性、可变属性（StateFlow）、交互方法（onXxx）、子组件 VM
4. **禁止出现**：业务 model 类、var 可变属性、MutableStateFlow、sealed class UIState、sealed interface Action、data class 定义在接口文件中
5. **交互方法参数约束**：参数只传 UI 层已知的值（index、tag 文本等），不传 feedId、userId 等业务数据
6. **子 VM 拆分**：每个独立 UI 区域、列表项、可复用组件对应独立子 VM 接口
7. **子 VM 交互逻辑内聚**：子 VM 实现类直接持有业务数据，交互方法内部直接处理，不通过回调与父 VM 耦合
8. **MutableStateFlow 简化写法**：实现类直接 `override val xxx = MutableStateFlow(...)`，无需拆成双行

**输出产物**：

- 页面级 VM 接口：`I{PageName}PageVM`
- 子组件 VM 接口：`I{ComponentName}VM`
- 代码位置：`wsCore/src/commonMain/.../core/{模块名}/{功能名}/vm/`

**验证清单**（输出前必须逐项检查）：

| # | 检查项 |
|---|--------|
| 1 | VM 接口中不出现任何 model 类、data class、DTO、PB 对象 |
| 2 | VM 接口中不出现 `var`、`MutableStateFlow`、`MutableSharedFlow` |
| 3 | 交互方法参数只传 UI 层已知的值 |
| 4 | 列表项设计为子 VM 接口，而非 data class |
| 5 | 子 VM 接口包含 `onClick()` 等交互方法 |
| 6 | 不继承与职责无关的父接口 |
| 7 | 不定义 `sealed class UIState` 或 `sealed interface Action` |
| 8 | 接口文件中不定义 data class |
| 9 | 每个独立 UI 区域对应一个子 VM 接口 |
| 10 | 可复用组件有独立子 VM 接口 |

---

### Step 3：Mock 数据方案

根据接口协议文档和 Step 2 中设计的 VM 接口，规划 Mock 数据的生成方案：

- Mock 数据需覆盖 VM 接口中所有 StateFlow 属性的各种状态（加载中、成功、失败、空态）
- 列表类数据需覆盖正常列表、空列表、单项列表等边界场景

---

### Step 4：数据接口层方案

根据接口协议文档，规划 Repository 和数据转换层的实现方案：

- Repository 负责网络请求封装和数据缓存
- 数据转换层负责将 DTO/PB 模型转换为 VM 实现类可消费的领域模型
- **注意**：数据转换的最终产物是 VM 实现类的构造参数，而非直接暴露给 UI 层

---

### Step 5：设计稿还原方案

根据需求文档的组件清单，规划 UI 组件拆分和实现方案：

#### 5.3 代码位置

```
shared/src/commonMain/.../module/{模块名}/{功能名}/ui/
├── {PageName}Page.kt          # 页面入口
├── {PageName}ContentView.kt   # 主内容区
└── component/
    ├── {ComponentA}.kt
    └── {ComponentB}.kt
```

### Step 8：保存技术方案文档

**新建模式**：将文档保存到与需求文档相同的目录：

- **目录**：`docs/component/{模块名}/{页面名驼峰}/`
- **文件名**：`{页面名下划线}_tech_solution.md`

**迭代模式**：将 diff 技术方案文档保存到 `diff/` 子目录：

- **目录**：`docs/component/{模块名}/{页面名驼峰}/diff/`
- **文件名**：`{页面名下划线}_tech_solution_diff.md`
- **若无变化**：文件内容写「技术方案保持原样，无变更」

**执行步骤：**

1. 扫描 `docs/component` 下的目录，确认对应页面文件夹路径
2. 将完整技术方案文档写入对应路径

> 文件命名示例：
> - 新建：`docs/component/drama/findDramaPage/find_drama_page_tech_solution.md`
> - 迭代：`docs/component/drama/findDramaPage/diff/find_drama_page_tech_solution_diff.md`

---

## 输出文档模板

```markdown
# {页面名称} - 技术方案文档

> **关联需求文档：** [{页面名下划线}.md](./{页面名下划线}.md)
> **关联协议文档：** [{页面名下划线}_protocol.md](./{页面名下划线}_protocol.md)
> **关联上报文档：** [{页面名下划线}_report.md](./{页面名下划线}_report.md)

---

## 一、ViewModel 接口设计

> 本章节严格遵循 `design-viewmodel-interface` skill 规范，面向 UI 设计。

### 1.1 页面级 VM 接口

（输出 I{PageName}PageVM 接口定义，包含固定属性、可变属性、交互方法、子组件 VM）

### 1.2 子组件 VM 接口

（输出各子组件 VM 接口定义）

### 1.3 VM 接口验证

（逐项列出验证清单的检查结果）

---

## 二、Mock 数据方案

（Mock 数据生成方案）

---

## 三、设计稿还原方案

（UI 组件拆分和实现方案）

---

## 四、数据接口层方案

（Repository、PB 请求封装、数据转换方案）

---

## 五、ViewModel 实现方案

（VM 实现类的编排逻辑、状态管理、上报集成方案）

---