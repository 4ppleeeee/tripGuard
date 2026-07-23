import { common } from '@kit.AbilityKit';
import {
  Bugly,
  BuglyBuilder,
  LogLevel,
  LogUploadListener,
  TDDiag,
  TDDiagBuilder,
  TDDiagUploadParams,
  TDLog,
  TDLogBuilder,
  TDLogMode,
} from 'bugly';
import deviceInfo from '@ohos.deviceInfo';
import fs from '@ohos.file.fs';
import { QimeiManager } from './QimeiManager';
import { hilog } from '@kit.PerformanceAnalysisKit';

const TAG = 'BuglyManager';

/**
 * Bugly 日志加密公钥（对应腾讯新闻的 BUGLY_LOG_PUB_KEY）。
 * 当前 demo 未配置加密公钥，保持为空串表示不启用日志加密；
 * 如业务侧后续接入日志加密，可在平台侧生成公钥后回填此处。
 */
const BUGLY_LOG_PUB_KEY = '';

/**
 * 主业务日志子通道前缀（namePrefix）。
 * 对应 Kotlin 侧 `QnPlatformLog.fileLog`，通过 `TDLogPrintLog(null, ...)` 命中。
 * 日志文件名形如 `business_{date}.xlog.log`。
 */
const SUB_LOG_PREFIX_BUSINESS = 'business';

/**
 * 视频日志子通道前缀。
 * - Kotlin 侧 `QnPlatformLog.videoFileLog` 会通过 `TDLogPrintLog("video", ...)` 写入。
 * - 日志文件名形如 `video_{date}.xlog.log`。
 */
const SUB_LOG_PREFIX_VIDEO = 'video';

/**
 * Compose 重组分析日志子通道前缀。
 * - Kotlin 侧 `QnPlatformLog.rcProfilerFileLog` 会通过 `TDLogPrintLog("rcprofiler", ...)` 写入。
 * - 日志文件名形如 `rcprofiler_{date}.xlog.log`。
 */
const SUB_LOG_PREFIX_RC_PROFILER = 'rcprofiler';

/**
 * 需要额外开启的子日志实例（主 `business` 实例由 `TDLog.init` 负责，不在此列表）。
 * 集中维护，便于后续扩展与收集上传。
 */
const SUB_LOG_PREFIXES: string[] = [SUB_LOG_PREFIX_VIDEO, SUB_LOG_PREFIX_RC_PROFILER];

/**
 * Harmony Bugly 初始化管理
 * 参照腾讯新闻鸿蒙端 BuglyManager 实现，集成 TDDiag 日志上传能力
 */
export class BuglyManager {
  private static instance: BuglyManager;
  private appContext?: common.ApplicationContext;
  private appId: string = '';
  private appKey: string = '';
  private appVersion: string = '';
  /**
   * 所有 TDLog 实例共用的根目录（打平结构）：
   *
   * ```
   * {rootLogDir}/
   *   ├── business_{date}.xlog.log
   *   ├── business_{date}.mmap3
   *   ├── video_{date}.xlog.log
   *   ├── video_{date}.mmap3
   *   ├── rcprofiler_{date}.xlog.log
   *   └── rcprofiler_{date}.mmap3
   * ```
   *
   * 三通道共用同一 `logDir`，靠 `namePrefix` 做文件名区分（xlog 内部会以
   * `{namePrefix}_*` 独立轮转，不会互相覆盖）。
   * 上传时直接扫描该目录下所有常规文件即可，无需再做子目录打包。
   */
  private rootLogDir: string = '';

  private constructor() {
  }

  static getInstance(): BuglyManager {
    if (!BuglyManager.instance) {
      BuglyManager.instance = new BuglyManager();
    }
    return BuglyManager.instance;
  }

  /**
   * 当前 TDLog 的根目录（打平结构，三通道共用）。
   * 供日志分享模块直接枚举里面的 xlog 文件打包后分享到微信。
   * 初始化前或初始化失败时返回空串。
   */
  getRootLogDir(): string {
    return this.rootLogDir;
  }

