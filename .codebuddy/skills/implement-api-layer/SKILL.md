---
name: implement-api-layer
description: Use when 用户已经有接口协议文档、需求文档和 ViewModel 接口，准备实现 Repository、PB 请求封装和响应到 UI 模型的转换逻辑。
---

# 接口层开发

## 目标
根据接口协议文档，自动生成接口层实现代码，包括 Repository 类、Wire PB 网络请求封装、响应数据转换和错误处理。

---

## 触发条件
用户提供以下输入时触发本 skill：
- **新建页面**：接口协议文档（`docs/component/{模块名}/{页面名驼峰}/{页面名下划线}_protocol.md`）+ 评审通过的需求文档
- **已有页面迭代**：基线协议文档（`_protocol.md`）+ diff 协议文档（`diff/{页面名下划线}_protocol_diff.md`）+ 评审通过的需求文档

---

## 输入

| 参数 | 说明 | 是否必须 |
|------|------|----------|
| 接口协议文档（基线） | `_protocol.md`，包含接口定义、请求参数、响应结构、错误码 | ✅ 必须 |
| 接口协议文档（diff） | `diff/_protocol_diff.md`，描述本次迭代新增/修改的接口；新建页面无此项 | 迭代模式必须 |
| 需求文档 | 辅助理解业务语义，生成更准确的注释和错误处理逻辑 | ✅ 必须 |
| ViewModel 接口代码 | Step 4 输出的 VM 接口，包含 UI 层数据模型定义 | ✅ 必须 |

---

## 输出

接口层实现代码，包含：
- **Repository 类**：数据仓库，封装网络请求和 Mock 数据切换
- **网络请求扩展**：Wire PB 请求/响应的发送封装
- **数据转换扩展**：PB 响应到 UI 数据模型的转换函数
- **代码注释**

---

## 执行步骤

### Step 0：模式判断

先检查当前页面目录下是否存在 `diff/` 子目录，且其中包含 `{页面名下划线}_diff.md`：

- **新建模式**：diff 目录或 diff 需求文档不存在，直接执行 Step 1（只读取基线协议文档）
- **迭代模式**：diff 目录及 diff 需求文档均存在，同时读取基线协议文档和 diff 协议文档，只针对 diff 中新增/修改的接口进行开发，不重新生成已有接口

---

### Step 1：前置检查 PB 协议名称

在执行任何步骤之前，先读取接口协议文档，检查每个接口是否已提供 PB 协议名称：

```
读取接口协议文档：
  - 新建页面：docs/component/{模块名}/{页面名驼峰}/{页面名下划线}_protocol.md
  - 已有页面迭代：同时读取基线 _protocol.md 和 diff/_protocol_diff.md
    （若 diff 文档中写「保持原样」，则只需检查基线文档中的接口）

对所有需要开发的接口（新建页面为全部接口，迭代模式为 diff 中新增/修改的接口），检查：
  CHECK: 该接口是否已明确标注了 PB 请求类名（如 stXxxReq）和响应类名（如 stXxxRsp）？
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
2. 提取所有接口的 CMD 命令字、请求/响应消息类型和字段、错误码定义

**已有页面迭代：**
1. 读取基线协议文档 `docs/component/{模块名}/{页面名驼峰}/{页面名下划线}_protocol.md`，了解已有接口全貌
2. 读取 diff 协议文档 `docs/component/{模块名}/{页面名驼峰}/diff/{页面名下划线}_protocol_diff.md`
   - 若 diff 文档写「保持原样」，则无需新增/修改接口层代码，跳过后续步骤
   - 否则，只针对 diff 中**新增或修改**的接口进行开发，不重新生成已有接口
3. 提取 diff 接口的 CMD 命令字、请求/响应消息类型和字段、错误码定义

---

### Step 3：参考项目已有的接口层模式

搜索项目中已有的接口实现，了解项目规范：

1. **Repository 模式**：参考 `FindDramaRepository.kt`
   - `internal class` 修饰
   - 构造参数 `useMock: Boolean = false`
   - 方法返回 `Result<T>` 类型（Kotlin Result）
   - Mock 模式使用 `delay()` 模拟延迟 + 返回 MockData
   - 真实模式使用 Wire PB `Message.send()` 扩展函数

2. **网络发送方式**：参考 `WireSendExt.kt`
   ```kotlin
   // Wire Message 的 send() 扩展函数
   // REQ: 请求 PB Message
   // RSP: 响应 PB Message
   suspend inline fun <reified REQ : Message<REQ, *>, RSP : Message<RSP, *>> REQ.send(
       responseAdapter: ProtoAdapter<RSP>
   ): Result<RSP>
   ```
   - CMD 命令字从请求类名自动推导（去掉 `st` 前缀和 `Req` 后缀）
   - 通过 `PBNetworkManager.send()` 发送
   - 返回 `Result<RSP>`

3. **数据转换模式**：参考 `RspExt.kt`
   - PB 响应到 UI 模型的扩展函数
   - 命名为 `{PBRsp}.to{UIModel}()`
   - 处理可空字段和默认值

---

### Step 4：生成 Repository 代码

在 `shared/src/commonMain/kotlin/com/tencent/weishi/module/{模块名}/{功能名}/repository/` 目录下生成：

```kotlin
package com.tencent.weishi.module.{模块名}.{功能名}.repository

