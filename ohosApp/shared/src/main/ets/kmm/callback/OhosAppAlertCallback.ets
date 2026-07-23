import { promptAction } from '@kit.ArkUI';

/**
 * 鸿蒙端 Alert 回调实现。
 * 对应 KMP 侧 @KNCallback 接口 OhosAppAlert（继承 IAppAlert）。
 * 通过鸿蒙 promptAction API 实现 toast 和 dialog。
 */
export class OhosAppAlertCallback {

  showToast(msg: string, duration?: number, debug?: boolean): void {
    if (!msg) {
      return;
    }
    try {
      // duration 单位为秒，promptAction.showToast 的 duration 单位为毫秒
      const durationMs = duration ? Math.round(duration * 1000) : 1500;
      promptAction.showToast({
        message: msg,
        duration: durationMs,
      });
    } catch (e) {
      console.error(`[OhosAppAlert] showToast error: ${e}`);
    }
  }

  showDialog(title: string, msg: string): void {
    try {
      promptAction.showDialog({
        title: title,
        message: msg,
        buttons: [{ text: '确定', color: '#3377FF' }],
      });
    } catch (e) {
      console.error(`[OhosAppAlert] showDialog error: ${e}`);
    }
  }

  showDecorDebugView(msg: string): void {
    // 鸿蒙端暂不支持 debug 浮层
    console.info(`[OhosAppAlert] showDecorDebugView: ${msg}`);
  }

  hideCurrentToast(): void {
    // 鸿蒙端 promptAction 不支持主动隐藏 toast
  }
}
