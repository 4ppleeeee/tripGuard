# SmartRun 应用名称映射表

> 本映射表覆盖 Android、iOS、鸿蒙三大平台。
>
> **使用场景**：调用 `launch_app(package_name=...)` 时需要提供正确的包名/Bundle ID。
> 不确定包名时，可先调用 `get_installed_apps` 查询设备上已安装的应用。

---

## 常用应用速查

| 应用 | Android 包名 | iOS Bundle ID | 鸿蒙 Bundle Name |
|------|-------------|---------------|-----------------|
| 微信 | `com.tencent.mm` | `com.tencent.xin` | `com.tencent.wechat` |
| QQ | `com.tencent.mobileqq` | `com.tencent.mqq` | `com.tencent.mqq` |
| 微博 | `com.sina.weibo` | `com.sina.weibo` | `com.sina.weibo.stage` |
| 抖音 | `com.ss.android.ugc.aweme` | `com.ss.iphone.ugc.Aweme` | `com.ss.hm.ugc.aweme` |
| 淘宝 | `com.taobao.taobao` | `com.taobao.taobao4iphone` | `com.taobao.taobao4hmos` |
| 京东 | `com.jingdong.app.mall` | `com.360buy.jdmobile` | `com.jd.hm.mall` |
| 支付宝 | `com.eg.android.AlipayGphone` | `com.alipay.iphoneclient` | `com.alipay.mobile.client` |
| 美团 | `com.sankuai.meituan` | `com.meituan.imeituan` | `com.sankuai.hmeituan` |
| 高德地图 | `com.autonavi.minimap` | `com.autonavi.amap` | `com.amap.hmapp` |
| 百度地图 | `com.baidu.BaiduMap` | `com.baidu.map` | `com.baidu.hmmap` |
| 拼多多 | `com.xunmeng.pinduoduo` | `com.xunmeng.pinduoduo` | `com.xunmeng.pinduoduo.hos` |
| 应用宝 | `com.tencent.android.qqdownloader` | - | - |
| 小红书 | `com.xingin.xhs` | `com.xingin.discover` | `com.xingin.xhs_hos` |

---

## 社交通讯

### Android

| 应用名 | 包名 |
|--------|------|
| 微信 | `com.tencent.mm` |
| QQ | `com.tencent.mobileqq` |
| 微博 | `com.sina.weibo` |
| Telegram | `org.telegram.messenger` |
| WhatsApp | `com.whatsapp` |
| Twitter / X | `com.twitter.android` |

### iOS

| 应用名 | Bundle ID |
|--------|-----------|
| 微信 | `com.tencent.xin` |
| QQ | `com.tencent.mqq` |
| 企业微信 | `com.tencent.ww` |
| 微博 | `com.sina.weibo` |
| TIM | `com.tencent.tim` |
| WhatsApp | `net.whatsapp.WhatsApp` |
| Facebook | `com.facebook.Facebook` |
| Messenger | `com.facebook.Messenger` |
| Instagram | `com.burbn.instagram` |
| Line | `jp.naver.line` |
| LinkedIn | `com.linkedin.LinkedIn` |

### 鸿蒙

| 应用名 | Bundle Name |
|--------|-------------|
| 微信 | `com.tencent.wechat` |
| QQ | `com.tencent.mqq` |
| 微博 | `com.sina.weibo.stage` |
| 企业微信 | `com.tencent.wework.hmos` |
| 飞书 | `com.ss.feishu` |

---

## 电商购物

### Android

| 应用名 | 包名 |
|--------|------|
| 淘宝 | `com.taobao.taobao` |
| 京东 | `com.jingdong.app.mall` |
| 拼多多 | `com.xunmeng.pinduoduo` |
| Temu | `com.einnovation.temu` |

### iOS

| 应用名 | Bundle ID |
|--------|-----------|
| 淘宝 | `com.taobao.taobao4iphone` |
| 京东 | `com.360buy.jdmobile` |
| 天猫 | `com.taobao.tmall` |
| 拼多多 | `com.xunmeng.pinduoduo` |
| 闲鱼 | `com.taobao.fleamarket` |
| 唯品会 | `com.vipshop.iphone` |
| 苏宁易购 | `SuningEMall` |
| 得物 | `com.siwuai.duapp` |

