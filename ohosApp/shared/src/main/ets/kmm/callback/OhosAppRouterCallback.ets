import router from '@ohos.router';
import { common } from '@kit.AbilityKit';
import { KRRecord } from '@kuikly/render';
import {
  COMPOSE_PAGE_LAUNCH_TYPE_SINGLE_INSTANCE,
  OhosComposePageRegistry,
} from '../../kuikly/OhosComposePageRegistry';

/**
 * 鸿蒙端路由回调实现。
 * 对应 KMP 侧 @KNCallback 接口 OhosAppRouter。
 * 通过鸿蒙 router API 实现页面跳转。
 */
export class OhosAppRouterCallback {
  private static readonly SINGLE_INSTANCE_RETRY_COUNT = 3;
  private static readonly SINGLE_INSTANCE_RETRY_DELAY_MS = 50;
  private readonly context: common.UIAbilityContext;

  constructor(context: common.UIAbilityContext) {
    this.context = context;
  }

  /**
   * 通过 scheme 跳转（暂不支持，预留接口）
   */
  toScheme(scheme: string): void {
    console.info(`[OhosAppRouter] toScheme: ${scheme}`);
    // 鸿蒙端暂不支持 scheme 跳转，后续按需对接
  }

  /**
   * 跳转到 Compose 页面
   * 通过 router.pushUrl 跳转到 pages/Index，传递 pageName 和 pageData
   */
  toComposePage(pageName: string, pageData: KRRecord, launchType: string = 'STANDARD'): void {
    console.info(`[OhosAppRouter] toComposePage: ${pageName}, launchType: ${launchType}`);
    if (launchType === COMPOSE_PAGE_LAUNCH_TYPE_SINGLE_INSTANCE) {
      this.openSingleInstancePage(pageName, pageData, OhosAppRouterCallback.SINGLE_INSTANCE_RETRY_COUNT);
      return;
    }

    this.pushComposePage(pageName, pageData);
  }

  private openSingleInstancePage(pageName: string, pageData: KRRecord, retryCount: number): void {
    if (OhosComposePageRegistry.dispatchToVisible(pageName, pageData)) {
      return;
    }
    if (retryCount > 0) {
      setTimeout(() => {
        this.openSingleInstancePage(pageName, pageData, retryCount - 1);
      }, OhosAppRouterCallback.SINGLE_INSTANCE_RETRY_DELAY_MS);
      return;
    }

    const backSteps = OhosComposePageRegistry.backStepsToLatest(pageName);
    if (backSteps !== undefined) {
      this.backToExistingPage(backSteps, () => {
        this.dispatchAfterBack(pageName, pageData, OhosAppRouterCallback.SINGLE_INSTANCE_RETRY_COUNT);
      });
      return;
    }

    this.pushComposePage(pageName, pageData);
  }

  private pushComposePage(pageName: string, pageData: KRRecord): void {
    router.pushUrl({
      url: 'pages/Index',
      params: {
        pageName,
        pageData
      }
    });
  }

  private backToExistingPage(backSteps: number, onComplete: () => void): void {
    if (backSteps <= 0) {
      onComplete();
      return;
    }
    router.back();
    setTimeout(() => {
      this.backToExistingPage(backSteps - 1, onComplete);
    }, OhosAppRouterCallback.SINGLE_INSTANCE_RETRY_DELAY_MS);
  }

  private dispatchAfterBack(pageName: string, pageData: KRRecord, retryCount: number): void {
    if (OhosComposePageRegistry.dispatchToVisible(pageName, pageData)) {
      return;
    }
    if (retryCount > 0) {
      setTimeout(() => {
        this.dispatchAfterBack(pageName, pageData, retryCount - 1);
      }, OhosAppRouterCallback.SINGLE_INSTANCE_RETRY_DELAY_MS);
      return;
    }
    if (!OhosComposePageRegistry.dispatchToLatest(pageName, pageData)) {
      this.pushComposePage(pageName, pageData);
    }
  }

  /**
   * 返回上一页
   */
  goBack(): void {
    router.back();
  }

  /**
   * 将当前 Ability 退到后台，不销毁页面栈。
   */
  moveTaskToBack(): void {
    this.context.moveAbilityToBackground()
      .catch((err: Error) => {
        console.error(`[OhosAppRouter] moveTaskToBack failed: ${JSON.stringify(err)}`);
      });
  }
}
