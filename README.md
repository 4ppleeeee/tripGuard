# Travel MVP Demo

基于 `/Users/aatroxli/coding/tencent/kmm-core` 复制出来的轻量 Android 原型，用来验证：

```text
小红书分享文本 -> Android 分享入口 -> 提取 URL -> 本地入库 -> 异步解析网页 meta -> 卡片展示
```

## 已实现

- Android `ACTION_SEND text/plain` 分享入口。
- 手动粘贴分享文案或 URL。
- 从文本中提取第一条 `http/https` 链接。
- 使用 `SharedPreferences` 保存本地资料库。
- 异步抓取网页 `title`、`description`、`og:image`。
- 卡片状态：等待解析、解析中、已解析、解析失败。
- 支持打开原文和重新解析。

## 运行

```bash
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ./gradlew :androidApp:assembleDebug --no-daemon --console=plain
```

APK 输出：

```text
androidApp/build/outputs/apk/debug/androidApp-arm64-v8a-debug.apk
androidApp/build/outputs/apk/debug/androidApp-armeabi-v7a-debug.apk
```

## 当前取舍

- 没有接服务端数据库，先用本地存储验证入口闭环。
- 没有做小红书正文抓取、登录态、反爬处理。
- 没有接 AI 摘要，卡片先保留可扩展的数据结构。
