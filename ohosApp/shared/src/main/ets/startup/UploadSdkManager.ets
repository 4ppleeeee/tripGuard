import { hilog } from '@kit.PerformanceAnalysisKit';

const TAG = 'UploadSdkManager';

export class UploadSdkManager {
  private static instance: UploadSdkManager;
  private bizAppId: number = 0;
  private bizDomain: string = '';

  static getInstance(): UploadSdkManager {
    if (!UploadSdkManager.instance) {
      UploadSdkManager.instance = new UploadSdkManager();
    }
    return UploadSdkManager.instance;
  }

  initUploadSdk(bizAppId: number, bizDomain: string, callback: Function): void {
    try {
      this.bizAppId = bizAppId;
      this.bizDomain = bizDomain;
      hilog.info(0x0000, TAG, `initUploadSdk bizAppId=${bizAppId}, bizDomain=${bizDomain}`);
    } catch (error) {
      hilog.error(0x0000, TAG, `initUploadSdk failed: ${JSON.stringify(error)}`);
    } finally {
      callback();
    }
  }

  getBizAppId(): number {
    return this.bizAppId;
  }

  getBizDomain(): string {
    return this.bizDomain;
  }
}