  /** 供分享模块拿到 `filesDir` / `cacheDir`，以及微信 SDK 所需的 UIAbilityContext（上层提供）。 */
  getAppContext(): common.ApplicationContext | undefined {
    return this.appContext;
  }

  initBugly(
    context: common.ApplicationContext,
    appId: string,
    appKey: string,
    qimeiAppKey: string,
    qimeiChannelId: string,
    appVersion: string,
    buildNumber: string,
    userId: string,
    appChannel: string,
    isDebug: boolean,
    callback: Function,
  ): void {
    this.appContext = context;
    this.appId = appId;
    this.appKey = appKey;
    this.appVersion = appVersion;

    // 必须先初始化本地日志通道 (TDLog)，否则 TDLog.getTDLogAdapter() 返回 null，
    // 后续 initBuglyLogReport 拿不到 loggerAdapter，uploadLogToBugly 也会失败。
    this.initBuglyLog(context, isDebug);

    let builder = new BuglyBuilder();
    builder.appId = appId;
    builder.appKey = appKey;
    builder.platform = BuglyBuilder.PLATFORM_OA;
    builder.appVersion = appVersion;
    builder.buildNum = buildNumber;
    builder.appChannel = appChannel;
    builder.userId = userId;
    builder.debugMode = isDebug;
    builder.initDelay = 0;

    Bugly.init(context, builder)
      .then(() => {
        Bugly.updateDeviceModel(deviceInfo.productModel);
        // 初始化 TDDiag 日志上传模块
        this.initBuglyLogReport(context, appVersion);
        // Qimei SDK 与 Bugly 解耦，尽量回填设备 ID
        if (qimeiAppKey && qimeiChannelId) {
          QimeiManager.getInstance().initQimei(
            context,
            qimeiAppKey,
            qimeiChannelId,
            isDebug,
            (qimei36: string, _: string) => {
              if (qimei36 && qimei36.length > 0) {
                Bugly.updateDeviceId(qimei36);
                TDDiag.setGuid(qimei36);
              }
            },
          );
        }
        callback();
      })
      .catch((error: Error) => {
        // 启动链路兜底：初始化失败也需要回调，避免 startup 任务悬挂。
        hilog.error(0x0000, TAG, `Bugly initialization failed: ${JSON.stringify(error)}`);
        callback();
      });
  }

  /**
   * 初始化 TDLog 本地日志通道（打平方案）。
   *
   * 三个通道共用同一根目录 `{filesDir}/logs/`，靠 `namePrefix` 做文件名区分：
   * - 主实例   `namePrefix=business`   → `business_{date}.xlog.log`
   * - 子实例 1 `namePrefix=video`      → `video_{date}.xlog.log`
   * - 子实例 2 `namePrefix=rcprofiler` → `rcprofiler_{date}.xlog.log`
   *
   * 只有调用过 `TDLog.init` 之后，`TDLog.getTDLogAdapter()` 才会返回非 null 的
   * 适配器，TDDiag 才能感知到本地日志路径。
   */
  private initBuglyLog(context: common.ApplicationContext, isDebug: boolean): void {
    try {
      const rootLogDir = `${context.filesDir}/logs`;
      this.rootLogDir = rootLogDir;
      const logBuilder = new TDLogBuilder();
      // 主实例直接落在根目录，通过 namePrefix 与子实例文件区分。
      logBuilder.logDir = rootLogDir;
      logBuilder.namePrefix = SUB_LOG_PREFIX_BUSINESS;
      logBuilder.consoleLog = isDebug;
      logBuilder.logLevel = LogLevel.LEVEL_ALL;
      // Release 必须使用异步模式，Debug 也可异步避免卡顿。
      logBuilder.mode = TDLogMode.Async;
      logBuilder.pubKey = BUGLY_LOG_PUB_KEY;
      logBuilder.maxFileSize = 50 * 1024 * 1024; // 单文件 50M
      logBuilder.maxAliveTime = 7 * 24 * 60 * 60; // 保存 7 天
      TDLog.init(logBuilder);
      // 额外打开每个子实例，使得 Kotlin 侧 TDLogPrintLog(namePrefix, ...) 可以命中。
      this.openSubLogInstances(rootLogDir, isDebug);
      hilog.info(0x0000, TAG, `TDLog initialized, rootLogDir=${rootLogDir} (flat layout)`);
    } catch (error) {
      hilog.error(0x0000, TAG, `TDLog initialization failed: ${JSON.stringify(error)}`);
    }
  }

