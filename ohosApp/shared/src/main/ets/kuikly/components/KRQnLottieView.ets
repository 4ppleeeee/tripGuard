import { KRAny, KuiklyRenderBaseView, KuiklyRenderCallback } from '@kuikly/render';
import { ComponentContent } from '@kit.ArkUI';
import { hilog } from '@kit.PerformanceAnalysisKit';
import lottie, { AnimationItem } from '@ohos/lottie';

const TAG = 'KRQnLottieView';

/**
 * Lottie 数据结构（对应 KMP 侧 QnLottieData 序列化后的 JSON）
 */
interface LottieData {
  name: string;
  status: string;
  aspectRatio: number;
  autoPlay: boolean;
  loop: boolean;
  tag: string;
  tintColor: string;
  tintColorKey: string;
  scaleType: number | null;
  scale: number | null;
  cornerInDp: number | null;
  enableInteraction: boolean;
  progressRange: ProgressRangeData | null;
  textDelegate: Record<string, string> | null;
}

/**
 * 动画播放区间
 */
interface ProgressRangeData {
  startProgress: number;
  endProgress: number;
}

/**
 * Lottie 下载状态（对应 KMP 侧 LottieDownloadStatus）
 * KMP 侧通过 JSON {"DOWNLOAD":"STATUS_NAME"} 格式传递
 */
class LottieDownloadStatus {
  static readonly PENDING = 'PENDING';
  static readonly DOWNLOADING = 'DOWNLOADING';
  static readonly COMPLETED = 'COMPLETED';
  static readonly FAILED = 'FAILED';

  static toJsonString(status: string): string {
    return JSON.stringify({ DOWNLOAD: status });
  }
}

/**
 * 鸿蒙端 Kuikly 自定义 Lottie 组件
 *
 * 对应 commonMain 中的 QnLottieView，viewName 为 "QnLottieView"。
 *
 * 参照 Android 端 AndroidLottieView / WsAndroidLottieView 的属性处理逻辑：
 * - 接收并解析 KMP 侧传递的 data 属性（QnLottieData JSON）
 * - 支持 autoPlay、loop、progress、applyTheme 等属性
 * - 支持 setLottieDownloadStatusListener 回调
 * - 不拦截手势（hitTestBehavior = None）
 *
 * 使用 @ohos/lottie 三方库实现动画播放。
 */
@Observed
export class KRQnLottieView extends KuiklyRenderBaseView {
  static readonly VIEW_NAME = 'QnLottieView';

  lottieData: LottieData | null = null;
  lottieProgress: number = 0;
  isDarkTheme: boolean = false;
  // 数据版本号，每次 lottieData 变化时递增，用于通知 UI 组件重新加载
  dataVersion: number = 0;
  private downloadStatusCallback: KuiklyRenderCallback | null = null;
  private lastLottieName: string = '';
  private lastLottieStatus: string = '';
  // UI 组件（KRQnLottie）注册的回调，用于在属性变更时主动驱动动画
  private reloadCallback: (() => void) | null = null;
  private progressUpdateCallback: ((progress: number) => void) | null = null;

  createArkUIView(): ComponentContent<KuiklyRenderBaseView> {
    const uiContext = this.getUIContext() as UIContext;
    return new ComponentContent<KuiklyRenderBaseView>(
      uiContext,
      wrapBuilder<[KuiklyRenderBaseView]>(createQnLottieView),
      this
    );
  }

  /**
   * 供 UI 组件注册重载回调，属性变更后主动触发重新加载动画。
   */
  registerReloadCallback(cb: (() => void) | null): void {
    this.reloadCallback = cb;
  }

  /**
   * 供 UI 组件注册进度更新回调，setProgress 属性变更时实时驱动动画。
   */
  registerProgressUpdateCallback(cb: ((progress: number) => void) | null): void {
    this.progressUpdateCallback = cb;
  }

