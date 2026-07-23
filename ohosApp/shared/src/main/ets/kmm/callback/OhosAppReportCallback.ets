import { BeaconManager } from '../../startup/BeaconManager';
import { BuglyManager } from '../../startup/BuglyManager';
import { OhosAppReport } from '../../kniogen/qnPlatform/callback';
import { hilog } from '@kit.PerformanceAnalysisKit';
import { util } from '@kit.ArkTS';
import fs from '@ohos.file.fs';
import { LogZipArchiver } from '../../startup/LogZipArchiver';

const TAG = 'OhosAppReportCallback';
const BUGLY_NAME = 'BuglyException';

export class OhosAppReportCallback implements OhosAppReport {
  reportBeacon(event: string, paramsJson: string): void {
    BeaconManager.getInstance().reportBeacon(event, paramsJson);
  }

  reportBugly(msg: string, errorMessage: string): void {
    const reason = msg && msg.length > 0 ? msg : (errorMessage || 'bugly report');
    const stack = errorMessage || '';
    try {
      BuglyManager.getInstance().reportBugly(BUGLY_NAME, reason, stack);
    } catch (error) {
      hilog.error(0x0000, TAG, `reportBugly failed: ${JSON.stringify(error)}`);
    }
  }

  reportDt(event: string, paramsJson: string): void {
  }

  setPageStartFrom(from: string): void {
  }

  resetPageStartFrom(): void {
  }

  uploadLogToBugly(): void {
    try {
      BuglyManager.getInstance().uploadLogToBugly();
    } catch (error) {
      hilog.error(0x0000, TAG, `uploadLogToBugly failed: ${JSON.stringify(error)}`);
    }
  }

  prepareFeedbackLogZipBase64(onResult: Function): void {
    this.prepareFeedbackLogZipBase64Async(onResult);
  }

  private async prepareFeedbackLogZipBase64Async(onResult: Function): Promise<void> {
    let zipPath: string | null = null;
    try {
      zipPath = await LogZipArchiver.buildLogZip();
      if (!zipPath || !fs.accessSync(zipPath)) {
        hilog.error(0x0000, TAG, 'prepareFeedbackLogZipBase64: no log zip');
        onResult('');
        return;
      }
      const stat = fs.statSync(zipPath);
      if (stat.size <= 0) {
        hilog.error(0x0000, TAG, `prepareFeedbackLogZipBase64: empty zip, path=${zipPath}`);
        onResult('');
        return;
      }
      onResult(this.readFileBase64(zipPath, stat.size));
    } catch (error) {
      hilog.error(0x0000, TAG, `prepareFeedbackLogZipBase64 failed: ${JSON.stringify(error)}`);
      onResult('');
    } finally {
      if (zipPath) {
        try {
          if (fs.accessSync(zipPath)) {
            fs.unlinkSync(zipPath);
          }
        } catch (error) {
          hilog.warn(0x0000, TAG, `cleanup feedback log zip failed: ${JSON.stringify(error)}`);
        }
      }
    }
  }

  private readFileBase64(localPath: string, fileSize: number): string {
    const file = fs.openSync(localPath, fs.OpenMode.READ_ONLY);
    try {
      const buffer = new ArrayBuffer(fileSize);
      const readLen = fs.readSync(file.fd, buffer);
      const content = new Uint8Array(buffer, 0, readLen);
      return new util.Base64Helper().encodeToStringSync(content);
    } finally {
      fs.closeSync(file);
    }
  }
}
