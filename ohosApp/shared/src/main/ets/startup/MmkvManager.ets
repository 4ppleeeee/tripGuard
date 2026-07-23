import { MMKV } from '@tencent/mmkv';
import { common } from '@kit.AbilityKit';

/**
 * MMKV manager for initialization.
 *
 * 只负责调用 MMKV.initialize(context)，让 ArkTS 侧 @tencent/mmkv 的 libmmkv.so
 * 设置好 g_rootDir（`${context.filesDir}/mmkv`）。
 * KMM 侧 mmkvKotlin 走默认 rootPath 的 mmkvWithID，与 ArkTS 共享同一套底层文件。
 */
export class MmkvManager {
  private static instance: MmkvManager;

  private constructor() {
  }

  public static getInstance(): MmkvManager {
    if (!MmkvManager.instance) {
      MmkvManager.instance = new MmkvManager();
    }
    return MmkvManager.instance;
  }

  /**
   * Initializes MMKV with the application context.
   * @param context application context
   * @param callback invoked after initialization completes
   */
  public initMmkv(context: common.ApplicationContext, callback: Function): void {
    MMKV.initialize(context);
    callback();
  }
}
