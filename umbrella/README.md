# umbrella 模块说明

## 模块定位

`umbrella` 是当前对外暴露的主模块，用于聚合并导出所有业务能力模块，统一提供给 Android、iOS、OHOS 宿主侧接入。

## 依赖关系

`umbrella` 通过 `modules.properties` 聚合以下模块：

- `startup`
- `shared`
- `wsCore`
- `wsView`
- `wsCompose`

## 三端接入约定

- Android：宿主依赖 `project(":umbrella")`
- iOS：Pod 使用 `pod 'umbrella', :path => '../umbrella'`
- OHOS：Native 侧链接 `libumbrella.so`，并通过 `libumbrella_api.h` 初始化

## 构建说明

- 构建脚本：`umbrella/build.gradle.kts`
- PodSpec：`umbrella/umbrella.podspec`
- 该模块使用 `qqnews.kmm.main`，并承担主模块导出职责
