---
name: docs-update-component
description: Use when 用户需要更新组件文档，包括：更新 docs/component-map.md（组件映射）和 docs/page-module-overview.md（页面模块总览）。
---

# 更新组件文档

## Overview

本 skill 负责维护项目中两份核心组件文档：

1. **`docs/component-map.md`**：组件需求文档与代码实现之间的映射索引。
2. **`docs/page-module-overview.md`**：页面模块总览，按业务域组织所有页面和组件的层级结构。

执行时按顺序完成两个大步骤，最终输出统一的变更摘要。

## When to Use

- 用户输入"更新文档"、"更新组件文档"、"维护组件映射"等。
- 新增需求文档后，需要将其同步到 component-map.md 和 page-module-overview.md。
- 代码开发完成后，需要将"待开发"状态更新为实际代码路径。
- 需要为组件补充或修正代码关联。
- 需要调整业务模块分组。

以下场景不要使用本 skill：

- 创建新的组件需求文档（应使用需求分析相关 skill）。
- 修改需求文档内容本身。

---

# 步骤一：更新 docs/component-map.md

## 文档结构说明

### 文件位置

`docs/component-map.md`

### 业务模块分组

文档按业务域分为多个二级标题模块，每个模块下有一个独立的 Markdown 表格：

| component 子目录 | 模块标题   | 说明                        |
|---------------|--------|---------------------------|
| `home`        | **首页** | 推荐页、热点频道、搜索等首页相关组件        |
| `search`      | **首页** | 搜索页归属首页模块                 |
| `drama`       | **短剧** | 短剧广场、榜单、播放页、找剧等           |
| `user`        | **用户** | 个人主页、粉丝、关注、编辑资料、IM、红点等    |
| `message`     | **消息** | 消息主页、新粉丝、系统通知、评论@、点赞收藏、私信 |
| `setting`     | **设置** | 设置主页、通用设置、隐私、推送、关于等       |

### 表格列定义

每个模块表格包含以下 5 列：

| 列名     | 说明                                           | 示例                                                                            |
|--------|----------------------------------------------|-------------------------------------------------------------------------------|
| 组件类型   | `页面` 或 `逻辑服务`                                | `页面`                                                                          |
| 组件id   | 驼峰命名，与 `docs/component/` 下的目录名一致             | `recommendPage`                                                               |
| 组件描述   | 从需求文档一级标题中提取的中文描述                            | `推荐页`                                                                         |
| 组件需求文档 | Markdown 链接，指向需求文档                           | `[recommend_page.md](../docs/component/home/recommendPage/recommend_page.md)` |
| 组件代码   | Markdown 链接，指向代码文件；多个文件用 ` + ` 连接；未开发填 `待开发` | `[Page.kt](../wsCompose/.../Page.kt) + [VM.kt](../wsUser/.../VM.kt)`          |

## Workflow（步骤一）

### Step 1.1：扫描 docs/component 目录

递归扫描 `docs/component/` 下所有子目录，收集组件信息：

1. **组件 id**：从目录名提取（如 `recommendPageCommentPanel`）
2. **所属 component 子目录**：路径中的第一级子目录（如 `home`、`drama`、`user`）
3. **需求文档路径**：每个组件目录下的 `.md` 文件（通常是蛇形命名，如 `recommend_page_comment_panel.md`）
4. **组件描述**：读取需求文档第一行标题（格式通常为 `# xxx` 或 `# xxx（componentId）`），提取中文部分

### Step 1.2：检查文档完整性

1. 读取 `docs/component-map.md` 当前内容。
2. 提取所有表格中已有的组件 id（第二列）。
3. 对比 Step 1.1 扫描结果，识别：
    - **缺失组件**：有需求文档但未记录在 component-map.md 中。
    - **孤立组件**：已记录但对应需求文档不存在（仅提示，不自动删除）。

### Step 1.3：搜索代码位置

对于需要关联代码的组件（新增组件或状态为"待关联"/"待开发"的组件），按以下策略搜索：

#### 1.3.1 推断类名

