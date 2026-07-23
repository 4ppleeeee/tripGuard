/***
*    !!!  GEN CODE DO NOT EDIT  !!!
***/
export interface IKmmContext {
            
   doShare(itemString: string): void;

   goBack(): void;

}

export interface OhosAppGyroscope {
            
   isAvailable(): boolean;

   registerListener(configJson: string, listenerId: string, onGyroscopeChanged: Function): boolean;

   unregisterListener(listenerId: string): void;

   unregisterAllListeners(): void;

   getLatestData(): string;

}

export interface OhosAppStatus {
            
   subscribeTheme(onUiModeChanged: Function): void;

   subscribeTextScaleRatio(onTextScaleRatioChanged: Function): void;

   getDefaultFontFamily(): string;

   subscribeFontFamily(onFontFamilyChanged: Function): void;

   isFontResourceReady(fontId: string, fontFamily: string): boolean;

   downloadFontResource(fontId: string, fontFamily: string, onFontDownloadStatusChanged: Function): void;

   subscribeNetState(onNetStateChanged: Function): void;

   unsubscribeNetState(): void;

   setDarkMode(isDarkMode: boolean): void;

}

