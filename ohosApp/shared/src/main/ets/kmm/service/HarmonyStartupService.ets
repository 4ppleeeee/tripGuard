import { registerServiceProvider } from 'knoi';
import { HarmonyStartupService } from '../../kniogen/consumer';
import { getHarmonyStartupProvider } from '../../kniogen/kmmStartupStd/provider';
import { QimeiManager } from '../../startup/QimeiManager';
import { BuglyManager } from '../../startup/BuglyManager';
import { BeaconManager } from '../../startup/BeaconManager';
import { ResHubManager } from '../../startup/ResHubManager';
import { ToggleManager } from '../../startup/ToggleManager';
import { LoginSDKManager } from '../../startup/LoginSDKManager';
import { MmkvManager } from '../../startup/MmkvManager';
import { TabExpManager } from '../../startup/TabExpManager';
import { TuringManager } from '../../startup/TuringManager';
import { UploadSdkManager } from '../../startup/UploadSdkManager';
import { Context } from '@kit.AbilityKit';


export function registerAppStartupService(context: Context) {
  registerServiceProvider("HarmonyStartupService", true, new OhosLoginServiceImpl(context))
}

class OhosLoginServiceImpl implements HarmonyStartupService {
  context: Context;

  constructor(context: Context) {
    this.context = context;
  }

  initQimei(appKey: string, channelId: string, isDebug: boolean, callback: Function): void {
    QimeiManager.getInstance().initQimei(this.context, appKey, channelId, isDebug, callback);
  }

  getUskey(appKey: string, appVersion: string, businessId: string, qimei36: string, busInfo: string): string {
    return QimeiManager.getUskey(appKey, appVersion, businessId, qimei36, busInfo);
  }

  initBugly(
    appId: string,
    appKey: string,
    qimeiAppKey: string,
    qimeiChannelId: string,
    appVersion: string,
    buildNumber: string,
    userId: string,
    appChannel: string,
    isDebug: boolean,
    callback: Function
  ): void {
    BuglyManager.getInstance().initBugly(
      this.context.getApplicationContext(),
      appId,
      appKey,
      qimeiAppKey,
      qimeiChannelId,
      appVersion,
      buildNumber,
      userId,
      appChannel,
      isDebug,
      callback,
    );
  }

  initBeacon(
    appKey: string,
    appVersion: string,
    packageName: string,
    qimeiAppKey: string,
    qimeiChannelId: string,
    isDebug: boolean,
    userAgreePrivacy: boolean,
    callback: Function
  ): void {
    BeaconManager.getInstance().initBeacon(
      this.context.getApplicationContext(),
      appKey,
      appVersion,
      packageName,
      qimeiAppKey,
      qimeiChannelId,
      isDebug,
      userAgreePrivacy,
      callback,
    );
  }

  initReshub(
    appId: string,
    appKey: string,
    appVersion: string,
    qimei: string,
    useTestEnv: boolean,
    isDebug: boolean,
    callback: Function,
  ): void {
    ResHubManager.getInstance().initReshub(
      this.context.getApplicationContext(),
      appId,
      appKey,
      appVersion,
      qimei,
      useTestEnv,
      isDebug,
      callback,
    );
  }

  initToggle(
    appId: string,
    appKey: string,
    appVersion: string,
    userId: string,
    deviceId: string,
    useTestEnv: boolean,
    isDebug: boolean,
    callback: Function,
  ): void {
    ToggleManager.getInstance().initToggle(
      this.context.getApplicationContext(),
      appId,
      appKey,
      appVersion,
      userId,
      deviceId,
      useTestEnv,
      isDebug,
      (env: string) => {
        const toggleManager = ToggleManager.getInstance();
        getHarmonyStartupProvider().setupAppConfig(toggleManager);
        callback(env);
      },
    );
  }

  switchToggleUser(userId: string): void {
    ToggleManager.getInstance().switchUserAndUpdateConfig(userId);
  }

  initUploadSdk(bizAppId: number, bizDomain: string, callback: Function): void {
    UploadSdkManager.getInstance().initUploadSdk(bizAppId, bizDomain, callback);
  }

  initTuring(appId: string, channelId: number, userId: string, isDebug: boolean, callback: Function): void {
    TuringManager.getInstance().initTuring(
      this.context,
      appId,
      channelId,
      userId,
      isDebug,
      callback,
    );
  }

  initQQLogin(appId: string, callback: Function): void {
    LoginSDKManager.getInstance().initQQ(appId, callback)
  }

  initWXLogin(appId: string, callback: Function): void {
    LoginSDKManager.getInstance().initWX(appId, callback)
  }

  initTabExp(appId: string, appVersion: string, isDebug: boolean, callback: Function): void {
    TabExpManager.getInstance().initTabExp(appId, appVersion, isDebug, callback)
  }

  initMmkv(callback: Function): void {
    MmkvManager.getInstance().initMmkv(this.context.getApplicationContext(), callback);
  }
}