从组件 id 推断可能的类名：

- `dramaPlayPage` → `DramaPlayPage`、`DramaPlayPageWidget`、`DramaPlayDataRepo`
- `recommendPageCommentPanel` → `CommentPanel`、`CommentPanelPageWidget`、`CommentPanelViewModel`
- `im` → `ImManager`、`IImService`
- `settingPage` → `SettingsPage`

#### 1.3.2 搜索范围

按代码分层在以下模块中搜索：

| 代码层          | 搜索模块                                         | 典型文件命名                                       |
|--------------|----------------------------------------------|----------------------------------------------|
| Compose UI 层 | `wsCompose/src/commonMain/kotlin/`           | `*Page.kt`、`*Bar.kt`、`*Panel.kt`、`*Sheet.kt` |
| Widget 层     | `wsFeeds/`、`wsDrama/`、`wsUser/` 的 `page/` 目录 | `*PageWidget.kt`、`*ChannelWidget.kt`         |
| ViewModel 层  | `wsFeeds/`、`wsDrama/`、`wsUser/` 的 `vm/` 目录   | `*ViewModel.kt`、`*VM.kt`                     |
| DataRepo 层   | `wsFeeds/`、`wsDrama/`、`wsUser/` 的 `page/` 目录 | `*DataRepo.kt`                               |
| 接口/契约层       | `wsCore/src/commonMain/kotlin/`              | `I*Service.kt`、`I*VM.kt`、`*Capability.kt`    |
| 框架层          | `qnFramework/src/commonMain/kotlin/`         | `I*Service.kt`                               |

#### 1.3.3 搜索方法

1. 使用 `codebase_search` 按组件关键词语义搜索。
2. 使用 `grep_search` 按推断的类名精确搜索。
3. 如果搜索无结果，标记为 `待开发`。

#### 1.3.4 代码路径格式

找到代码后，使用相对路径的 Markdown 链接格式：

```
[FileName.kt](../模块名/src/commonMain/kotlin/com/tencent/weishi/.../FileName.kt)
```

多个相关文件用 ` + ` 连接，按 UI → Widget → ViewModel → DataRepo → Interface 的顺序排列。

### Step 1.4：更新 component-map.md

#### 1.4.1 新增组件行

根据组件所属模块，将新行插入到对应模块表格中：

- 已关联代码的组件排在前面。
- "待开发"组件排在末尾。
- 同一模块内按功能相关性分组。

#### 1.4.2 更新已有组件

如果是更新代码关联（从"待开发"/"待关联"变为实际代码路径），直接修改对应行的"组件代码"列。

#### 1.4.3 模块归属判断

根据组件 id 前缀和 component 子目录判断归属模块：

```
docs/component/home/*        → ## 首页
docs/component/search/*      → ## 首页
docs/component/drama/*       → ## 短剧
docs/component/user/*                 → ## 用户
docs/component/message/*              → ## 消息
docs/component/setting/*              → ## 设置
```

---

# 步骤二：更新 docs/page-module-overview.md

## 文档结构说明

### 文件位置

`docs/page-module-overview.md`

### 文档格式

`page-module-overview.md` 是一个按业务域组织的层级列表文档，使用 Markdown 无序列表嵌套：

```markdown
# 页面模块总览

- **首页**
    - 推荐页+热点页：短视频播放&合集浮层&互动
        - [推荐页](component/home/recommendPage/recommend_page.md)
        - [推荐页-评论面板](component/home/recommendPageCommentPanel/recommend_page_comment_panel.md)
    - [首页-热点频道](component/home/hotChannelPage/hot_channel_page.md)

- **短剧**
    - 短剧的推荐和底层
        - [短剧-精选](component/drama/dramaFeaturedPage/drama_featured_page.md)
```

### 业务域与 component-map.md 的对应关系

| component-map.md 模块 | page-module-overview.md 业务域 |
|---------------------|-----------------------------|
| ## 首页               | **首页**                      |
| ## 短剧               | **短剧**                      |
| ## 用户               | **我的**                      |
| ## 消息               | **我的** → Tab 4 → 我的消息       |
| ## 设置               | **我的** → 设置                 |

