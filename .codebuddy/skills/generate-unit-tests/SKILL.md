---
name: generate-unit-tests
description: Use when 用户需要为 ViewModel 实现生成单元测试，并根据需求文档与 Mock 数据覆盖核心流程、边界场景和异常场景。
---

# VM 单元测试生成

## 目标
根据需求文档和 ViewModel 实现代码，自动生成覆盖核心需求的单元测试代码，并根据需求文档生成不同场景的 Mock 数据。

---

## 触发条件
用户提供以下输入时触发本 skill：
- ViewModel 实现代码
- 需求文档（新建页面提供基线文档；已有页面迭代需同时提供基线文档 + diff 文档）

---

## 输入

| 参数 | 说明 | 是否必须 |
|------|------|----------|
| 需求文档（基线） | 评审通过的基线需求文档，用于理解页面整体功能和已有测试场景 | ✅ 必须 |
| 需求文档（diff） | `diff/{页面名下划线}_diff.md`，描述本次迭代新增/修改的内容；新建页面无此项 | 迭代模式必须 |
| ViewModel 实现代码 | 待测试的 VM 实现代码 | ✅ 必须 |
| Mock 数据文件 | 已有的 Mock 数据，作为测试数据基础 | ✅ 必须 |

---

## 输出

- 单元测试用例代码（覆盖正常流程、边界场景、异常场景）
- 针对各测试场景生成的 Mock 数据
- 测试执行命令

---

## 执行步骤

### Step 0：模式判断

先检查当前页面目录下是否存在 `diff/` 子目录，且其中包含 `{页面名下划线}_diff.md`：

- **新建模式**：diff 目录或 diff 需求文档不存在，只读取基线需求文档，生成完整测试用例
- **迭代模式**：diff 目录及 diff 需求文档均存在，同时读取基线需求文档和 diff 需求文档，只针对 diff 中新增/修改的功能补充测试用例

---

### Step 1：读取输入文件

1. 读取基线需求文档 `docs/component/{模块名}/{页面名驼峰}/{页面名下划线}.md`
2. **迭代模式**：同时读取 diff 需求文档 `docs/component/{模块名}/{页面名驼峰}/diff/{页面名下划线}_diff.md`
   - 若 diff 文档中某项写「保持原样」，则对应功能的测试用例无需新增，沿用已有测试即可
   - 只针对 diff 中**新增或修改**的功能补充测试用例
3. 读取 ViewModel 实现代码
4. 读取已有的 Mock 数据文件

---

### Step 2：参考项目测试基础设施

搜索项目中已有的测试代码，了解测试规范：

1. 查看 `shared/src/commonTest/` 目录下已有的测试文件
2. 了解项目使用的测试框架（kotlin-test + kotlinx-coroutines-test）
3. 参考测试文件的命名规范和组织方式

**测试文件位置：** `shared/src/commonTest/kotlin/com/tencent/weishi/module/{模块名}/{功能名}/`

---

### Step 3：设计测试用例

根据需求文档，设计以下类别的测试用例：

#### 3.1 初始化测试
- ViewModel 创建后，初始状态为 Loading
- 首次数据加载成功后，状态变为 Success
- 首次数据加载失败后，状态变为 Error

#### 3.2 核心功能路径测试
从需求文档的**互动组件**中提取每个用户操作的测试用例：
- 每个 Action 的正常执行路径
- Action 执行后的状态变化验证
- Action 的参数校验

#### 3.3 边界场景测试
从需求文档的**动态组件**中提取边界场景：
- 空数据时的状态
- 列表翻到最后一页（isFinished = true）
- 快速连续操作（测试 Mutex 串行）

#### 3.4 异常场景测试
从需求文档的**接口异常 UI 变更**中提取异常场景：
- 网络请求失败时的状态
- 不同错误码的处理逻辑
- 重试操作的正确性

---

### Step 4：生成测试代码

```kotlin
package com.tencent.weishi.module.{模块名}.{功能名}

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class {PageName}ViewModelTest {

    /**
     * 创建使用 Mock 数据的 ViewModel
     */
    private fun createViewModel(): {PageName}ViewModel {
        return {PageName}ViewModel(useMock = true)
    }

    // === 初始化测试 ===

    @Test
    fun `初始状态应为 Loading`() = runTest {
        val viewModel = createViewModel()
        assertTrue(viewModel.uiStateFlow.value is {PageName}UIState.Loading)
    }

    @Test
    fun `首次加载成功后状态变为 Success`() = runTest {
        val viewModel = createViewModel()
        viewModel.dispatchAction({PageName}Action.Load{XXX}Action)
        // 等待状态变化
        // assertTrue(viewModel.uiStateFlow.value is {PageName}UIState.Success)
    }

    // === 核心功能路径测试 ===

    @Test
    fun `{操作描述} 应正确更新状态`() = runTest {
        val viewModel = createViewModel()
        // 先加载数据
        viewModel.dispatchAction({PageName}Action.Load{XXX}Action)
        // 执行操作
        viewModel.dispatchAction({PageName}Action.{OperationAction}({params}))
        // 验证状态
        val state = viewModel.uiStateFlow.value as {PageName}UIState.Success
        // assertEquals(expected, state.{field})
    }

    // === 边界场景测试 ===

    @Test
    fun `空数据时应正确处理`() = runTest {
        // ...
    }

    // === 异常场景测试 ===

    @Test
    fun `网络请求失败时应显示错误状态`() = runTest {
        // ...
    }
}
```

---

### Step 5：生成测试专用 Mock 数据

如果现有 Mock 数据不足以覆盖所有测试场景，在 Mock 数据文件中补充：

- 空列表场景数据
- 错误响应数据
- 边界值数据

---

### Step 6：执行测试

提供测试执行命令：

```bash
./gradlew :shared:jvmTest --tests "com.tencent.weishi.module.{模块名}.{功能名}.{PageName}ViewModelTest"
```

如果测试不通过，分析失败原因并修复测试代码或标注为需要修复的业务代码问题。

---

## 测试命名规范

- 测试类名：`{PageName}ViewModelTest`
- 测试方法名：使用中文反引号描述，清晰表达测试意图
  - `` `初始状态应为 Loading` ``
  - `` `选择分类后应刷新列表数据` ``
  - `` `网络请求失败时应显示错误状态` ``

---

## 示例调用

**用户输入：**
> 为找剧页 ViewModel 生成单元测试

**执行流程：**
1. 读取需求文档（22 个组件、6 个互动组件、异常场景）
2. 读取 FindDramaViewModel 实现代码
3. 设计 15+ 个测试用例（初始化 3 个 + 功能路径 6 个 + 边界 3 个 + 异常 3 个）
4. 生成 `FindDramaViewModelTest.kt`
5. 执行 `./gradlew :shared:jvmTest --tests "...FindDramaViewModelTest"`
