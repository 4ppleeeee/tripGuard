---
name: generate-mock-data
description: Use when 用户需要根据接口协议文档生成多场景 Mock 数据，或在迭代场景下为新增/修改接口补充 Mock 实现。
---

# Mock 数据生成

## 目标
根据接口协议文档和需求文档，自动生成覆盖多场景的 Mock 数据文件，支持在无后端接口时独立运行和调试 UI。

---

## 触发条件
用户提供以下输入时触发本 skill：
- **新建页面**：接口协议文档（`docs/component/{模块名}/{页面名驼峰}/{页面名下划线}_protocol.md`）
- **已有页面迭代**：基线协议文档（`_protocol.md`）+ diff 协议文档（`diff/{页面名下划线}_protocol_diff.md`）

---

## 输入

| 参数 | 说明 | 是否必须 |
|------|------|----------|
| 接口协议文档（基线） | `_protocol.md`，包含字段定义、数据类型、枚举值等 | ✅ 必须 |
| 接口协议文档（diff） | `diff/_protocol_diff.md`，描述本次迭代新增/修改的接口；新建页面无此项 | 迭代模式必须 |
| 需求文档 | 辅助理解各场景下的数据展示形态 | 可选 |
| 设计稿 | 辅助理解 Mock 数据应呈现的视觉效果 | 可选 |

---

## 输出

Mock 数据 Kotlin 文件，提供：
- 正常数据（完整字段、典型值）
- 边界数据（空列表、最大长度字符串、极值数字等）
- 多状态数据（不同业务状态对应的数据）
- 分页模拟（支持翻页和到底判断）

---

## 执行步骤

### Step 0：模式判断

先检查当前页面目录下是否存在 `diff/` 子目录，且其中包含 `{页面名下划线}_diff.md`：

- **新建模式**：diff 目录或 diff 需求文档不存在，直接执行 Step 1（只读取基线协议文档）
- **迭代模式**：diff 目录及 diff 需求文档均存在，同时读取基线协议文档和 diff 协议文档，只针对 diff 中新增/修改的接口生成/更新 Mock 数据

---

### Step 1：前置检查 PB 协议名称

```
读取接口协议文档：
  - 新建页面：docs/component/{模块名}/{页面名驼峰}/{页面名下划线}_protocol.md
  - 已有页面迭代：同时读取基线 _protocol.md 和 diff/_protocol_diff.md
    （若 diff 文档中写「保持原样」，则只需检查基线文档中的接口）

对所有需要生成 Mock 的接口（新建页面为全部接口，迭代模式为 diff 中新增/修改的接口），检查：
  CHECK: 该接口是否已明确标注了 PB 响应类名（如 stXxxRsp）？
    ├── 所有接口均已提供 → 继续执行 Step 1
    └── 存在任意一个接口缺少 PB 类名 → ⛔ 暂停任务
                                         列出所有缺少 PB 协议名称的接口，提示用户：
                                         "以下接口缺少 PB 协议名称，请逐一提供每个接口的
                                         请求类名（stXxxReq）和响应类名（stXxxRsp）：
                                         - 接口1：{接口描述}
                                         - 接口2：{接口描述}
                                         ...
                                         （不能遗漏任何一个接口）"
                                         等待用户补全所有接口的 PB 协议名称后，再继续执行。
```

> ⚠️ **注意**：必须确保**每一个**接口都有对应的 PB 类名，不允许部分提供后继续执行。

---

### Step 2：读取接口协议文档

**新建页面：**
1. 读取 `docs/component/{模块名}/{页面名驼峰}/{页面名下划线}_protocol.md`
2. 提取所有接口的响应数据结构和字段定义
3. 识别列表类接口的分页参数

**已有页面迭代：**
1. 读取基线协议文档 `docs/component/{模块名}/{页面名驼峰}/{页面名下划线}_protocol.md`，了解已有接口全貌
2. 读取 diff 协议文档 `docs/component/{模块名}/{页面名驼峰}/diff/{页面名下划线}_protocol_diff.md`
   - 若 diff 文档写「保持原样」，则无需新增 Mock，跳过后续步骤
   - 否则，只针对 diff 中**新增或修改**的接口生成/更新 Mock 数据
3. 识别列表类接口的分页参数

---

### Step 3：参考项目已有的 Mock 数据模式

搜索项目中已有的 Mock 数据实现：