### 鸿蒙

| 应用名 | Bundle Name |
|--------|-------------|
| 淘宝 | `com.taobao.taobao4hmos` |
| 京东 | `com.jd.hm.mall` |
| 拼多多 | `com.xunmeng.pinduoduo.hos` |
| 唯品会 | `com.vip.hosapp` |
| 闲鱼 | `com.taobao.idlefish4ohos` |
| 得物 | `com.dewu.hos` |

---

## 生活社交

### Android

| 应用名 | 包名 |
|--------|------|
| 小红书 | `com.xingin.xhs` |
| 豆瓣 | `com.douban.frodo` |
| 知乎 | `com.zhihu.android` |
| Reddit | `com.reddit.frontpage` |

### iOS

| 应用名 | Bundle ID |
|--------|-----------|
| 小红书 | `com.xingin.discover` |
| 豆瓣 | `com.douban.frodo` |
| 知乎 | `com.zhihu.ios` |

### 鸿蒙

| 应用名 | Bundle Name |
|--------|-------------|
| 小红书 | `com.xingin.xhs_hos` |
| 知乎 | `com.zhihu.hmos` |

---

## 地图导航

### Android

| 应用名 | 包名 |
|--------|------|
| 高德地图 | `com.autonavi.minimap` |
| 百度地图 | `com.baidu.BaiduMap` |
| Google Maps | `com.google.android.apps.maps` |

### iOS

| 应用名 | Bundle ID |
|--------|-----------|
| 高德地图 | `com.autonavi.amap` |
| 百度地图 | `com.baidu.map` |
| 腾讯地图 | `com.tencent.sosomap` |

### 鸿蒙

| 应用名 | Bundle Name |
|--------|-------------|
| 高德地图 | `com.amap.hmapp` |
| 百度地图 | `com.baidu.hmmap` |

---

## 餐饮外卖

### Android

| 应用名 | 包名 |
|--------|------|
| 美团 | `com.sankuai.meituan` |
| 大众点评 | `com.dianping.v1` |
| 饿了么 | `me.ele` |
| 肯德基 | `com.yek.android.kfc.activitys` |

### iOS

| 应用名 | Bundle ID |
|--------|-----------|
| 美团 | `com.meituan.imeituan` |
| 美团外卖 | `com.meituan.itakeaway` |
| 大众点评 | `com.dianping.dpscope` |
| 饿了么 | `me.ele.ios.eleme` |

### 鸿蒙

| 应用名 | Bundle Name |
|--------|-------------|
| 美团 | `com.sankuai.hmeituan` |
| 美团外卖 | `com.meituan.takeaway` |
| 大众点评 | `com.sankuai.dianping` |
| 海底捞 | `com.haidilao.haros` |

---

## 出行旅游

### Android

| 应用名 | 包名 |
|--------|------|
| 携程 | `ctrip.android.view` |
| 铁路12306 | `com.MobileTicket` |
| 去哪儿旅行 | `com.Qunar` |
| 滴滴出行 | `com.sdu.didi.psnger` |

### iOS

| 应用名 | Bundle ID |
|--------|-----------|
| 携程 | `ctrip.com` |
| 去哪儿旅行 | `com.qunar.iphoneclient8` |
| 飞猪 | `com.taobao.travel` |

### 鸿蒙

| 应用名 | Bundle Name |
|--------|-------------|
| 铁路12306 | `com.chinarailway.ticketingHM` |
| 滴滴出行 | `com.sdu.didi.hmos.psnger` |
| 同程旅行 | `com.tongcheng.hmos` |

---

## 视频娱乐

### Android

| 应用名 | 包名 |
|--------|------|
| bilibili | `tv.danmaku.bili` |
| 抖音 | `com.ss.android.ugc.aweme` |
| 快手 | `com.smile.gifmaker` |
| 腾讯视频 | `com.tencent.qqlive` |
| 爱奇艺 | `com.qiyi.video` |
| 优酷视频 | `com.youku.phone` |
| 芒果TV | `com.hunantv.imgo.activity` |
| TikTok | `com.zhiliaoapp.musically` |