  /**
   * 打开 TDLog 子实例（video / rcprofiler）。
   * 所有子实例与主实例共用同一 `logDir`，靠 `namePrefix` 做文件名区分；
   * xlog 内部会以 `{namePrefix}_*` 独立维护文件轮转，不会相互覆盖。
   */
  private openSubLogInstances(rootLogDir: string, isDebug: boolean): void {
    SUB_LOG_PREFIXES.forEach((prefix: string) => {
      try {
        // 子实例已存在时跳过，避免重复初始化。
        if (TDLog.getSubLogInstance(prefix)) {
          return;
        }
        const subInstance = TDLog.openSubLogInstance(
          LogLevel.LEVEL_ALL,
          TDLogMode.Async,
          rootLogDir,
          prefix,
        );
        if (!subInstance) {
          hilog.error(0x0000, TAG, `openSubLogInstance returned null, prefix=${prefix}`);
          return;
        }
        subInstance.setConsoleLogOpen(isDebug);
        hilog.info(
          0x0000,
          TAG,
          `TDLog sub instance opened, prefix=${prefix}, dir=${rootLogDir} (flat)`,
        );
      } catch (error) {
        hilog.error(
          0x0000,
          TAG,
          `openSubLogInstance failed, prefix=${prefix}, err=${JSON.stringify(error)}`,
        );
      }
    });
  }

  /**
   * 初始化 TDDiag 日志诊断上传模块
   * 参照腾讯新闻 initBuglyLogReport 实现
   */
  private initBuglyLogReport(context: common.ApplicationContext, versionName: string): void {
    try {
      const loggerAdapter = TDLog.getTDLogAdapter();
      if (!loggerAdapter) {
        hilog.error(
          0x0000,
          TAG,
          'initBuglyLogReport skipped: TDLog.getTDLogAdapter() is null, please check initBuglyLog.',
        );
        return;
      }
      const diagBuilder = new TDDiagBuilder();
      diagBuilder.appId = this.appId;
      diagBuilder.appKey = this.appKey;
      diagBuilder.platform = TDDiagBuilder.PLATFORM_OA;
      diagBuilder.appVersion = versionName;
      diagBuilder.loggerAdapter = loggerAdapter;
      diagBuilder.logUploadListener = new BuglyLogUploadListener();
      TDDiag.init(context, diagBuilder);
      hilog.info(0x0000, TAG, 'TDDiag initialized successfully.');
    } catch (error) {
      hilog.error(0x0000, TAG, `TDDiag initialization failed: ${JSON.stringify(error)}`);
    }
  }

  reportBugly(name: string, reason: string, stack: string): void {
    if (!this.appContext) {
      hilog.error(0x0000, TAG, 'reportBugly called before init.');
      return;
    }
    try {
      Bugly.postCustomError(name, reason, stack, true, this.appContext);
    } catch (error) {
      hilog.error(0x0000, TAG, `reportBugly failed: ${JSON.stringify(error)}`);
    }
  }

