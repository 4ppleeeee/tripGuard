import AbilityConstant from '@ohos.app.ability.AbilityConstant';
import hilog from '@ohos.hilog';
import UIAbility from '@ohos.app.ability.UIAbility';
import Want from '@ohos.app.ability.Want';
import window from '@ohos.window';
import { Configuration } from '@ohos.app.ability.Configuration';
import { ConfigurationConstant } from '@kit.AbilityKit';
import {
  LogAdapter,
  RouterAdapter,
  loadKnoi,
  LoginSDKManager,
  AppThemeManager,
  getHarmonyStartupProvider,
  KuiklyRenderAdapterManager
} from '@kmm/core';
import BuildProfile from 'BuildProfile';
import bundleManager from '@ohos.bundle.bundleManager';

export default class EntryAbility extends UIAbility {
  onCreate(want: Want, launchParam: AbilityConstant.LaunchParam): void {
    // 从 entry（application 模块）的 BuildProfile 读取 DEBUG / VERSION_NAME 并透传给 shared。
    // 仅 application 模块的 BuildProfile 会由构建系统自动生成并包含 VERSION_NAME，
    // HAR 模块（shared）的 BuildProfile 只有 HAR_VERSION，无法直接取到 App 版本号。
    // 传入 UIAbilityContext：Permission 模块需要用它拉起系统权限弹窗，其他模块可通过
    // uiContext.getApplicationContext() 取到 ApplicationContext。
    loadKnoi(this.context, BuildProfile.DEBUG, BuildProfile.VERSION_NAME)
    hilog.info(0x0000, 'testTag', '%{public}s', 'Ability onCreate');

    // 启动时打印签名信息，便于流水线上核对 bundleId 对应的 appIdentifier
    this.printSignatureInfo();

    // 检查是否为外部 scheme 拉起
    this.handleAppScheme(want);
  }

  onDestroy(): void {
    hilog.info(0x0000, 'testTag', '%{public}s', 'Ability onDestroy');
  }

  onWindowStageCreate(windowStage: window.WindowStage): void {
    // Main window is created, set main page for this ability
    hilog.info(0x0000, 'testTag', '%{public}s', 'Ability onWindowStageCreate');
    windowStage.getMainWindowSync().setWindowLayoutFullScreen(true);

    KuiklyRenderAdapterManager.krLogAdapter = new LogAdapter();
    KuiklyRenderAdapterManager.krRouterAdapter = new RouterAdapter();

    // 设置 AbilityContext 供微信登录使用
    LoginSDKManager.getInstance().setAbilityContext(this.context);

    windowStage.loadContent('pages/Index', (err, data) => {
      // 设置 UIContext 供 QQ 登录使用（需要在 loadContent 之后获取）
      if (!err.code) {
        const mainWindow = windowStage.getMainWindowSync();
        LoginSDKManager.getInstance().setUIContext(mainWindow.getUIContext());
      }
      if (err.code) {
        hilog.error(0x0000, 'testTag', 'Failed to load the content. Cause: %{public}s', JSON.stringify(err) ?? '');
        return;
      }
      hilog.info(0x0000, 'testTag', 'Succeeded in loading the content. Data: %{public}s', JSON.stringify(data) ?? '');
    });
  }

  onWindowStageDestroy(): void {
    // Main window is destroyed, release UI related resources
    hilog.info(0x0000, 'testTag', '%{public}s', 'Ability onWindowStageDestroy');
  }

  onForeground(): void {
    // Ability has brought to foreground
    hilog.info(0x0000, 'testTag', '%{public}s', 'Ability onForeground');
    getHarmonyStartupProvider().onAppForeground();
  }

  onBackground(): void {
    // Ability has back to background
    hilog.info(0x0000, 'testTag', '%{public}s', 'Ability onBackground');
    getHarmonyStartupProvider().onAppBackground();
  }

  onConfigurationUpdate(newConfig: Configuration): void {
    // 系统配置变化（如深色模式切换），通知 AppThemeManager
    const colorMode = newConfig.colorMode;
    if (colorMode !== undefined) {
      AppThemeManager.getInstance().onColorModeChanged(colorMode as ConfigurationConstant.ColorMode);
    }
    // 系统字体缩放变化，通知 AppThemeManager
    const fontSizeScale = newConfig.fontSizeScale;
    if (fontSizeScale !== undefined) {
      AppThemeManager.getInstance().onFontScaleChanged(fontSizeScale);
    }
  }

  onNewWant(want: Want, launchParam: AbilityConstant.LaunchParam): void {
    super.onNewWant(want, launchParam);
    LoginSDKManager.getInstance().onNewWant(want, launchParam);

    // 处理外部 scheme 拉起
    this.handleAppScheme(want);
  }

  /**
   * 处理 demo:// scheme 外部拉起。
   */
  private handleAppScheme(want: Want): void {
    const uri = want.uri;
    if (uri && uri.startsWith('demo://')) {
      hilog.info(0x0000, 'ExternalScheme', '收到外部 scheme: %{public}s', uri);
    }
  }

  /**
   * 打印应用签名信息（appIdentifier / appId / fingerprint）
   * 用于在流水线构建产物启动时核对 bundleId 对应的 appIdentifier
   */
  private async printSignatureInfo(): Promise<void> {
    try {
      const bundleInfo = await bundleManager.getBundleInfoForSelf(
        bundleManager.BundleFlag.GET_BUNDLE_INFO_WITH_SIGNATURE_INFO
      );
      if (bundleInfo) {
        const appIdentifier = bundleInfo.signatureInfo.appIdentifier;
        const appId = bundleInfo.signatureInfo.appId;
        const fingerprint = bundleInfo.signatureInfo.fingerprint.toLowerCase();
        hilog.info(
          0x0000,
          'IDManager',
          'get signature info, appid: %{public}s indentifier: %{public}s fingerprint: %{public}s',
          appId,
          appIdentifier,
          fingerprint
        );
      }
    } catch (err) {
      hilog.error(0x0000, 'IDManager', 'get signature info error: %{public}s', JSON.stringify(err) ?? '');
    }
  }
}