import com.tencent.weishi.module.{模块名}.{功能名}.mock.{PageName}MockData
import kotlinx.coroutines.delay

/**
 * {页面名称} 数据仓库
 * 封装 {页面名称} 相关的网络请求
 *
 * @param useMock 是否使用 Mock 数据，用于无后端环境的独立开发
 */
internal class {PageName}Repository(
    private val useMock: Boolean = false
) {

    /**
     * {接口用途描述}
     * CMD: {命令字}
     */
    suspend fun fetch{DataName}({params}): Result<{UIModel}> {
        if (useMock) {
            delay(500)
            return Result.success({PageName}MockData.mock{DataName}())
        }
        return {PBReqClass}({构建请求参数})
            .send({PBRspClass}.ADAPTER)
            .map { it.to{UIModel}() }
    }

    // 对每个接口重复以上模式...
}
```

---

### Step 5：生成数据转换扩展

在 `shared/src/commonMain/kotlin/com/tencent/weishi/module/{模块名}/{功能名}/ext/` 目录下生成：

```kotlin
package com.tencent.weishi.module.{模块名}.{功能名}.ext

/**
 * PB 响应到 UI 模型的转换扩展函数
 */

/**
 * {PBRsp} → {UIModel}
 */
internal fun {PBRsp}.to{UIModel}(): {UIModel} {
    return {UIModel}(
        // 字段映射，处理可空和默认值
        field1 = this.field1 ?: "",
        field2 = this.field2?.toInt() ?: 0,
        items = this.items.map { it.to{ItemUIModel}() }
    )
}

/**
 * {PBItem} → {ItemUIModel}
 */
internal fun {PBItem}.to{ItemUIModel}(): {ItemUIModel} {
    return {ItemUIModel}(
        // 字段映射
    )
}
```

---

### Step 6：验证代码完整性

检查生成的接口层代码：
1. 每个接口协议文档中定义的接口都有对应的 Repository 方法
2. 每个 Repository 方法都有 Mock 模式支持
3. 数据转换扩展覆盖了所有响应字段
4. 错误处理逻辑与需求文档中的异常 UI 变更对应
5. import 语句完整正确

---

### Step 7：回写 PB 协议名称到协议文档

若本次执行过程中用户补充提供了 PB 协议名称（即 Step 1 触发了暂停并由用户补全），则在接口层代码生成完成后，将 PB 协议名称回写到协议文档中：

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
> 根据找剧页接口协议开发接口层

**执行流程：**
1. 读取 `docs/component/drama/findDramaPage/find_drama_page_protocol.md`
2. 参考 `FindDramaRepository.kt` 和 `WireSendExt.kt`
3. 生成 `{PageName}Repository.kt`（2 个方法：fetchAllCategories + fetchDramaItems）
4. 生成 `RspExt.kt`（PB → UI 模型转换）
5. 验证与 ViewModel 数据模型的对接
