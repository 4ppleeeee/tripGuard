/*
 * Copyright shim, do not modify package or class name.
 *
 * 该 shim 类是为了兼容 QQ Open SDK（com.tencent.tauth.Tencent / com.tencent.connect.share.QQShare）。
 * 经 logcat 验证：QQ SDK 内部使用反射查找 android.support.v4.content.FileProvider 完成
 *   image-mode 分享（SHARE_TO_QQ_TYPE_IMAGE + SHARE_TO_QQ_IMAGE_LOCAL_URL）时的
 *   File -> content:// Uri 转换。
 *
 * 本项目已迁移到 AndroidX，未启用 Jetifier（android.enableJetifier 未开启，避免编译耗时膨胀），
 * 因此运行时会抛 NoClassDefFoundError: Landroid/support/v4/content/FileProvider; 导致
 * 评论分享到 QQ 时 SDK 内部 crash 后被 try/catch 吞掉，表现为"点 QQ 后什么都没发生"。
 *
 * 此 shim 通过保留 android.support.v4.content.FileProvider 类名 + 直接继承 AndroidX 实现，
 * 让 QQ SDK 的反射成功，行为完全等同 AndroidX 版 FileProvider，无功能差异。
 *
 * - 不要重命名 / 不要改包名
 * - 不要在 AndroidManifest 注册（已注册的是 androidx.core.content.FileProvider）
 * - 当 QQ SDK 升级到使用 AndroidX 时可移除此 shim
 */
package android.support.v4.content;

public class FileProvider extends androidx.core.content.FileProvider {
}
