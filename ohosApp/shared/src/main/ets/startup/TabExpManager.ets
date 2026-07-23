import { hilog } from '@kit.PerformanceAnalysisKit';

const TAG = 'TabExpManager';

/**
 * 鸿蒙端 AB SDK 预留管理器。
 * 当前 demo 壳先透传初始化回调，后续可在这里接入正式 SDK。
 */
export class TabExpManager {
  private static instance: TabExpManager;

  private constructor() {
  }

  static getInstance(): TabExpManager {
    if (!TabExpManager.instance) {
      TabExpManager.instance = new TabExpManager();
    }
    return TabExpManager.instance;
  }

  initTabExp(appId: string, appVersion: string, isDebug: boolean, callback: Function): void {
    hilog.info(0x0000, TAG, `initTabExp noop, appId=${appId}, appVersion=${appVersion}, isDebug=${isDebug}`);
    callback();
  }
}