  setProp(propKey: string, propValue: KRAny | KuiklyRenderCallback): boolean {
    switch (propKey) {
      case 'data':
        // KMP 鸿蒙端通过 KtJson.safeEncode 将 QnLottieData 序列化为 JSON 字符串
        try {
          let jsonStr = propValue as string;
          let data: LottieData = JSON.parse(jsonStr) as LottieData;

          // 参照 Android 端 AndroidLottieView.setProp("data") 的逻辑：
          // 如果 name 或 status 变化，需要取消上次动画并重置进度
          if (this.lastLottieName !== data.name || this.lastLottieStatus !== data.status) {
            this.lottieProgress = 0;
          }
          this.lastLottieName = data.name;
          this.lastLottieStatus = data.status;

          this.lottieData = data;
          this.dataVersion++;
          hilog.debug(0, TAG, 'setLottieData: name=%{public}s, autoPlay=%{public}s, loop=%{public}s',
            data.name, String(data.autoPlay), String(data.loop));
          // 数据变更后，主动通知 UI 组件重新加载（覆盖组件已挂载但未重新进入 onAppear 的场景）
          if (this.reloadCallback) {
            this.reloadCallback();
          }
        } catch (e) {
          hilog.error(0, TAG, 'parse lottie data failed: %{public}s', String(e));
        }
        return true;
      case 'applyTheme':
        // 主题切换（日夜间模式）
        this.isDarkTheme = propValue as boolean;
        hilog.debug(0, TAG, 'applyTheme: isDark=%{public}s', String(this.isDarkTheme));
        return true;
      case 'setProgress':
        // 设置动画进度（0-1）
        let progress = propValue as number;
        if (!isNaN(progress)) {
          this.lottieProgress = progress;
          hilog.debug(0, TAG, 'setProgress: %{public}s', String(progress));
          // 属性变更时实时驱动动画（若已就绪），覆盖 progress=0 回到首帧的场景
          if (this.progressUpdateCallback) {
            this.progressUpdateCallback(progress);
          }
        }
        return true;
      case 'setLottieDownloadStatusListener':
        this.downloadStatusCallback = propValue as KuiklyRenderCallback;
        return true;
      default:
        return super.setProp(propKey, propValue);
    }
  }

  call(method: string, params: KRAny, callback: KuiklyRenderCallback | null): KRAny {
    return null;
  }

  /**
   * 通知下载状态给 KMP 侧
   */
  notifyDownloadStatus(status: string): void {
    if (this.downloadStatusCallback) {
      this.downloadStatusCallback(LottieDownloadStatus.toJsonString(status));
      hilog.debug(0, TAG, 'notifyDownloadStatus: %{public}s', status);
    }
  }
}

@Component
export struct KRQnLottie {
  @ObjectLink renderView: KRQnLottieView;
  // Canvas 渲染上下文，@ohos/lottie 需要 CanvasRenderingContext2D 作为容器
  private canvasSettings: RenderingContextSettings = new RenderingContextSettings(true);
  private canvasContext: CanvasRenderingContext2D = new CanvasRenderingContext2D(this.canvasSettings);
  private animationItem: AnimationItem | null = null;
  private isCanvasReady: boolean = false;
  // 记录上次加载的数据版本，避免重复加载
  private lastLoadedVersion: number = -1;

  aboutToAppear() {
    // 注册回调：属性变更后主动驱动重载和进度更新
    this.renderView.registerReloadCallback(() => {
      if (this.isCanvasReady) {
        this.loadAndPlayAnimation();
      }
    });
    this.renderView.registerProgressUpdateCallback((progress: number) => {
      // 动画已就绪时立即应用新进度；未就绪时交由 DOMLoaded 阶段统一处理
      if (this.animationItem) {
        this.setProgress(progress);
      }
    });
  }

  aboutToDisappear() {
    this.renderView.registerReloadCallback(null);
    this.renderView.registerProgressUpdateCallback(null);
    this.destroyAnimation();
  }

