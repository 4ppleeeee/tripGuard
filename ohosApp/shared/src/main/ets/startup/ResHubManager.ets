import { common } from '@kit.AbilityKit';
import { deviceInfo } from '@kit.BasicServicesKit';
import { hilog } from '@kit.PerformanceAnalysisKit';
import { ResHub, ResHubParam } from '@shiply/shiply';
import { QimeiManager } from './QimeiManager';

/**
 * Harmony Reshub 初始化管理
 */
export class ResHubManager {
  private static instance: ResHubManager;
  private isInitialized: boolean = false;

  private constructor() {
  }

  static getInstance(): ResHubManager {
    if (!ResHubManager.instance) {
      ResHubManager.instance = new ResHubManager();
    }
    return ResHubManager.instance;
  }

  initReshub(
    context: common.ApplicationContext,
    appId: string,
    appKey: string,
    appVersion: string,
    qimei: string,
    useTestEnv: boolean,
    isDebug: boolean,
    callback: Function,
  ): void {
    const env = useTestEnv ? 'test' : 'release';
    if (this.isInitialized) {
      callback(env);
      return;
    }

    try {
      let params = new ResHubParam();
      params.appVersion = appVersion;
      params.qimei = qimei && qimei.length > 0 ? qimei : QimeiManager.getQimei36();
      params.deviceType = `${deviceInfo.deviceType}`;
      params.systemVersion = `${deviceInfo.sdkApiVersion}`;
      params.isDebugPackage = isDebug;
      params.resStoragePath = `${context.filesDir}/reshub/resource`;
      params.resConfigStoragePath = `${context.filesDir}/reshub/config`;
      params.localPresetResPath = `${context.filesDir}/reshub/preset`;
      params.callbackOnMainThread = true;
      params.configUpdateInterval = 300;
      params.enableSampleReport = true;
      params.sampling = 100;

      ResHub.initResHubCenter(
        params,
        appId,
        appKey,
        env,
        (level: number, log: string) => {
          if (isDebug) {
            hilog.info(0x0000, 'ResHubInit', `level=${level}, log=${log}`);
          }
        },
      );
      this.isInitialized = true;
    } catch (error) {
      hilog.error(0x0000, 'ResHubInit', `initReshub failed: ${JSON.stringify(error)}`);
    }

    callback(env);
  }
}
