import 'libkmm_shared.so'
import { init, setup } from 'knoi';
import { registerAppStartupService } from './service/HarmonyStartupService';
import { registerOhosResService } from './service/OhosResServiceImpl';
import { registerOhosNetworkService } from './service/OhosNetworkServiceImpl';
import { registerOhosFileManagerService } from './service/OhosFileManagerServiceImpl';
import { registerOhosFileCacheService } from './service/OhosFileCacheServiceImpl';
import { registerOhosCookieService } from './service/OhosCookieServiceImpl';
import { common, Context } from '@kit.AbilityKit';
import bundleManager from '@ohos.bundle.bundleManager';
import { getHarmonyStartupProvider } from '../kniogen/kmmStartupStd/provider';
import { OhosAppReportCallback } from './callback/OhosAppReportCallback';
import { OhosAppRouterCallback } from './callback/OhosAppRouterCallback';
import { OhosAppAlertCallback } from './callback/OhosAppAlertCallback';
import { OhosAppResManagerCallback } from './callback/OhosAppResManagerCallback';
import { OhosAppShareCallback } from './callback/OhosAppShareCallback';
import { OhosAppStatusCallback } from './callback/OhosAppStatusCallback';
import { OhosAppGyroscopeCallback } from './callback/OhosAppGyroscopeCallback';
import { OhosAppStatusBarCallback } from './callback/OhosAppStatusBarCallback';
import { OhosAppWindowBridgeCallback } from './callback/OhosAppWindowBridgeCallback';
import { OhosSystemVolumeControllerCallback } from './callback/OhosSystemVolumeControllerCallback';
import { OhosAppPermissionCallback } from './callback/OhosAppPermissionCallback';
import { OhosAppLocationCallback } from './callback/OhosAppLocationCallback';
import { MmkvManager } from '../startup/MmkvManager';
import { WsAppRuntimeInfo } from './WsAppRuntimeInfo';

export class Kommon {
  private static instance: Kommon;
  private hasInit: boolean = false;

  private constructor() {
  }

  public static getInstance() {
    if (!Kommon.instance) {
      Kommon.instance = new Kommon()
    }
    return Kommon.instance
  }

  lazySetup(uiContext: common.UIAbilityContext, isDebug: boolean, appVersion: string) {
    if (this.hasInit) {
      return
    }
    this.hasInit = true
    this.setup(uiContext, isDebug, appVersion)
  }

  /**
   * 需要注入的模块，给路由使用，注入过晚会导致路由解析失败
   */
  private setup(uiContext: common.UIAbilityContext, isDebug: boolean, appVersion: string) {
    WsAppRuntimeInfo.appVersion = appVersion;

    const context: Context = uiContext.getApplicationContext();
    // 通过 bundleManager 同步读取当前包名，用于登录模块按企业分发/正式分发选择微信 AppId。
    // 不依赖 appStatus().getPackageName()，因为 setupAppLogin 早于 appStatus 注入，
    // 此时拿到的是空串会导致 RDM 包走到 release AppId。
    let bundleId = '';
    try {
      const bundleInfo = bundleManager.getBundleInfoForSelfSync(
        bundleManager.BundleFlag.GET_BUNDLE_INFO_WITH_APPLICATION
      );
      bundleId = bundleInfo.name;
    } catch (e) {
      console.error(`[Kommon] getBundleInfoForSelfSync failed: ${e}`);
    }
    // MMKV 必须在 onAppStartup() 之前初始化：
    // 对齐 Android（AndroidKmkvInitTask 里 MMKV.initialize(app)）
    // 与 iOS（IOSMmkvSetup 里 MMKV.initialize(rootDir: nil)）的模式，
    // 先让 native 侧 @tencent/mmkv so 加载并设置 rootDir，
    // 之后 KMP commonMain 的 KmkvStorage 通过 mmkv_c_* C 接口访问才会拿到有效句柄。
    MmkvManager.getInstance().initMmkv(context as common.ApplicationContext, () => {});

    // consumers
    registerAppStartupService(context);
    registerOhosResService(context);
    registerOhosNetworkService();
    registerOhosFileManagerService(context as common.ApplicationContext);
    // 对应 KMP 侧 IFileCacheManager：同步文件查询/写入/删除，供音频/本地缓存场景使用
    registerOhosFileCacheService();
    // 注入 ArkWeb Cookie 服务，确保 KMP WebView Cookie 写入真实 ArkWeb CookieManager
    registerOhosCookieService();
    getHarmonyStartupProvider().setupAppReport(new OhosAppReportCallback());
    getHarmonyStartupProvider().setupAppShare(new OhosAppShareCallback());
    getHarmonyStartupProvider().setupAppResManager(new OhosAppResManagerCallback(uiContext));
    getHarmonyStartupProvider().setupAppRouter(new OhosAppRouterCallback(uiContext));
    getHarmonyStartupProvider().setupAppAlert(new OhosAppAlertCallback());
    getHarmonyStartupProvider().setupAppStatus(new OhosAppStatusCallback());
    // 注入陀螺仪能力（sensor.on/off）
    getHarmonyStartupProvider().setupAppGyroscope(new OhosAppGyroscopeCallback());
    // 注入状态栏控制（window.setWindowSystemBarProperties）
    getHarmonyStartupProvider().setupAppStatusBar(new OhosAppStatusBarCallback());
    // 注入窗口管理能力（屏幕方向、状态栏显隐、全屏控制）
    getHarmonyStartupProvider().setupAppWindow(new OhosAppWindowBridgeCallback());
    // 注入系统媒体音量监听（AudioVolumeManager.on('volumeChange')）
    getHarmonyStartupProvider().setupSystemVolumeController(new OhosSystemVolumeControllerCallback());
    // 注入应用权限（abilityAccessCtrl 定位权限查询与申请）
    // Permission 回调需要稳定的 UIAbilityContext，用于 requestPermissionsFromUser，
    // 避免在运行时通过 getContext(this) 推断上下文失败导致权限弹窗不出现。
    getHarmonyStartupProvider().setupAppPermission(new OhosAppPermissionCallback(uiContext));
    // 注入定位能力（geoLocationManager.getCurrentLocation）
    getHarmonyStartupProvider().setupAppLocation(new OhosAppLocationCallback());
    // 最后调用App启动
    // isDebug / appVersion 由 entry（application 模块）的 BuildProfile 注入，
    // HAR 模块的 BuildProfile 不包含 VERSION_NAME，不能在此直接读取。
    getHarmonyStartupProvider().onAppStartup(isDebug, bundleId, bundleId, appVersion, context);
  }
}

export function loadKnoi(uiContext: common.UIAbilityContext, isDebug: boolean, appVersion: string) {
  setup('libumbrella.so', isDebug);
  init();
  // 初始化qncore的注入,再晚会导致外部拉起时还没注入
  Kommon.getInstance().lazySetup(uiContext, isDebug, appVersion);
}
