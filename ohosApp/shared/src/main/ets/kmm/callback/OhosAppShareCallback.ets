import { hilog } from '@kit.PerformanceAnalysisKit';
import { fileIo, fileUri } from '@kit.CoreFileKit';
import {
  SendMessageToWXReq,
  SendReqResult,
  WXImageObject,
  WXMediaMessage
} from '@tencent/wechat_open_sdk';
import {
  Constants as QQConstants,
  ShareData as QQShareData,
  ShareResult,
  ShareResultType as QQShareResultType
} from '@tencent/qq-open-sdk';
import { OhosAppShare } from '../../kniogen/qnFramework/callback';
import { BuglyManager } from '../../startup/BuglyManager';
import { LoginSDKManager } from '../../startup/LoginSDKManager';

const TAG = 'OhosAppShareCallback';
const WX_SCENE_TIMELINE = 1;
const QQ_SHARE_SIGN_PLACEHOLDER = '业务签名';

/**
 * ohos 侧 `IAppShare` 的分享实现。
 *
 * 说明：
 * - 鸿蒙版 `@tencent/wechat_open_sdk` 目前支持 `WXTextObject` / `WXImageObject`，
 *   评论分享海报走 `WXImageObject` 图片分享。
 * - 该 SDK 尚未提供 `WXFileObject`，因此无法像 Android / iOS 一样把日志 zip 以文件消息分享给微信。
 * - 鸿蒙端也暂未接入企业微信分享 SDK。
 * - 为保证用户"分享日志"请求不会默默丢失，这里统一降级为 `BuglyManager.uploadLogToBugly`，
 *   让日志经 Bugly 链路回传到服务端，行为对齐 Android `SetupAppShare` 的兜底分支。
 * - 待微信鸿蒙 SDK 提供 `WXFileObject` / `WXApi.isWXAppInstalled` 等能力后，
 *   再按 QQ 新闻 `OhosShareService.share2WxFriends` 的方式接入真实的微信文件分享。
 */
export class OhosAppShareCallback implements OhosAppShare {

  /**
   * 判断指定分享渠道是否支持（对应 App 是否已安装）。
   *
   * 逻辑：
   * - 对 SYSTEM / COPY_LINK / SAVE_IMAGE / SAVE_VIDEO 这类无需外部 App 的渠道始终返回 true；
   * - 微信 / 朋友圈走 `WXImageObject` 图片分享；
   * - QQ / QQ 空间走 OpenSDK 1.0.4 图片分享；
   * - 微博 / 企业微信当前能力未接入，先保持不展示。
   */
  isShareChannelSupported(channelId: string): boolean {
    const normalizedChannelId = channelId.toUpperCase();

    switch (normalizedChannelId) {
      case 'SYSTEM':
      case 'COPY_LINK':
      case 'SAVE_IMAGE':
      case 'SAVE_VIDEO':
      case 'WEIXIN':
      case 'WEIXIN_MOMENTS':
      case 'QQ':
      case 'QZONE':
        return true;
      case 'WEIBO':
        // 鸿蒙端微博分享暂未接入独立 SDK，先不展示。
        return false;
      case 'WORK_WEIXIN':
        // 鸿蒙端暂未接入企业微信 SDK，保持不展示。
        return false;
      default:
        hilog.debug(0x0000, TAG, `isShareChannelSupported: unknown channelId=${channelId}`);
        return false;
    }
  }

  shareLogToWeChat(): void {
    hilog.warn(
      0x0000,
      TAG,
      'shareLogToWeChat: wechat_open_sdk on ohos has no file-sharing API, fallback to Bugly'
    );
    this.uploadLogToBuglyAsFallback('shareLogToWeChat');
  }

  shareLogToWeCom(): void {
    hilog.warn(
      0x0000,
      TAG,
      'shareLogToWeCom: WeCom SDK not available on ohos, fallback to Bugly'
    );
    this.uploadLogToBuglyAsFallback('shareLogToWeCom');
  }

  shareToChannel(channelId: string, title: string, desc: string, url: string, imagePath: string): void {
    const normalizedChannelId = channelId.toUpperCase();
    hilog.info(
      0x0000,
      TAG,
      `shareToChannel: channel=${normalizedChannelId}, title=${title}, ` +
        `descLength=${desc.length}, hasUrl=${url.length > 0}, imagePath=${imagePath}`
    );
    switch (normalizedChannelId) {
      case 'WEIXIN':
      case 'WEIXIN_MOMENTS':
        this.shareImageToWeChat(normalizedChannelId, title, imagePath);
        break;
      case 'QQ':
      case 'QZONE':
        this.shareImageToQQ(imagePath);
        break;
      default:
        hilog.warn(0x0000, TAG, `shareToChannel: unsupported channel=${channelId}`);
        break;
    }
  }

