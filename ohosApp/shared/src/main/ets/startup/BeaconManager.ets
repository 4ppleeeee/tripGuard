import { common } from '@kit.AbilityKit';
import { hilog } from '@kit.PerformanceAnalysisKit';
import { HrDtReport } from 'dtreport';
import { DtrConfig, DtrEvent, EventType } from 'dtreport/src/main/js/shared';
import { QimeiManager } from './QimeiManager';

/**
 * Harmony Beacon 初始化管理
 */
export class BeaconManager {
  private static instance: BeaconManager;
  private isInitialized: boolean = false;

  private constructor() {
  }

  static getInstance(): BeaconManager {
    if (!BeaconManager.instance) {
      BeaconManager.instance = new BeaconManager();
    }
    return BeaconManager.instance;
  }

  initBeacon(
    context: common.ApplicationContext,
    appKey: string,
    appVersion: string,
    packageName: string,
    qimeiAppKey: string,
    qimeiChannelId: string,
    isDebug: boolean,
    userAgreePrivacy: boolean,
    callback: Function,
  ): void {
    if (this.isInitialized) {
      callback();
      return;
    }

    try {
      const config = new DtrConfig.Builder()
        .packageName(packageName)
        .appVersion(appVersion)
        .build();
      HrDtReport.getInstance().start(context, appKey, config);
      this.isInitialized = true;

      if (userAgreePrivacy) {
        this.syncQimei(context, qimeiAppKey, qimeiChannelId, isDebug);
      }
    } catch (error) {
      hilog.error(0x0000, 'BeaconInit', `initBeacon failed: ${JSON.stringify(error)}`);
    }

    callback();
  }

  reportBeacon(event: string, paramsJson: string): void {
    try {
      const dtrEvent = new DtrEvent.Builder()
        .eventCode(event)
        .eventType(EventType.NORMAL)
        .params(paramsJson || '{}')
        .build();
      HrDtReport.getInstance().report(dtrEvent);
    } catch (error) {
      hilog.error(0x0000, 'BeaconReport', `reportBeacon failed: ${JSON.stringify(error)}`);
    }
  }

  private syncQimei(
    context: common.ApplicationContext,
    qimeiAppKey: string,
    qimeiChannelId: string,
    isDebug: boolean,
  ): void {
    const applyQimei = (qimei36: string) => {
      if (!qimei36 || qimei36.length <= 0) {
        return;
      }
      HrDtReport.getInstance().setQimei(qimei36, '1');
    };

    const cachedQimei = QimeiManager.getQimei36();
    if (cachedQimei && cachedQimei.length > 0) {
      applyQimei(cachedQimei);
      return;
    }

    if (!qimeiAppKey || !qimeiChannelId) {
      return;
    }

    QimeiManager.getInstance().initQimei(
      context,
      qimeiAppKey,
      qimeiChannelId,
      isDebug,
      (qimei36: string): void => applyQimei(qimei36),
    );
  }
}
