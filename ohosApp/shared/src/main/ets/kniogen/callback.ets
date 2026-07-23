/***
*    !!!  GEN CODE DO NOT EDIT  !!!
***/
export interface OhosAppRouter {
            
   toScheme(scheme: string): void;

   toComposePage(pageName: string, pageData: object, launchType: string): void;

   goBack(): void;

   moveTaskToBack(): void;

}

export interface OhosAppShare {
            
   isShareChannelSupported(channelId: string): boolean;

   shareLogToWeChat(): void;

   shareLogToWeCom(): void;

   shareToChannel(channelId: string, title: string, desc: string, url: string, imagePath: string): void;

}

export interface IKmmContext {
            
   getWindowId(): number;

}

export interface OhosAppAlert {
            
   showToast(msg: string, duration?: number, debug?: boolean): void;

   showDialog(title: string, msg: string): void;

   showDecorDebugView(msg: string): void;

   hideCurrentToast(): void;

}

export interface OhosAppConfig {
            
   getShiplyConfig(key: string, defaultValue: string): string;

   getShiplySwitch(key: string, defaultValue: boolean): boolean;

}

export interface OhosAppReport {
            
   reportBeacon(event: string, paramsJson: string): void;

   reportBugly(msg: string, errorMessage: string): void;

   reportDt(event: string, paramsJson: string): void;

   setPageStartFrom(from: string): void;

   resetPageStartFrom(): void;

   uploadLogToBugly(): void;

   prepareFeedbackLogZipBase64(onResult: Function): void;

}

export interface OhosAppGyroscope {
            
   isAvailable(): boolean;

   startListening(samplingRateName: string, onData: Function): void;

   stopListening(): void;

}

export interface OhosAppLocation {
            
   requestLocation(scene: string, legacyCode: number, onResult: Function): void;

}

export interface OhosAppPermission {
            
   hasLocationPermission(): boolean;

   requestLocationPermission(isForceRequestPermission: boolean, onResult: Function): void;

}

export interface OhosAppResManager {
            
   getAssetJson(fileName: string): string;

   copyToClipboard(content: string): void;

   preloadImage(url: string, onResult: Function): void;

   saveImage(url: string, onResult: Function): void;

   saveVideo(localFilePath: string): void;

   selectImage(maxCount: number, onResult: Function): void;

   selectMedia(mediaType: number, maxCount: number, cropEnabled: boolean, aspectX: number, aspectY: number, outputX: number, outputY: number, onResult: Function): void;

   selectMediaSource(mediaType: number, maxCount: number, cropEnabled: boolean, aspectX: number, aspectY: number, outputX: number, outputY: number, onResult: Function): void;

   takePhoto(cropEnabled: boolean, aspectX: number, aspectY: number, outputX: number, outputY: number, onResult: Function): void;

   consumeMediaResult(resultToken: number): string;

   getPaletteColor(imageUrl: string, paletteParam: string, onResult: Function): void;

   preloadLottieToMemory(url: string, onResult: Function): void;

   preloadAlphaVideo(url: string, onResult: Function): void;

   uploadImage(serialNo: number, filePath: string, scene: number, authJson: string, onProgress: Function, onResult: Function): void;

   uploadVideo(serialNo: number, filePath: string, authJson: string, onProgress: Function, onResult: Function): void;

}

export interface OhosAppStatus {
            
   subscribeTheme(onUiModeChanged: Function): void;

   subscribeTextScaleRatio(onTextScaleRatioChanged: Function): void;

   subscribeFontFamily(onFontFamilyChanged: Function): void;

   subscribeNetState(onChange: Function): void;

}

export interface OhosAppStatusBar {
            
   setWhiteBar(): void;

   setBlackBar(): void;

   resetStatusBar(): void;

   setStatusBarVisibility(visible: boolean): void;

   setCustomBar(textColor: string, backgroundColor: string): void;

}

export interface OhosAppWindowBridge {
            
   setScreenOrientation(orientation: string): void;

   enterFullScreen(): void;

   exitFullScreen(): void;

   getScreenOrientation(): string;

   startDeviceOrientationListening(onOrientationChanged: Function): boolean;

   stopDeviceOrientationListening(): void;

}

export interface OhosHttpRequestService {
            
   jsonPostRequest(builder: void, onResult: Function): void;

   formPostRequest(builder: void, onResult: Function): void;

   getRequest(builder: void, onResult: Function): void;

   multiJsonPostRequest(builder: void, onResult: Function): void;

   streamPostRequest(builder: void, onResult: Function): void;

   sseRequest(builder: void, onResult: Function): void;

   pbPostRequest(url: string, bodyBase64: string, headers: object, onResult: Function): void;

}

export interface OhosSystemVolumeController {
            
   startListening(onVolumeChanged: Function): void;

   stopListening(): void;

}
