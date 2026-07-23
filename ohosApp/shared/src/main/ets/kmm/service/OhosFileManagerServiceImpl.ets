import { registerServiceProvider } from 'knoi';
import { OhosFileManagerService } from '../../kniogen/consumer';
import { fileIo } from '@kit.CoreFileKit';
import { common } from '@kit.AbilityKit';
import hilog from '@ohos.hilog';

const TAG = 'OhosFileManagerService';

let appContext: common.ApplicationContext | null = null;

/**
 * 初始化文件管理服务，并向 knoi 注册 provider。
 * 必须在 knoi init() 之后调用。
 */
export function registerOhosFileManagerService(context: common.ApplicationContext) {
  appContext = context;
  registerServiceProvider('OhosFileManagerService', true, new OhosFileManagerServiceImpl());
}

class OhosFileManagerServiceImpl implements OhosFileManagerService {

  /**
   * 读取文件内容。
   * @param fileName 文件名（或相对路径，如 "dir/file.txt"）
   * @param needUser 是否按用户隔离目录（true=filesDir，false=cacheDir）
   * @returns Promise<string> 文件内容字符串；失败时 resolve 空字符串
   */
  readUserFile(fileName: string, needUser: boolean): object {
    return new Promise<string>((resolve) => {
      try {
        const filePath = this.buildFilePath(fileName, needUser);
        this.ensureParentDir(filePath);
        const content = fileIo.readTextSync(filePath);
        resolve(content);
      } catch (err) {
        hilog.error(0x0000, TAG, 'readUserFile failed, fileName=%{public}s, err=%{public}s',
          fileName, JSON.stringify(err) ?? '');
        resolve('');
      }
    });
  }

  /**
   * 写入文件内容。
   * @param fileName 文件名（或相对路径）
   * @param data 要写入的字符串内容
   * @param needUser 是否按用户隔离目录
   * @returns Promise<string> 成功时 resolve "ok"；失败时 resolve 空字符串
   */
  writeUserFile(fileName: string, data: string, needUser: boolean): object {
    return new Promise<string>((resolve) => {
      const filePath = this.buildFilePath(fileName, needUser);
      this.ensureParentDir(filePath);
      let file: fileIo.File | null = null;
      try {
        // 使用 CREATE | READ_WRITE | TRUNC：不存在则创建，存在则清空后覆盖写入。
        file = fileIo.openSync(
          filePath,
          fileIo.OpenMode.CREATE | fileIo.OpenMode.READ_WRITE | fileIo.OpenMode.TRUNC,
        );
        fileIo.writeSync(file.fd, data);
        resolve('ok');
      } catch (err) {
        hilog.error(0x0000, TAG, 'writeUserFile failed, fileName=%{public}s, err=%{public}s',
          fileName, JSON.stringify(err) ?? '');
        resolve('');
      } finally {
        if (file) {
          try {
            fileIo.closeSync(file);
          } catch (closeErr) {
            hilog.error(0x0000, TAG, 'writeUserFile close failed, err=%{public}s',
              JSON.stringify(closeErr) ?? '');
          }
        }
      }
    });
  }

  /**
   * 删除文件。
   * @param filePath 文件绝对路径或相对路径（相对于 filesDir）
   */
  deleteUserFile(filePath: string): void {
    try {
      const resolvedPath = this.resolveAbsPath(filePath);
      if (fileIo.accessSync(resolvedPath)) {
        fileIo.unlinkSync(resolvedPath);
      }
    } catch (err) {
      hilog.error(0x0000, TAG, 'deleteUserFile failed, filePath=%{public}s, err=%{public}s',
        filePath, JSON.stringify(err) ?? '');
    }
  }

  /**
   * 查找指定目录下匹配前缀的文件路径列表。
   * @param dirName 目录名（相对于 filesDir）
   * @param fileNamePrefix 文件名前缀过滤
   * @param sortAscending 是否按文件名升序排列
   * @returns Promise<string> JSON 编码的绝对路径数组
   */
  findFilesPath(dirName: string, fileNamePrefix: string, sortAscending: boolean): object {
    return new Promise<string>((resolve) => {
      try {
        const ctx = appContext;
        if (!ctx) {
          resolve('[]');
          return;
        }
        const dirPath = `${ctx.filesDir}/${dirName}`;
        if (!fileIo.accessSync(dirPath)) {
          resolve('[]');
          return;
        }
        const entries = fileIo.listFileSync(dirPath);
        let matched = entries.filter((name) => name.startsWith(fileNamePrefix));
        if (sortAscending) {
          matched = matched.sort((a, b) => a < b ? -1 : a > b ? 1 : 0);
        } else {
          matched = matched.sort((a, b) => a > b ? -1 : a < b ? 1 : 0);
        }
        const paths = matched.map((name) => `${dirPath}/${name}`);
        resolve(JSON.stringify(paths));
      } catch (err) {
        hilog.error(0x0000, TAG, 'findFilesPath failed, dirName=%{public}s, err=%{public}s',
          dirName, JSON.stringify(err) ?? '');
        resolve('[]');
      }
    });
  }

  /**
   * 在 app 缓存目录下创建子文件夹，返回该文件夹的绝对路径。
   * 如果目录已存在则直接返回路径，不会重复创建。
   *
   * @param dirName 子文件夹名称（相对于 app 缓存根目录）
   * @returns 创建成功返回绝对路径，失败返回空字符串
   */
  createCacheDir(dirName: string): string {
    try {
      const ctx = appContext;
      if (!ctx || !dirName) {
        return '';
      }
      const dirPath = `${ctx.cacheDir}/${dirName}`;
      if (!fileIo.accessSync(dirPath)) {
        fileIo.mkdirSync(dirPath, true);
      }
      return dirPath;
    } catch (err) {
      hilog.error(0x0000, TAG, 'createCacheDir failed, dirName=%{public}s, err=%{public}s',
        dirName, JSON.stringify(err) ?? '');
      return '';
    }
  }

  // region 私有辅助方法

  /**
   * 根据 needUser 选择根目录，拼接完整文件路径。
   * - needUser=true：使用 filesDir（应用私有持久化目录，沙箱内按 App 隔离）
   * - needUser=false：使用 cacheDir（应用缓存目录）
   */
  private buildFilePath(fileName: string, needUser: boolean): string {
    const ctx = appContext;
    if (!ctx) {
      return fileName;
    }
    const rootDir = needUser ? ctx.filesDir : ctx.cacheDir;
    return `${rootDir}/${fileName}`;
  }

  /**
   * 如果文件路径是绝对路径则直接返回，否则拼接 filesDir。
   */
  private resolveAbsPath(filePath: string): string {
    if (filePath.startsWith('/')) {
      return filePath;
    }
    const ctx = appContext;
    if (!ctx) {
      return filePath;
    }
    return `${ctx.filesDir}/${filePath}`;
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

  // endregion
}
