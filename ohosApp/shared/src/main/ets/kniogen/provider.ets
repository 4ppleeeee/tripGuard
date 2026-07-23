/***
*    !!!  GEN CODE DO NOT EDIT  !!!
***/
import { getService } from "knoi"


export interface HarmonyPageStackManager {
            
   onPageCreated(contxt: object): void;

   onPageDestroyed(contxt: object): void;

}

export function getHarmonyPageStackManager(): HarmonyPageStackManager {
   return getService<HarmonyPageStackManager>("HarmonyPageStackManager")
}


export interface HarmonyStartupProvider {
            
   setupAppConfig(appConfig: object): void;

   setupAppReport(appReport: object): void;

   setupAppShare(appShare: object): void;

   setupAppResManager(resManager: object): void;

   setupAppRouter(router: object): void;

   setupAppAlert(alert: object): void;

   setupAppGyroscope(gyroscope: object): void;

   setupAppStatusBar(statusBar: object): void;

   setupAppWindow(bridge: object): void;

   setupSystemVolumeController(volumeController: object): void;

   setupAppPermission(permission: object): void;

   setupAppLocation(location: object): void;

   setupAppStatus(status: object): void;

   onAppStartup(isDebug: boolean, appVersion: string, nativeContext?: object | null): void;

   onAppForeground(): void;

   onAppBackground(): void;

}

export function getHarmonyStartupProvider(): HarmonyStartupProvider {
   return getService<HarmonyStartupProvider>("HarmonyStartupProvider")
}

