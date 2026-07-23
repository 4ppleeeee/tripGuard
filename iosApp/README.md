# iosApp — iOS 端构建与运行说明

## 环境要求

| 项 | 版本 | 说明 |
|---|---|---|
| macOS + Xcode | Xcode 15+（已在 26.x 上验证） | 常规 iOS 构建链 |
| Ruby | **3.2.2**（由 `.ruby-version` 指定） | 低于 3.x 在 1.16 CocoaPods 下会撞 activesupport/Logger 加载问题 |
| CocoaPods | **1.16.2（精确匹配）** | `Gemfile.lock` 锁死；Podfile 顶部有版本守卫，其他版本会直接 raise |
| bundler | 任意较新版本即可 | 负责按 `Gemfile.lock` 解析 cocoapods |

这三处锁定的源文件（均已随仓库提交，不要改版本号而不同步改其它两个）：
- `iosApp/.ruby-version`
- `iosApp/Gemfile` + `iosApp/Gemfile.lock`
- `iosApp/Podfile` 顶部的 `REQUIRED_COCOAPODS` 守卫

## 首次准备


```bash
# 1. 装 Ruby 3.2.2（推荐 rbenv；asdf / chruby 也可）
brew install rbenv ruby-build
echo 'eval "$(rbenv init -)"' >> ~/.zshrc   # 按你的 shell 调整
exec $SHELL -l
rbenv install 3.2.2

# 2. 装 bundler（每个 Ruby 版本下装一次）
gem install bundler

# 3. 拉项目依赖
cd iosApp
bundle install
```

完成后，在 `iosApp` 目录敲 `pod --version`，rbenv 会自动按 `.ruby-version` 切到 3.2.2；`bundle exec pod --version` 应返回 `1.16.2`。

## 日常使用

原先执行 `pod install` / `pod update` 的地方**全部**换成：

```bash
bundle exec pod install
bundle exec pod update
```

原因：`pod install` 走的是系统或全局 gem 下的 CocoaPods，版本不可控；`bundle exec pod install` 强制走 `Gemfile.lock` 里的 1.16.2，和团队其他人完全一致。

如果忘了敲 `bundle exec`，Podfile 顶部的守卫会立即报错，提醒改正。

## 升级 CocoaPods 版本（维护者）

同步改三处，并在同一个 commit 提交：

1. `iosApp/Podfile` 的 `REQUIRED_COCOAPODS = '= X.Y.Z'`
2. `iosApp/Gemfile` 的 `gem 'cocoapods', 'X.Y.Z'`
3. `cd iosApp && bundle update cocoapods` 刷新 `Gemfile.lock`
4. `bundle exec pod install` 刷新 `Podfile.lock`

## 常见问题

**报错 `CocoaPods 版本不符合要求：当前 X.Y.Z，需要 = 1.16.2`**
你直接敲了 `pod install`，走到了本地其它版本的 CocoaPods。改用 `bundle exec pod install`。

**报错 `Bundler: command not found`**
当前 Ruby 下没装 bundler。`gem install bundler` 即可。

**报错 `Ruby version xxx doesn't match ... 3.2.2`（bundler 给出）**
你的 shell 没走 rbenv 的自动切换。确认 `eval "$(rbenv init -)"` 已写入 shell rc 并重新开 terminal；或临时 `rbenv shell 3.2.2` 再试。

**我不用 rbenv，用 asdf / chruby / mise**
都能读 `.ruby-version`，效果一致。