  /**
   * 加载并播放 Lottie 动画
   */
  private loadAndPlayAnimation(): void {
    const data = this.renderView.lottieData;
    if (!data || !data.name) {
      hilog.warn(0, TAG, 'loadAndPlayAnimation: no lottie data or name');
      return;
    }

    if (!this.isCanvasReady) {
      hilog.warn(0, TAG, 'loadAndPlayAnimation: canvas not ready');
      return;
    }

    // 如果已经加载过相同版本的数据，不重复加载
    if (this.lastLoadedVersion === this.renderView.dataVersion) {
      return;
    }
    this.lastLoadedVersion = this.renderView.dataVersion;

    // 销毁之前的动画
    this.destroyAnimation();

    try {
      let animationSource: string = data.name;
      // 判断是网络资源还是本地资源
      let isNetworkResource: boolean = animationSource.startsWith('http://') || animationSource.startsWith('https://');

      hilog.info(0, TAG, 'loadAndPlayAnimation: source=%{public}s, isNetwork=%{public}s, loop=%{public}s, autoPlay=%{public}s',
        animationSource, String(isNetworkResource), String(data.loop), String(data.autoPlay));

      // 通知 KMP 侧下载中
      this.renderView.notifyDownloadStatus(LottieDownloadStatus.DOWNLOADING);

      // 使用 @ohos/lottie 加载动画
      // container 传入 CanvasRenderingContext2D 实例
      // 关键区分：
      //   - 网络资源（http/https）使用 uri 参数，支持 json 和 zip 格式
      //   - 本地资源使用 path 参数，仅支持 ets 目录下的相对路径 json 文件
      if (isNetworkResource) {
        this.animationItem = lottie.loadAnimation({
          container: this.canvasContext,
          renderer: 'canvas',
          loop: data.loop,
          autoplay: data.autoPlay,
          name: data.tag || data.name,
          uri: animationSource,
        });
      } else {
        this.animationItem = lottie.loadAnimation({
          container: this.canvasContext,
          renderer: 'canvas',
          loop: data.loop,
          autoplay: data.autoPlay,
          name: data.tag || data.name,
          path: animationSource,
        });
      }

      // 监听动画事件
      if (this.animationItem) {
        this.animationItem.addEventListener('DOMLoaded', () => {
          hilog.debug(0, TAG, 'lottie animation DOMLoaded (data ready)');
          this.renderView.notifyDownloadStatus(LottieDownloadStatus.COMPLETED);

          // 应用播放区间（progressRange）：对齐 Android setProgressRange 行为
          this.applyProgressRange(data);

          // 应用文本替换（textDelegate）：对齐 Android setTextDelegate 行为
          this.applyTextDelegate(data);

          // 如果设置了进度，在数据加载完成后应用。
          // 条件覆盖 0（>= 0），支持下拉刷新等 progress=0 回到首帧的场景。
          if (this.renderView.lottieProgress >= 0 && !data.autoPlay) {
            this.setProgress(this.renderView.lottieProgress);
          }
        });

        this.animationItem.addEventListener('complete', () => {
          hilog.debug(0, TAG, 'lottie animation complete');
        });

        this.animationItem.addEventListener('loopComplete', () => {
          hilog.debug(0, TAG, 'lottie animation loopComplete');
        });

        this.animationItem.addEventListener('error', () => {
          hilog.error(0, TAG, 'lottie animation error');
          this.renderView.notifyDownloadStatus(LottieDownloadStatus.FAILED);
        });
      }

      hilog.info(0, TAG, 'loadAndPlayAnimation: lottie.loadAnimation called');
    } catch (e) {
      hilog.error(0, TAG, 'loadAndPlayAnimation failed: %{public}s', String(e));
      this.renderView.notifyDownloadStatus(LottieDownloadStatus.FAILED);
    }
  }

  /**
   * 应用 Lottie 播放区间（progressRange），对齐 Android 端 setProgressRange。
   */
  private applyProgressRange(data: LottieData): void {
    if (!this.animationItem) {
      return;
    }
    const range = data.progressRange;
    if (!range) {
      return;
    }
    try {
      const totalFrames: number = (this.animationItem.totalFrames ?? 0) as number;
      if (totalFrames <= 0) {
        return;
      }
      const startFrame: number = Math.max(0, Math.floor(totalFrames * range.startProgress));
      const endFrame: number = Math.min(totalFrames, Math.ceil(totalFrames * range.endProgress));
      if (endFrame <= startFrame) {
        return;
      }
      // playSegments([start, end], forceFlag=true) 立即生效
      this.animationItem.playSegments([startFrame, endFrame], true);
      hilog.debug(0, TAG, 'applyProgressRange: [%{public}s,%{public}s] frames=[%{public}s,%{public}s]',
        String(range.startProgress), String(range.endProgress), String(startFrame), String(endFrame));
    } catch (e) {
      hilog.error(0, TAG, 'applyProgressRange failed: %{public}s', String(e));
    }
  }