  private shareImageToWeChat(channelId: string, title: string, imagePath: string): void {
    const localPath = this.normalizeLocalImagePath(imagePath);
    if (localPath.length === 0 || !this.isLocalFileReadable(localPath)) {
      hilog.error(0x0000, TAG, `shareImageToWeChat: image file not found, path=${imagePath}`);
      return;
    }

    const wxApi = LoginSDKManager.getInstance().getWXApi();
    if (!wxApi) {
      hilog.error(0x0000, TAG, 'shareImageToWeChat: WX SDK not initialized');
      return;
    }

    const abilityContext = LoginSDKManager.getInstance().getAbilityContext();
    if (!abilityContext) {
      hilog.error(0x0000, TAG, 'shareImageToWeChat: AbilityContext not available');
      return;
    }

    const imageObject = new WXImageObject();
    imageObject.uri = imagePath.startsWith('file://') ? imagePath : fileUri.getUriFromPath(localPath);

    const message = new WXMediaMessage();
    message.title = title.length > 0 ? title : '评论分享';
    message.mediaObject = imageObject;

    const req = new SendMessageToWXReq();
    req.transaction = `comment_share_${Date.now()}`;
    req.message = message;
    req.scene = channelId === 'WEIXIN_MOMENTS' ? WX_SCENE_TIMELINE : SendMessageToWXReq.WXSceneSession;

    void Promise.resolve(wxApi.sendReq(abilityContext, req) as SendReqResult | Promise<SendReqResult>)
      .then((result: SendReqResult) => {
        if (result === false) {
          hilog.error(0x0000, TAG, `shareImageToWeChat: sendReq returned false, channel=${channelId}`);
        }
      })
      .catch((error: Error) => {
        hilog.error(0x0000, TAG, `shareImageToWeChat: sendReq failed: ${JSON.stringify(error)}`);
      });
  }

  private shareImageToQQ(imagePath: string): void {
    const localPath = this.normalizeLocalImagePath(imagePath);
    if (localPath.length === 0 || !this.isLocalFileReadable(localPath)) {
      hilog.error(0x0000, TAG, `shareImageToQQ: image file not found, path=${imagePath}`);
      return;
    }

    const qqApi = LoginSDKManager.getInstance().getQQApi();
    if (!qqApi) {
      hilog.error(0x0000, TAG, 'shareImageToQQ: QQ SDK not initialized');
      return;
    }

    const imageUri = imagePath.startsWith('file://') ? imagePath : fileUri.getUriFromPath(localPath);
    const shareJson = JSON.stringify({
      msg_style: QQConstants.ShareMsgStyle.SHARE_MSG_STYLE_PICTURE,
      share_uris: [imageUri]
    });

    const shareData = new QQShareData();
    shareData.timestamp = Math.floor(Date.now() / 1000);
    shareData.nonce = Math.floor(Math.random() * 100000000 + 100);
    shareData.shareJson = shareJson;
    // QQ OpenSDK 1.0.4 Demo 使用业务后台签名；当前仓库暂无对应接口，先填占位值并保留详细结果日志。
    shareData.shareJsonSign = QQ_SHARE_SIGN_PLACEHOLDER;

    void qqApi.share(QQConstants.ShareType.SHARE_IMAGE, shareData)
      .then((result: ShareResult) => {
        if (result.resultType === QQShareResultType.Success) {
          hilog.info(0x0000, TAG, 'shareImageToQQ: success');
        } else {
          hilog.error(
            0x0000,
            TAG,
            `shareImageToQQ: failed resultType=${result.resultType}, code=${result.code}, ` +
              `message=${result.message}, detail=${result.detail}`
          );
        }
      })
      .catch((error: Error) => {
        hilog.error(0x0000, TAG, `shareImageToQQ: share failed: ${JSON.stringify(error)}`);
      });
  }

  private normalizeLocalImagePath(imagePath: string): string {
    if (imagePath.length === 0) {
      return '';
    }
    if (!imagePath.startsWith('file://')) {
      return imagePath;
    }
    try {
      return new fileUri.FileUri(imagePath).path;
    } catch (error) {
      hilog.error(0x0000, TAG, `normalizeLocalImagePath failed: ${JSON.stringify(error)}`);
      return '';
    }
  }

  private isLocalFileReadable(localPath: string): boolean {
    try {
      return fileIo.accessSync(localPath);
    } catch (error) {
      hilog.error(0x0000, TAG, `isLocalFileReadable failed: ${JSON.stringify(error)}`);
      return false;
    }
  }

  private uploadLogToBuglyAsFallback(from: string): void {
    try {
      BuglyManager.getInstance().uploadLogToBugly();
    } catch (error) {
      hilog.error(0x0000, TAG, `${from} fallback failed: ${JSON.stringify(error)}`);
    }
  }
}
