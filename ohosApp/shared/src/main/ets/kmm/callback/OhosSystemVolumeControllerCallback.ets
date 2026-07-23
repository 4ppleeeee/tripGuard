import { audio } from '@kit.AudioKit';

/**
 * 鸿蒙端 SystemVolumeController 回调实现。
 * 对应 KMP 侧 @KNCallback 接口 OhosSystemVolumeController。
 *
 * 使用 @kit.AudioKit 的 AudioVolumeManager 订阅媒体音量变化：
 *  - 只监听 STREAM_USAGE_MEDIA（媒体流）的音量，与腾讯新闻/视频业务一致；
 *  - 回调只回传"当前音量值"，isUp（音量升/降）由 KMP 侧对比前值计算。
 *
 * 【ArkTS strict 模式下的关键点】
 *  - AudioVolumeManager.on('volumeChange', cb) 回调参数类型为 VolumeEvent；
 *  - 通过 audio.getAudioManager().getVolumeManager() 获取 AudioVolumeManager 实例；
 *  - on/off 必须用同一个函数引用，因此 currentCallback 要用成员字段持有。
 */
export class OhosSystemVolumeControllerCallback {

  private currentCallback: ((event: audio.VolumeEvent) => void) | null = null;

  /**
   * 开始监听系统媒体音量变化。
   * @param onVolumeChanged 音量变化回调，参数为当前音量值（整数值）
   */
  startListening(onVolumeChanged: (volume: number) => void): void {
    // 幂等：如已注册，先停掉老的再注册
    this.stopListening();
    try {
      const volumeManager = audio.getAudioManager().getVolumeManager();
      const callback: (event: audio.VolumeEvent) => void = (event: audio.VolumeEvent): void => {
        try {
          // 只关心媒体流音量变化，其他流（铃声/通知）忽略
          if (event.volumeType === audio.AudioVolumeType.MEDIA) {
            onVolumeChanged(event.volume);
          }
        } catch (cbErr) {
          console.error(`[OhosSystemVolumeController] onVolumeChanged error: ${cbErr}`);
        }
      };
      this.currentCallback = callback;
      volumeManager.on('volumeChange', callback);
    } catch (e) {
      console.error(`[OhosSystemVolumeController] startListening error: ${e}`);
      this.currentCallback = null;
    }
  }

  /** 停止监听系统媒体音量变化 */
  stopListening(): void {
    if (!this.currentCallback) {
      return;
    }
    try {
      const volumeManager = audio.getAudioManager().getVolumeManager();
      volumeManager.off('volumeChange', this.currentCallback);
    } catch (e) {
      console.error(`[OhosSystemVolumeController] stopListening error: ${e}`);
    } finally {
      this.currentCallback = null;
    }
  }
}
