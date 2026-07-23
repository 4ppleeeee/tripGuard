import { KuiklyNativeManager } from '@kuikly/render';
import Napi from 'libkmm_shared.so';

/**
 * KMM Kuikly NativeManager。
 *
 * native bridge 由 shared HAR 内部加载（libkmm_shared.so），
 * entry 模块通过 shared 暴露的 globalNativeManager 间接调用，
 * 避免 entry 默认 napi 模块名 'entry' 与本桥接 so 冲突。
 */
class WsNativeManager extends KuiklyNativeManager {
  protected loadNative(): number {
    return Napi.initKuikly();
  }
}

const globalNativeManager = new WsNativeManager();

export default globalNativeManager;
