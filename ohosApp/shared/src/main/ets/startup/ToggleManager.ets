import { common } from '@kit.AbilityKit';
import { deviceInfo } from '@kit.BasicServicesKit';
import { hilog } from '@kit.PerformanceAnalysisKit';
import { RDelivery, RDeliveryConfig, RDUpdateStrategy } from '@shiply/shiply';

/**
 * Harmony Shiply/Toggle 初始化与查询管理
 */
export class ToggleManager {
  private static instance: ToggleManager;
  private isInitialized: boolean = false;
  private dbPath: string = '';
  private config?: RDeliveryConfig;
  private logger?: (level: number, message: string) => void;

  private constructor() {
  }

  static getInstance(): ToggleManager {
    if (!ToggleManager.instance) {
      ToggleManager.instance = new ToggleManager();
    }
    return ToggleManager.instance;
  }

  initToggle(
    context: common.ApplicationContext,
    appId: string,
    appKey: string,
    appVersion: string,
    userId: string,
    deviceId: string,
    useTestEnv: boolean,
    isDebug: boolean,
    callback: Function,
  ): void {
    const env = useTestEnv ? 'test' : 'online';
    if (this.isInitialized) {
      this.switchUserAndUpdateConfig(userId);
      callback(env);
      return;
    }

    try {
      const config = new RDeliveryConfig();
      config.logicEnvironment = useTestEnv ? '1' : '';
      config.appId = appId;
      config.appKey = appKey;
      config.userId = userId || '';
      config.deviceId = deviceId || '';
      config.language = 'zh_CN';
      config.appVersion = appVersion;
      config.osVersion = `${deviceInfo.sdkApiVersion}`;
      config.bundleId = 'com.tencent.news.base.app';
      config.updateStrategy = RDUpdateStrategy.SdkInit | (1 << 1) | (1 << 3);
      config.updateInterval = 4 * 60 * 60;
      config.is_debugPackage = false;

      const dbPath = `${context.filesDir}/shiply/config`;
      const logger = (level: number, message: string) => {
        if (isDebug) {
          hilog.info(0x0000, 'ToggleInit', `level=${level}, log=${message}`);
        }
      };

      RDelivery.SdkStart(dbPath, config, logger);
      this.dbPath = dbPath;
      this.config = config;
      this.logger = logger;
      this.isInitialized = true;
      this.requestFullRemoteData();
    } catch (error) {
      hilog.error(0x0000, 'ToggleInit', `initToggle failed: ${JSON.stringify(error)}`);
    }

    callback(env);
  }

  switchUserAndUpdateConfig(userId: string): void {
    if (!this.isInitialized || !this.config || !this.logger || this.dbPath.length === 0) {
      return;
    }
    const normalizedUserId = userId || '';
    if (this.config.userId !== normalizedUserId) {
      // Shiply Harmony SDK 当前未暴露单独 switchUser API，复用原配置重新 SdkStart 后再主动拉取。
      this.config.userId = normalizedUserId;
      RDelivery.SdkStart(this.dbPath, this.config, this.logger);
    }
    this.requestFullRemoteData();
  }

  getStringValue(key: string, defaultValue: string): string {
    const data = RDelivery.SyncGetRDeliveryDataByKey(key);
    if (data === undefined || data === null) {
      return defaultValue;
    }
    const value = `${data.value}`;
    return value.length > 0 ? value : defaultValue;
  }

  isEnable(key: string, defaultValue: boolean): boolean {
    const data = RDelivery.SyncGetRDeliveryDataByKey(key);
    if (data === undefined || data === null) {
      return defaultValue;
    }
    const switchState = Number(data.switchState);
    if (Number.isNaN(switchState)) {
      return defaultValue;
    }
    return switchState === 1;
  }

  getShiplyConfig(key: string, defaultValue: string): string {
    return this.getStringValue(key, defaultValue);
  }

  getShiplySwitch(key: string, defaultValue: boolean): boolean {
    return this.isEnable(key, defaultValue);
  }

  getTabExpInt(_: string, defaultValue: number): number {
    // 鸿蒙端暂无 TAB/Roma 实验 SDK，保持默认值兜底。
    return defaultValue;
  }

  private requestFullRemoteData(): void {
    RDelivery.RequestFullRemoteData({}, (errorCode: number, configList) => {
      hilog.info(0x0000, 'ToggleInit', `RequestFullRemoteData callback, errorCode=${errorCode}`);
    });
  }
}
