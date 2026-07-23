import { registerServiceProvider } from 'knoi';
import { OhosFileCacheService } from '../../kniogen/consumer';
import { fileIo } from '@kit.CoreFileKit';
import { util } from '@kit.ArkTS';
import hilog from '@ohos.hilog';

const TAG = 'OhosFileCacheService';

/**
 * 同步的文件缓存服务，对应 KMP 侧 IFileCacheManager。
 *
 * 与 OhosFileManagerServiceImpl 的区别：
 *   - OhosFileManagerServiceImpl 返回 Promise（异步），面向业务层文件读写；
 *   - OhosFileCacheServiceImpl   同步返回，面向音频/本地缓存的 IFileCacheManager 接口。
 *
 * 使用 @kit.CoreFileKit 的 fileIo 同步 API（accessSync/mkdirSync/unlinkSync/rmdirSync 等）。
 */
export function registerOhosFileCacheService() {
  registerServiceProvider('OhosFileCacheService', true, new OhosFileCacheServiceImpl());
}

class OhosFileCacheServiceImpl implements OhosFileCacheService {

  /**
   * 同步判断文件是否存在。
   */
  containsFile(filePath: string): boolean {
    if (!filePath) return false;
    try {
      return fileIo.accessSync(filePath);
    } catch (err) {
      hilog.error(0x0000, TAG, 'containsFile failed, filePath=%{public}s, err=%{public}s',
        filePath, JSON.stringify(err) ?? '');
      return false;
    }
  }

  /**
   * 同步写入 Base64 文件内容到文件，自动创建父目录。
   * 存在则覆盖（TRUNC），不存在则创建（CREATE）。
   */
  cacheFile(filePath: string, data: string): boolean {
    if (!filePath || !data) return false;
    let file: fileIo.File | null = null;
    try {
      this.ensureParentDir(filePath);
      const bytes = new util.Base64Helper().decodeSync(data);
      file = fileIo.openSync(
        filePath,
        fileIo.OpenMode.CREATE | fileIo.OpenMode.READ_WRITE | fileIo.OpenMode.TRUNC,
      );
      fileIo.writeSync(file.fd, bytes.buffer as ArrayBuffer);
      return true;
    } catch (err) {
      hilog.error(0x0000, TAG, 'cacheFile failed, filePath=%{public}s, err=%{public}s',
        filePath, JSON.stringify(err) ?? '');
      return false;
    } finally {
      if (file) {
        try {
          fileIo.closeSync(file);
        } catch (closeErr) {
          hilog.error(0x0000, TAG, 'cacheFile close failed, err=%{public}s',
            JSON.stringify(closeErr) ?? '');
        }
      }
    }
  }

  /**
   * 同步删除单个文件，不存在时静默忽略。
   */
  removeFile(filePath: string): void {
    if (!filePath) return;
    try {
      if (fileIo.accessSync(filePath)) {
        fileIo.unlinkSync(filePath);
      }
    } catch (err) {
      hilog.error(0x0000, TAG, 'removeFile failed, filePath=%{public}s, err=%{public}s',
        filePath, JSON.stringify(err) ?? '');
    }
  }

  /**
   * 同步递归删除目录及其所有子项。
   */
  removeDirPathFile(dirPath: string): void {
    if (!dirPath) return;
    try {
      if (!fileIo.accessSync(dirPath)) return;
      this.removePathRecursively(dirPath);
    } catch (err) {
      hilog.error(0x0000, TAG, 'removeDirPathFile failed, dirPath=%{public}s, err=%{public}s',
        dirPath, JSON.stringify(err) ?? '');
    }
  }

  /**
   * 确保文件的父目录存在，不存在则递归创建。
   */
  private ensureParentDir(filePath: string): void {
    const lastSlash = filePath.lastIndexOf('/');
    if (lastSlash <= 0) {
      return;
    }
    const parentDir = filePath.substring(0, lastSlash);
    try {
      if (!fileIo.accessSync(parentDir)) {
        fileIo.mkdirSync(parentDir, true);
      }
    } catch (err) {
      hilog.error(0x0000, TAG, 'ensureParentDir failed, dir=%{public}s, err=%{public}s',
        parentDir, JSON.stringify(err) ?? '');
    }
  }

  /**
   * 递归删除文件或目录，尽量对齐 Android File.deleteRecursively / iOS removeItemAtPath。
   */
  private removePathRecursively(path: string): void {
    const stat = fileIo.statSync(path);
    if (stat.isDirectory()) {
      const entries = fileIo.listFileSync(path);
      const normalizedDir = path.endsWith('/') ? path : `${path}/`;
      for (const name of entries) {
        this.removePathRecursively(`${normalizedDir}${name}`);
      }
      fileIo.rmdirSync(path);
      return;
    }
    if (stat.isFile()) {
      fileIo.unlinkSync(path);
    }
  }
}
