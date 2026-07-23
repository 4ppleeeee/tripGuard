/***
*    !!!  GEN CODE DO NOT EDIT  !!!
***/
export interface HarmonyStartupService {
            
   initQimei(appKey: string, channelId: string, isDebug: boolean, callback: Function): void;

   getUskey(appKey: string, appVersion: string, businessId: string, qimei36: string, busInfo: string): string;

   initBugly(appId: string, appKey: string, qimeiAppKey: string, qimeiChannelId: string, appVersion: string, buildNumber: string, userId: string, appChannel: string, isDebug: boolean, callback: Function): void;

   initBeacon(appKey: string, appVersion: string, packageName: string, qimeiAppKey: string, qimeiChannelId: string, isDebug: boolean, userAgreePrivacy: boolean, callback: Function): void;

   initReshub(appId: string, appKey: string, appVersion: string, qimei: string, useTestEnv: boolean, isDebug: boolean, callback: Function): void;

   initToggle(appId: string, appKey: string, appVersion: string, userId: string, deviceId: string, useTestEnv: boolean, isDebug: boolean, callback: Function): void;

   switchToggleUser(userId: string): void;

   initUploadSdk(bizAppId: number, bizDomain: string, callback: Function): void;

   initTuring(appId: string, channelId: number, userId: string, isDebug: boolean, callback: Function): void;

   initQQLogin(appId: string, callback: Function): void;

   initWXLogin(appId: string, callback: Function): void;

   initMmkv(callback: Function): void;

   initTabExp(appId: string, appVersion: string, isDebug: boolean, callback: Function): void;

}

export interface OhosCookieService {
            
   getCookie(url: string): string;

   setCookie(url: string, value: string): void;

   removeAllCookies(): void;

}

export interface OhosFileCacheService {
            
   containsFile(filePath: string): boolean;

   cacheFile(filePath: string, data: string): boolean;

   removeFile(filePath: string): void;

   removeDirPathFile(dirPath: string): void;

}

export interface OhosFileManagerService {
            
   readUserFile(fileName: string, needUser: boolean): object;

   writeUserFile(fileName: string, data: string, needUser: boolean): object;

   deleteUserFile(filePath: string): void;

   findFilesPath(dirName: string, fileNamePrefix: string, sortAscending: boolean): object;

   createCacheDir(dirName: string): string;

}

export interface OhosNetworkService {
            
   getTotalRxBytes(): object;

   getTotalTxBytes(): object;

   subscribeNetState(onChange: Function): void;

}

export interface OhosResService {
            
   getIconFontMapping(): object;

}