### iOS

| 应用名 | Bundle ID |
|--------|-----------|
| 哔哩哔哩 | `tv.danmaku.bilianime` |
| 抖音 | `com.ss.iphone.ugc.Aweme` |
| 快手 | `com.jiangjia.gif` |
| 腾讯视频 | `com.tencent.live4iphone` |
| 爱奇艺 | `com.qiyi.iphone` |
| 优酷 | `com.youku.YouKu` |
| 芒果TV | `com.hunantv.imgotv` |
| TikTok | `com.zhiliaoapp.musically` |
| YouTube | `com.google.ios.youtube` |
| Netflix | `com.netflix.Netflix` |

### 鸿蒙

| 应用名 | Bundle Name |
|--------|-------------|
| bilibili | `yylx.danmaku.bili` |
| 抖音 | `com.ss.hm.ugc.aweme` |
| 快手 | `com.kuaishou.hmapp` |
| 腾讯视频 | `com.tencent.videohm` |
| 爱奇艺 | `com.qiyi.video.hmy` |
| 芒果TV | `com.mgtv.phone` |

---

## 音乐音频

### Android

| 应用名 | 包名 |
|--------|------|
| 网易云音乐 | `com.netease.cloudmusic` |
| QQ音乐 | `com.tencent.qqmusic` |
| 汽水音乐 | `com.luna.music` |
| 喜马拉雅 | `com.ximalaya.ting.android` |

### iOS

| 应用名 | Bundle ID |
|--------|-----------|
| 网易云音乐 | `com.netease.cloudmusic` |
| QQ音乐 | `com.tencent.QQMusic` |
| 喜马拉雅 | `com.gemd.iting` |
| 酷狗音乐 | `com.kugou.kugou1002` |
| Spotify | `com.spotify.client` |

### 鸿蒙

| 应用名 | Bundle Name |
|--------|-------------|
| QQ音乐 | `com.tencent.hm.qqmusic` |
| 汽水音乐 | `com.luna.hm.music` |
| 喜马拉雅 | `com.ximalaya.ting.xmharmony` |

---

## 办公效率

### Android

| 应用名 | 包名 |
|--------|------|
| 飞书 | `com.ss.android.lark` |
| 钉钉 | `com.alibaba.android.rimet` |
| QQ邮箱 | `com.tencent.androidqqmail` |
| Gmail | `com.google.android.gm` |

### iOS

| 应用名 | Bundle ID |
|--------|-----------|
| 飞书 | `com.bytedance.ee.lark` |
| 钉钉 | `com.laiwang.DingTalk` |
| QQ邮箱 | `com.tencent.qqmail` |
| Gmail | `com.google.Gmail` |
| 腾讯文档 | `com.tencent.txdocs` |

### 鸿蒙

| 应用名 | Bundle Name |
|--------|-------------|
| 飞书 | `com.ss.feishu` |
| WPS | `cn.wps.mobileoffice.hap` |

---

## 新闻阅读

### Android

| 应用名 | 包名 |
|--------|------|
| 今日头条 | `com.ss.android.article.news` |
| 腾讯新闻 | `com.tencent.news` |
| 番茄小说 | `com.dragon.read` |

### iOS

| 应用名 | Bundle ID |
|--------|-----------|
| 今日头条 | `com.ss.iphone.article.News` |
| 腾讯新闻 | `com.tencent.info` |
| 番茄小说 | `com.dragon.read` |

### 鸿蒙

| 应用名 | Bundle Name |
|--------|-------------|
| 今日头条 | `com.ss.hm.article.news` |

---

## 浏览器

### Android

| 应用名 | 包名 |
|--------|------|
| Chrome | `com.android.chrome` |

### iOS

| 应用名 | Bundle ID |
|--------|-----------|
| Safari | `com.apple.mobilesafari` |
| Chrome | `com.google.chrome.ios` |
| Firefox | `org.mozilla.ios.Firefox` |
| QQ浏览器 | `com.tencent.mttlite` |

### 鸿蒙