1. 查看 `shared/src/commonMain/.../mock/` 目录下的文件
2. 参考 `FindDramaMockData.kt` 的实现模式：
   - 使用 `object` 单例提供 Mock 方法
   - 方法名与 Repository 方法对应（如 `mockCategoryLines()`、`mockDramaItems(page, pageSize)`）
   - 返回类型为 UI 层数据模型（非 PB 类型）
   - 列表数据支持分页参数，通过 `page` 和 `pageSize` 控制
   - 设置最大页数限制（如 3 页），超过返回空列表

**项目 Mock 数据规范要点：**
- Mock 文件放在 `module/{模块名}/{功能名}/mock/` 目录下
- 命名为 `{PageName}MockData.kt`，使用 `internal object`
- 数据内容应贴合真实业务场景（中文文案、真实图片 URL 等）
- 通过 Repository 构造参数 `useMock: Boolean` 切换 Mock / 真实网络

---

### Step 4：设计 Mock 数据场景

根据接口协议和需求文档，为每个接口设计以下 Mock 场景：

#### 3.1 正常场景
- **完整数据**：所有字段都有值，列表有多条数据
- **典型值**：使用贴近真实业务的数据（中文名称、真实封面图 URL 等）

#### 3.2 边界场景
- **空列表**：列表类接口返回 0 条数据
- **单条数据**：列表只有 1 条数据
- **长文本**：标题/描述超长
- **分页末尾**：`isFinished = true`
- **极值数据**：播放量为 0、为最大值等

#### 3.3 多状态场景
- 不同业务状态对应的数据变体
- 不同分类/筛选条件下的数据差异

---

### Step 5：生成 Mock 数据代码

在 `shared/src/commonMain/kotlin/com/tencent/weishi/module/{模块名}/{功能名}/mock/` 目录下生成文件：

```kotlin
package com.tencent.weishi.module.{模块名}.{功能名}.mock

/**
 * {页面名称} Mock 数据
 * 用于无后端接口时的独立开发和调试
 */
internal object {PageName}MockData {

    /**
     * Mock {数据描述}
     * 场景：正常数据，{N} 条
     */
    fun mock{DataName}(): List<{Model}> {
        return listOf(
            {Model}(
                // 贴近真实业务的数据
            ),
            // ...
        )
    }

    /**
     * Mock {列表数据描述}
     * 支持分页，最多 {N} 页
     */
    fun mock{ListName}(page: Int, pageSize: Int): List<{Model}> {
        val allItems = listOf(/* 全量数据 */)
        val start = page * pageSize
        if (start >= allItems.size) return emptyList()
        return allItems.subList(start, minOf(start + pageSize, allItems.size))
    }
}
```

---

### Step 6：在 Repository 中集成 Mock 开关

确保 Repository 类支持 Mock 模式切换：

```kotlin
class {PageName}Repository(
    private val useMock: Boolean = false
) {
    suspend fun fetch{Data}(): Result<{Model}> {
        if (useMock) {
            delay(500) // 模拟网络延迟
            return Result.success({PageName}MockData.mock{Data}())
        }
        // 真实网络请求
    }
}
```

---

### Step 7：回写 PB 协议名称到协议文档

若本次执行过程中用户补充提供了 PB 协议名称（即 Step 1 触发了暂停并由用户补全），则在 Mock 数据代码生成完成后，将 PB 协议名称回写到对应协议文档中：

1. **新建页面**：打开 `docs/component/{模块名}/{页面名驼峰}/{页面名下划线}_protocol.md`
   **已有页面迭代**：打开 `docs/component/{模块名}/{页面名驼峰}/diff/{页面名下划线}_protocol_diff.md`
2. 找到每个接口的定义位置，在对应接口下补充或更新 PB 协议名称，格式为：

   ```
   - **请求类名：** `stXxxReq`
   - **响应类名：** `stXxxRsp`
   ```

   例如：
   ```markdown
   ### 接口1：获取分类列表

   - **CMD：** GetCategoryList
   - **请求类名：** `stGetCategoryListReq`
   - **响应类名：** `stGetCategoryListRsp`
   ```

3. 保存文档，确保每个接口都已记录 PB 协议名称，方便后续开发复用。

> 若协议文档在 Step 0 检查时已包含所有 PB 类名（无需用户补充），则跳过此步骤。

---

## 示例调用

**用户输入：**
> 根据找剧页接口协议生成 Mock 数据

**执行流程：**
1. 读取 `docs/component/drama/findDramaPage/find_drama_page_protocol.md`
2. 参考 `FindDramaMockData.kt` 的实现模式
3. 为「获取分类」生成 6 行分类 Mock 数据
4. 为「搜索短剧」生成 12 条短剧 Mock 数据，支持 3 页翻页
5. 生成 `{PageName}MockData.kt` 到 `mock/` 目录
6. 确认 Repository 已集成 `useMock` 开关