  /**
   * 通过 TDDiag 上传日志文件到 Bugly 平台
   * 参照腾讯新闻 uploadBuglyLog 实现
   *
   * 打平方案下的上传流程：
   * 1. 三个通道的文件都在 `rootLogDir` 下，靠文件名前缀（business_/video_/rcprofiler_）区分。
   * 2. 先用 `loggerAdapter.getLogPaths()` 拿到 SDK 自己登记的路径，再扫描一次
   *    `rootLogDir` 兜底（防止 adapter 未列出某些轮转文件）。
   * 3. 去重后塞进 `TDDiagUploadParams.files`，由 TDDiag 内部打 zip 上传；
   *    无需再手工拷贝/打包/清理临时目录。
   * 4. xlog 文件本身是二进制格式，解压后需用 mars-xlog 的
   *    `decode_mars_nocrypt_log_file.py` 脚本解码得到明文。
   */
  uploadLogToBugly(): void {
    if (!this.appContext) {
      hilog.error(0x0000, TAG, 'uploadLogToBugly called before init.');
      return;
    }
    try {
      const loggerAdapter = TDLog.getTDLogAdapter();
      if (!loggerAdapter) {
        hilog.error(
          0x0000,
          TAG,
          'uploadLogToBugly failed: loggerAdapter is null, TDLog is not initialized (call initBuglyLog first).',
        );
        return;
      }

      // 1) SDK 登记的路径。
      const paths = new Set<string>();
      loggerAdapter.getLogPaths().forEach((path: string) => {
        if (path && path.length > 0) {
          paths.add(path);
        }
      });
      // 2) 扫描根目录兜底收集所有 xlog 相关文件。
      this.collectAllLogFiles(paths);

      if (paths.size === 0) {
        hilog.error(0x0000, TAG, 'uploadLogToBugly failed: log paths are empty.');
        return;
      }

      const params = new TDDiagUploadParams();
      params.tag = 'KmmLogUpload';
      params.summary = 'KMM日志主动上传';
      params.files = Array.from(paths);
      TDDiag.uploadLogFiles(params);
      hilog.info(
        0x0000,
        TAG,
        `已触发TDDiag日志上传, count=${params.files.length}, files=${JSON.stringify(params.files)}`,
      );
    } catch (error) {
      hilog.error(0x0000, TAG, `uploadLogToBugly failed: ${JSON.stringify(error)}`);
    }
  }

  /**
   * 扫描 `rootLogDir` 下所有常规文件，追加到 `pathsOut`（去重）。
   * 打平布局下，三个通道的文件共享同一目录，直接一次遍历即可覆盖：
   * - `business_*.xlog.log` / `business_*.mmap3`
   * - `video_*.xlog.log`    / `video_*.mmap3`
   * - `rcprofiler_*.xlog.log` / `rcprofiler_*.mmap3`
   */
  private collectAllLogFiles(pathsOut: Set<string>): void {
    if (!this.rootLogDir) {
      return;
    }
    try {
      if (!fs.accessSync(this.rootLogDir)) {
        return;
      }
      const files = fs.listFileSync(this.rootLogDir);
      files.forEach((name: string) => {
        const fullPath = `${this.rootLogDir}/${name}`;
        try {
          const stat = fs.statSync(fullPath);
          if (stat.isFile()) {
            pathsOut.add(fullPath);
          }
        } catch (err) {
          hilog.warn(
            0x0000,
            TAG,
            `stat failed, path=${fullPath}, err=${JSON.stringify(err)}`,
          );
        }
      });
    } catch (error) {
      hilog.warn(
        0x0000,
        TAG,
        `collectAllLogFiles failed, dir=${this.rootLogDir}, err=${JSON.stringify(error)}`,
      );
    }
  }
}

/**
 * 日志上传回调：仅打印结果。
 * 打平方案下上传流程没有临时产物（zip/staging），无需额外清理。
 */
class BuglyLogUploadListener implements LogUploadListener {
  onSuccess(): void {
    hilog.info(0x0000, TAG, 'Upload Log Success');
  }

  onFailure(): void {
    hilog.error(0x0000, TAG, 'Upload Log Failure');
  }
}