### 链接格式

page-module-overview.md 中的链接使用相对于 `docs/` 目录的路径（因为该文件本身在 `docs/` 下）：

```
[组件描述](component/{子目录}/{组件id}/{需求文档名}.md)
```

## Workflow（步骤二）

### Step 2.1：对比缺失条目

1. 读取 `docs/page-module-overview.md` 当前内容。
2. 提取所有已有的链接目标路径。
3. 对比步骤一中扫描到的所有组件，识别在 page-module-overview.md 中缺失的组件。

### Step 2.2：确定插入位置

对于每个缺失的组件，根据以下规则确定插入位置：

1. **首页组件**（`home/*`、`search/*`）：
    - 推荐页相关组件 → 插入到 `推荐页+热点页` 子列表下
    - 热点频道相关 → 插入到 `首页-热点频道` 附近
    - 搜索相关 → 插入到 `**搜索**` 业务域下

2. **短剧组件**（`drama/*`）：
    - 精选/推荐相关 → 插入到 `短剧的推荐和底层` 子列表下
    - 剧单/找剧/榜单 → 插入到 `剧单、找剧、榜单` 子列表下
    - 播放页相关 → 根据功能归属插入

3. **用户组件**（`user/*`）：
    - Tab4 相关 → 插入到 `**我的**` → `Tab 4` 下
    - 客态个人页 → 插入到 `**我的**` → `客态个人页` 下

4. **消息组件**（`message/*`）：
    - 插入到 `**我的**` → `Tab 4` → 消息相关条目附近

5. **设置组件**（`setting/*`）：
    - 插入到 `**我的**` → `设置` 子列表下

### Step 2.3：插入新条目

对于每个缺失组件，生成格式为：

```markdown
        - [组件描述](component/{子目录}/{组件id}/{需求文档名}.md)
```

插入到对应位置，保持缩进层级与周围条目一致。

### Step 2.4：检查已有条目

- 如果已有条目的链接路径已失效（对应文件不存在），在摘要中提示但不自动删除。
- 如果已有条目的描述与需求文档标题不一致，可以更新描述。

---

# 输出变更摘要

完成两个步骤后，输出以下格式的统一变更摘要：

```text
📄 组件文档更新完成

━━━ component-map.md ━━━

📌 新增组件：{N} 个
  - {组件id1}（{模块}）→ {代码状态}
  - {组件id2}（{模块}）→ {代码状态}

📌 更新代码关联：{M} 个
  - {组件id1}：待开发 → {实际代码文件}

📌 仍待开发：{K} 个
  - {组件id1}（{模块}）

📌 孤立组件（需求文档不存在）：{L} 个
  - {组件id1}
  （如果没有则不显示此段）

━━━ page-module-overview.md ━━━

📌 新增条目：{P} 个
  - {组件描述1}（{业务域}）
  - {组件描述2}（{业务域}）

📌 失效链接：{Q} 个
  - {链接路径}
  （如果没有则不显示此段）
```

---

# 严格边界

- **只修改 `docs/component-map.md` 和 `docs/page-module-overview.md`**，不修改任何需求文档内容。
- **不创建新的需求文档**。
- **不删除已有行/条目**，即使对应需求文档不存在（仅在摘要中提示）。
- **代码关联必须基于实际搜索结果**，搜索不到则填 `待开发`，不要猜测。
- **保持表格格式一致**（component-map.md）：列宽、分隔线、链接格式与已有行保持统一。
- **保持列表格式一致**（page-module-overview.md）：缩进层级、链接格式与已有条目保持统一。
- **需求文档链接路径**：
    - component-map.md 中使用 `../docs/component/` 开头的相对路径。
    - page-module-overview.md 中使用 `component/` 开头的相对路径。
- **代码链接路径**必须使用 `../模块名/src/commonMain/kotlin/` 开头的相对路径。
- 组件描述从需求文档标题提取，如果标题格式不标准，使用组件 id 的中文翻译或保留英文。