  /**
   * 应用 Lottie 文本替换（textDelegate），对齐 Android 端 setTextDelegate。
   * 通过遍历动画渲染元素，对 text 类型图层执行 updateDocumentData 替换 t 字段。
   */
  private applyTextDelegate(data: LottieData): void {
    if (!this.animationItem) {
      return;
    }
    const delegate = data.textDelegate;
    if (!delegate) {
      return;
    }
    try {
      // @ohos/lottie 基于 lottie-web，文本图层可通过 renderer.elements[i].updateDocumentData 修改
      const renderer: ESObject = (this.animationItem as ESObject).renderer;
      const elements: ESObject = renderer ? renderer.elements : null;
      if (!elements || !elements.length) {
        return;
      }
      for (let i = 0; i < elements.length; i++) {
        const element: ESObject = elements[i];
        if (!element || typeof element.updateDocumentData !== 'function') {
          continue;
        }
        // 元素的 data.nm 为图层名称；若名称命中 delegate key，则替换文本
        const layerName: string = element.data ? String(element.data.nm ?? '') : '';
        const newText: string | undefined = delegate[layerName];
        if (newText !== undefined && newText !== null) {
          // 第二个参数为 keyframe index，固定传 0
          element.updateDocumentData({ t: newText }, 0);
          hilog.debug(0, TAG, 'applyTextDelegate: layer=%{public}s -> %{public}s', layerName, newText);
        }
      }
    } catch (e) {
      hilog.error(0, TAG, 'applyTextDelegate failed: %{public}s', String(e));
    }
  }

  /**
   * 设置动画进度
   * @param progress 进度值 0-1
   */
  private setProgress(progress: number): void {
    if (this.animationItem && progress >= 0 && progress <= 1) {
      // @ohos/lottie goToAndStop 第一个参数是帧数或百分比
      // 使用 true 表示按百分比（0-100）
      this.animationItem.goToAndStop(progress * 100, true);
      hilog.debug(0, TAG, 'setProgress: %{public}s', String(progress));
    }
  }

  /**
   * 销毁动画，释放资源
   */
  private destroyAnimation(): void {
    if (this.animationItem) {
      this.animationItem.destroy();
      this.animationItem = null;
      hilog.debug(0, TAG, 'destroyAnimation');
    }
  }

  build() {
    Stack() {
      Canvas(this.canvasContext)
        .width('100%')
        .height('100%')
        .onReady(() => {
          this.isCanvasReady = true;
          hilog.debug(0, TAG, 'Canvas onReady');
          // Canvas 就绪后加载动画
          this.loadAndPlayAnimation();
        })
    }
    .width('100%')
    .height('100%')
    .onAppear(() => {
      // 如果 Canvas 已就绪且数据已更新，重新加载
      if (this.isCanvasReady && this.renderView.lottieData) {
        this.loadAndPlayAnimation();
      }
    })
    .onDisAppear(() => {
      // 注意：不把 isCanvasReady 置为 false。
      // 原因：Canvas.onReady 只在组件构建时触发一次，组件仅隐藏（非重建）时不会再次触发。
      // 如果这里重置为 false，再次可见时会因 isCanvasReady 判空而无法恢复动画。
      this.destroyAnimation();
      // 数据版本重置，便于重显时强制重载
      this.lastLoadedVersion = -1;
    })
    // 不拦截手势，避免影响列表滑动等交互
    .hitTestBehavior(HitTestMode.None)
  }
}

@Builder
function createQnLottieView(view: KuiklyRenderBaseView) {
  KRQnLottie({ renderView: view as KRQnLottieView })
}