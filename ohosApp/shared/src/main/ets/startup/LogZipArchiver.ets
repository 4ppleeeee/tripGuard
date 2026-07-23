import fs from '@ohos.file.fs';
import zlib from '@ohos.zlib';
import { hilog } from '@kit.PerformanceAnalysisKit';
import { BuglyManager } from './BuglyManager';

const TAG = 'LogZipArchiver';

/**
 * 日志打包工具：把 `BuglyManager` 管理的 xlog 打平目录复制到 `cacheDir` 下的 staging 目录，
 * 再通过 `@ohos.zlib` 的 `compressFiles` 压缩成一个 `kmm_logs_{ts}.zip`，
 * 供"分享到微信 / 企业微信"消费。
 *
 * 对齐 Android `SetupAppShare.buildOrganizedLogZip`：
 * - zip 名字以 `kmm_logs_` 为前缀，和 `uploadLogToBugly` 的 `kmm_diag_zip_` 区分；
 * - zip 内部不再按 business/video/rcprofiler 做子目录（因为 ohos 端 TDLog 本身就是打平、文件名已带前缀）；
 * - 产物放到 cacheDir，由系统自动回收；调用方可自行 cleanup。
 */
export class LogZipArchiver {

  /**
   * 打包当前所有 xlog 日志到一个 zip 文件。
   *
   * @returns 成功时返回 zip 绝对路径；失败或没有日志时返回 null。
   */
  static async buildLogZip(): Promise<string | null> {
    const bugly = BuglyManager.getInstance();
    const rootLogDir = bugly.getRootLogDir();
    const appContext = bugly.getAppContext();
    if (!rootLogDir || !appContext) {
      hilog.error(0x0000, TAG, 'buildLogZip: BuglyManager is not initialized');
      return null;
    }
    if (!fs.accessSync(rootLogDir)) {
      hilog.error(0x0000, TAG, `buildLogZip: rootLogDir not exists, path=${rootLogDir}`);
      return null;
    }

    const ts = Date.now();
    const stagingDir = `${appContext.cacheDir}/kmm_log_share_${ts}`;
    const zipPath = `${appContext.cacheDir}/kmm_logs_${ts}.zip`;

    try {
      // 1) 建 staging 目录，把 rootLogDir 下的所有常规文件 **复制** 进来。
      //    直接 compressFile(rootLogDir) 会把整个文件夹名一起塞进 zip，解压出来多一层 `logs/`，
      //    不够干净；同时避免 compressFiles 时 TDLog 正在写导致的文件句柄占用问题。
      if (!fs.accessSync(stagingDir)) {
        fs.mkdirSync(stagingDir, true);
      }
      const files = fs.listFileSync(rootLogDir);
      let copied = 0;
      files.forEach((name: string) => {
        const src = `${rootLogDir}/${name}`;
        const dst = `${stagingDir}/${name}`;
        try {
          const stat = fs.statSync(src);
          if (stat.isFile()) {
            fs.copyFileSync(src, dst);
            copied++;
          }
        } catch (err) {
          hilog.warn(0x0000, TAG, `copy skip, name=${name}, err=${JSON.stringify(err)}`);
        }
      });

      if (copied === 0) {
        hilog.error(0x0000, TAG, 'buildLogZip: no log files to pack');
        LogZipArchiver.safeRmDir(stagingDir);
        return null;
      }

      // 2) 压缩 staging 目录到 zip。
      await zlib.compressFile(stagingDir, zipPath, { level: zlib.CompressLevel.COMPRESS_LEVEL_DEFAULT_COMPRESSION });

      // 3) staging 复制产物可以立即清理，zip 保留给微信读取。
      LogZipArchiver.safeRmDir(stagingDir);

      if (!fs.accessSync(zipPath)) {
        hilog.error(0x0000, TAG, `buildLogZip: zip missing after compress, path=${zipPath}`);
        return null;
      }
      hilog.info(0x0000, TAG, `buildLogZip success, path=${zipPath}, files=${copied}`);
      return zipPath;
    } catch (error) {
      hilog.error(0x0000, TAG, `buildLogZip failed: ${JSON.stringify(error)}`);
      LogZipArchiver.safeRmDir(stagingDir);
      return null;
    }
  }

  private static safeRmDir(dir: string): void {
    try {
      if (fs.accessSync(dir)) {
        fs.rmdirSync(dir);
      }
    } catch (err) {
      hilog.warn(0x0000, TAG, `safeRmDir failed, dir=${dir}, err=${JSON.stringify(err)}`);
    }
  }
}
