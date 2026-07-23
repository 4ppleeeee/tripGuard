import {
  KRAny, KuiklyRenderBaseView, KuiklyRenderCallback,
} from '@kuikly/render';
import { ComponentContent } from '@kit.ArkUI';
import json from '@ohos.util.json';
import { hilog } from '@kit.PerformanceAnalysisKit';
import { util } from '@kit.ArkTS';

// Event keys（与 DSL 层 WSVideoEvent 保持一致）
const EVENT_ON_SURFACE_CREATED = 'onSurfaceCreated';
const EVENT_ON_SURFACE_DESTROYED = 'onSurfaceDestroyed';

/**
 * 鸿蒙端 Kuikly 自定义视频播放组件
 *
 * 职责：包装一个 XComponent(SURFACE) 提供渲染表面，
 * 在 surface 创建成功时通过 Kuikly 事件回调将 surfaceId 通知 DSL 层。
 * DSL 层拿到 surfaceId 后通过 WSVideoPlayer.playVideo(surfaceId, videoInfo)
 * 将其传递给 ThumbPlayer NAPI（鸿蒙端 setSurface 接受 surfaceId）。
 */
@Observed
export class KRWSVideoView extends KuiklyRenderBaseView {
  static readonly VIEW_NAME = 'KRWSVideoView';
  xComponentController: XComponentController = new XComponentController();
  surfaceId: string = '';
  private onSurfaceCreatedCallback: KuiklyRenderCallback | null = null;
  private onSurfaceDestroyedCallback: KuiklyRenderCallback | null = null;

  createArkUIView(): ComponentContent<KuiklyRenderBaseView> {
    const uiContext = this.getUIContext() as UIContext;
    return new ComponentContent<KuiklyRenderBaseView>(
      uiContext,
      wrapBuilder<[KuiklyRenderBaseView]>(createWSVideoView),
      this
    );
  }

  setProp(propKey: string, propValue: KRAny | KuiklyRenderCallback): boolean {
    switch (propKey) {
      case EVENT_ON_SURFACE_CREATED:
        this.onSurfaceCreatedCallback = propValue as KuiklyRenderCallback;
        // 如果 surface 已经创建了，立即回调
        if (this.surfaceId.length > 0) {
          this.onSurfaceCreatedCallback(this.surfaceId);
        }
        return true;
      case EVENT_ON_SURFACE_DESTROYED:
        this.onSurfaceDestroyedCallback = propValue as KuiklyRenderCallback;
        return true;
      default:
        return super.setProp(propKey, propValue);
    }
  }

  call(method: string, params: KRAny, callback: KuiklyRenderCallback | null): KRAny {
    return null;
  }

  /** XComponent 加载完成时调用 */
  onXComponentLoaded() {
    let surfaceId = this.xComponentController.getXComponentSurfaceId();
    hilog.info(1, "WSVideoView", "onSurfaceCreated: " + surfaceId)
    let record: Record<string, string> = {
      'surfaceId': surfaceId
    }
    this.surfaceId = json.stringify(record)
    if (this.onSurfaceCreatedCallback) {
      this.onSurfaceCreatedCallback(this.surfaceId);
    }
  }

  /** XComponent 销毁时调用 */
  onXComponentDestroyed() {
    if (this.onSurfaceDestroyedCallback) {
      this.onSurfaceDestroyedCallback(this.surfaceId);
    }
    this.surfaceId = '';
  }
}

@Component
export struct KRWSVideo {
  @ObjectLink renderView: KRWSVideoView;

  build() {
    Stack() {
      XComponent({
        id: 'ws_video_surface_' + util.generateRandomUUID(true),
        type: XComponentType.SURFACE,
        controller: this.renderView.xComponentController,
        libraryname: 'ThumbPlayer'
      })
        .onLoad((xComponentContext?: object | Record<string, () => void>) => {
          this.renderView.onXComponentLoaded();
        })
        .onDestroy(() => {
          this.renderView.onXComponentDestroyed();
        })
        .width('100%')
        .height('100%')
    }
    .width('100%')
    .height('100%')
    .backgroundColor(Color.Red)
  }
}

@Builder
function createWSVideoView(view: KuiklyRenderBaseView) {
  KRWSVideo({ renderView: view as KRWSVideoView })
}