| 应用名 | Bundle Name |
|--------|-------------|
| 浏览器 | `com.huawei.hmos.browser` |
| UC浏览器 | `com.uc.mobile` |

---

## 系统应用

### Android

| 应用名 | 包名 |
|--------|------|
| 设置 | `com.android.settings` |
| 时钟 | `com.android.deskclock` |
| 联系人 | `com.android.contacts` |
| 文件管理 | `com.android.fileexplorer` |

### iOS (Apple 原生)

| 应用名 | Bundle ID |
|--------|-----------|
| 设置 | `com.apple.Preferences` |
| Safari | `com.apple.mobilesafari` |
| App Store | `com.apple.AppStore` |
| 相机 | `com.apple.camera` |
| 照片 | `com.apple.mobileslideshow` |
| 时钟 / 闹钟 | `com.apple.mobiletimer` |
| 备忘录 | `com.apple.mobilenotes` |
| 提醒事项 | `com.apple.reminders` |
| 快捷指令 | `com.apple.shortcuts` |
| 天气 | `com.apple.weather` |
| 日历 | `com.apple.mobilecal` |
| 地图 | `com.apple.Maps` |
| 电话 | `com.apple.mobilephone` |
| 通讯录 | `com.apple.MobileAddressBook` |
| 信息 | `com.apple.MobileSMS` |
| FaceTime | `com.apple.facetime` |
| 计算器 | `com.apple.calculator` |
| 健康 | `com.apple.Health` |
| 钱包 | `com.apple.Passbook` |
| 文件 | `com.apple.DocumentsApp` |
| 邮件 | `com.apple.mobilemail` |
| 查找 | `com.apple.findmy` |
| 翻译 | `com.apple.Translate` |
| 音乐 | `com.apple.Music` |
| 播客 | `com.apple.podcasts` |
| 语音备忘录 | `com.apple.VoiceMemos` |

### 鸿蒙 (华为系统应用)

| 应用名 | Bundle Name |
|--------|-------------|
| 设置 | `com.huawei.hmos.settings` |
| 浏览器 | `com.huawei.hmos.browser` |
| 计算器 | `com.huawei.hmos.calculator` |
| 日历 | `com.huawei.hmos.calendar` |
| 相机 | `com.huawei.hmos.camera` |
| 时钟 | `com.huawei.hmos.clock` |
| 云空间 | `com.huawei.hmos.clouddrive` |
| 邮件 | `com.huawei.hmos.email` |
| 文件管理器 | `com.huawei.hmos.filemanager` |
| 相册 | `com.huawei.hmos.photos` |
| 笔记 / 备忘录 | `com.huawei.hmos.notepad` |
| 录音机 | `com.huawei.hmos.soundrecorder` |
| 联系人 | `com.ohos.contacts` |
| 短信 | `com.ohos.mms` |
| 电话 | `com.ohos.callui` |
| 应用市场 | `com.huawei.hmsapp.appgallery` |
| 天气 | `com.huawei.hmsapp.totemweather` |
| 华为音乐 | `com.huawei.hmsapp.music` |
| 华为视频 | `com.huawei.hmsapp.himovie` |
| 智能助手 / 小艺 | `com.huawei.hmos.vassistant` |

---

## AI 工具

### Android

| 应用名 | 包名 |
|--------|------|
| 豆包 | `com.larus.nova` |

### 鸿蒙

| 应用名 | Bundle Name |
|--------|-------------|
| 豆包 | `com.larus.nova.hm` |

---

## 金融服务

### iOS

| 应用名 | Bundle ID |
|--------|-----------|
| 支付宝 | `com.alipay.iphoneclient` |
| 云闪付 | `com.unionpay.chsp` |
| 中国银行 | `com.boc.BOCMBCI` |

### 鸿蒙

| 应用名 | Bundle Name |
|--------|-------------|
| 支付宝 | `com.alipay.mobile.client` |
| 建设银行 | `com.ccb.mobilebank.hm` |

---

## 健康运动

### Android

| 应用名 | 包名 |
|--------|------|
| Keep | `com.gotokeep.keep` |

### 鸿蒙

| 应用名 | Bundle Name |
|--------|-------------|
| 运动健康 | `com.huawei.hmos.health` |
