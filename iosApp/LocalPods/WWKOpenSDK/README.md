# WWKOpenSDK (企业微信 iOS SDK)

企业微信开放平台 iOS SDK，用于分享、授权等功能。

## SDK 来源

SDK 文件（`libWXWorkApi.a` + 头文件）来自 microvision 项目中的 `DCLSecuritySDK` Pod。
原始下载地址：https://developer.work.weixin.qq.com/document/path/91196

## 目录结构

```
LocalPods/WWKOpenSDK/
├── WWKOpenSDK.podspec
├── README.md
└── Frameworks/
    ├── WWKApi.h
    ├── WWKApiObject.h
    └── libWXWorkApi.a
```

## 使用方式

在 Podfile 中引用：
```ruby
pod 'WWKOpenSDK', :path => 'LocalPods/WWKOpenSDK'
```

在 Bridging Header 中导入：
```objc
#import "WWKApi.h"
#import "WWKApiObject.h"
```

## 版本信息

- 当前配置版本：2.0.5
- 对应 Android 端：`com.tencent.weishi.thirdparty:wwapi:3.0.0.7`
- 企微 Schema：`wwauthc8d2d7a989d28694000026`（正式环境）/ `wwauth40f89068e85f7d48000004`（调试环境）

## 注意事项

- 如果 SDK 未放置，`pod install` 会报错，此时 `ThirdPartyLoginBridge.m` 会自动 fallback 到动态加载模式
- `IOSAppShare.swift` 中的企微分享也会 fallback 到 URL Scheme 方式
