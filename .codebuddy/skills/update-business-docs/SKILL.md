---
name: update-business-docs
description: Use when 用户输入"更新业务文档"，自动扫描 docs/component 目录下的需求文档，将新增文档关联到 page-module-overview.md 和 component-map.md 中。
---

# 更新业务文档

## Overview
自动扫描 `docs/component` 目录下的所有组件/页面需求文档，执行以下两项同步任务：
1. **关联到 page-module-overview.md**：将 `docs/component` 下尚未出现在 `docs/page-module-overview.md` 中的需求文档，以 Markdown 链接形式补充到对应的模块分类下。
2. **关联到 component-map.md**：将 `docs/component` 下尚未出现在 `docs/component-map.md` 中的组件，补充为新行到映射表中。

## When to Use
- 用户输入"更新业务文档"。
- 用户要求同步 component 目录下的文档到总览文件。
- 用户要求检查文档关联是否完整。

以下场景不要使用本 skill：
- 用户要求创建新的组件需求文档（应使用对应的需求分析 skill）。
- 用户要求修改某个具体组件的需求内容。

## Workflow

### Step 1：扫描 docs/component 目录

递归扫描 `docs/component/` 下所有子目录，收集每个组件的信息：

1. **组件目录路径**：如 `docs/component/home/recommendPageCommentPanel/`
2. **需求文档路径**：每个组件目录下与目录同名的 `.md` 文件（如 `recommend_page_comment_panel.md`），这是主需求文档
3. **组件 id**：从目录名提取，即驼峰命名的目录名（如 `recommendPageCommentPanel`）
4. **所属模块**：从路径中的第一级子目录提取（如 `home`、`drama`、`user`）
5. **组件名称**：从主需求文档的第一行标题中提取（格式通常为 `# xxx（componentId）`）

### Step 2：关联到 page-module-overview.md

1. 读取 `docs/page-module-overview.md` 的当前内容。
2. 提取其中已有的所有 `component/` 链接路径。
3. 对比 Step 1 扫描结果，找出**尚未出现在 page-module-overview.md 中的文档**。
4. 对于每个缺失的文档，根据其所属模块（`home` → 首页、`drama` → 短剧、`user` → 我的），将链接插入到对应模块分类下的合适位置。
5. 链接格式为：`[组件中文名](component/{模块}/{组件目录}/{需求文档文件名})`

**模块映射关系**：
| component 子目录 | page-module-overview.md 中的分类 |
|---|---|
| `home` | **首页** |
| `drama` | **短剧** |
| `user` | **我的** |

**插入规则**：
- 在对应模块分类下，找到最相关的子分类位置插入。
- 如果无法确定子分类，则追加到该模块分类的末尾。
- 保持缩进层级与已有条目一致。
- 不要修改已有条目，只做新增。

### Step 3：关联到 component-map.md

1. 读取 `docs/component-map.md` 的当前内容。
2. 提取其中已有的所有组件 id（表格第二列）。
3. 对比 Step 1 扫描结果，找出**尚未出现在 component-map.md 中的组件**。
4. 对于每个缺失的组件，在表格末尾追加新行。
5. 新行格式：

```
| 页面 | {组件id} | component/{模块}/{组件目录}/{需求文档文件名} | 待关联 |
```

**组件代码列的填写规则**：
- 默认填写 `待关联`。
- 如果能在项目代码中通过组件 id 或组件名称搜索到明确对应的代码文件/类名，则填写实际的代码类名。
- 搜索范围包括：`wsCompose`、`wsDrama`、`wsFeeds`、`wsUser` 等业务模块。
- 搜索策略：用组件 id 的关键词（如 `CommentPanel`、`CollectionEpisode`）在代码库中搜索 Fragment、Activity、Page、Widget、ViewModel 等类名。

### Step 4：输出变更摘要

完成编辑后，输出以下格式的变更摘要：

```text
📄 文档同步完成

📌 page-module-overview.md 变更：
  ✅ 新增关联 {N} 个文档
  - {组件名1} → {模块分类}
  - {组件名2} → {模块分类}
  （如果没有新增则显示：✅ 已是最新，无需更新）

📌 component-map.md 变更：
  ✅ 新增关联 {M} 个组件
  - {组件id1}
  - {组件id2}
  （如果没有新增则显示：✅ 已是最新，无需更新）
```

## 严格边界

- 只做文档关联和同步，不修改任何需求文档的内容。
- 不修改 `page-module-overview.md` 中已有的条目文字或链接。
- 不修改 `component-map.md` 中已有行的内容。
- 不创建新的需求文档。
- 如果扫描发现 component 目录下的某个子目录没有主需求文档（与目录同名的 .md 文件），跳过该目录并在摘要中提示。
- 组件代码列如果无法确定，一律填写 `待关联`，不要猜测。
