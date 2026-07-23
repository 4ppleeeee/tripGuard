import { KRAny, KuiklyRenderBaseView, KuiklyRenderCallback } from '@kuikly/render';
import { ComponentContent, componentSnapshot } from '@kit.ArkUI';
import { image } from '@kit.ImageKit';
import { fileIo } from '@kit.CoreFileKit';

/**
 * 鸿蒙端 Kuikly 自定义截图组件
 *
 * 对应 commonMain 中的 QnScreenshotView，viewName 为 "QnScreenshotView"。
 * 该组件是一个透明容器，完全不拦截手势（hitTestBehavior = None + touchable(false)），
 * 子视图可以正常接收触摸事件（滑动、点击等）。
 * 支持 call("take") 截图能力，将当前视图内容保存为图片并回调文件路径。
 *
 * 参照 Android 端 AndroidScreenshotView（FrameLayout + IKuiklyRenderViewExport）
 * 和 iOS 端 QnScreenshotView（UIView + KuiklyRenderViewExportProtocol）实现。
 */
@Observed
export class KRQnScreenshotView extends KuiklyRenderBaseView {
  static readonly VIEW_NAME = 'QnScreenshotView';
  // 用于截图的组件 ID
  screenshotId: string = 'qn_screenshot_' + Date.now().toString();

  createArkUIView(): ComponentContent<KuiklyRenderBaseView> {
    const uiContext = this.getUIContext() as UIContext;
    return new ComponentContent<KuiklyRenderBaseView>(
      uiContext,
      wrapBuilder<[KuiklyRenderBaseView]>(createQnScreenshotView),
      this
    );
  }

  setProp(propKey: string, propValue: KRAny | KuiklyRenderCallback): boolean {
    switch (propKey) {
      default:
        return super.setProp(propKey, propValue);
    }
  }

  call(method: string, params: KRAny, callback: KuiklyRenderCallback | null): KRAny {
    if (method === 'take') {
      this.takeScreenshot(callback);
    }
    return null;
  }

  private takeScreenshot(callback: KuiklyRenderCallback | null): void {
    if (!callback) {
      return;
    }
    try {
      // 使用鸿蒙 componentSnapshot API 截取当前组件
      componentSnapshot.get(this.screenshotId, (error: Error, pixelMap: image.PixelMap) => {
        if (error || !pixelMap) {
          callback(JSON.stringify({ path: '' }));
          return;
        }
        // 将 PixelMap 保存为 PNG 文件
        let context: Context = getContext() as Context;
        let filePath: string = context.cacheDir + '/screenshot_' + Date.now().toString() + '.png';
        try {
          let file = fileIo.openSync(filePath, fileIo.OpenMode.CREATE | fileIo.OpenMode.READ_WRITE);
          let packer = image.createImagePacker();
          let packOpts: image.PackingOption = { format: 'image/png', quality: 100 };
          packer.packToFile(pixelMap, file.fd, packOpts).then(() => {
            fileIo.closeSync(file);
            pixelMap.release();
            callback(JSON.stringify({ path: filePath }));
          }).catch(() => {
            fileIo.closeSync(file);
            pixelMap.release();
            callback(JSON.stringify({ path: '' }));
          });
        } catch (e) {
          pixelMap.release();
          callback(JSON.stringify({ path: '' }));
        }
      });
    } catch (e) {
      callback(JSON.stringify({ path: '' }));
    }
  }
}

@Component
export struct KRQnScreenshot {
  @ObjectLink renderView: KRQnScreenshotView;

  build() {
    Stack() {
    }
    .id(this.renderView.screenshotId)
    .width('100%')
    .height('100%')
    .hitTestBehavior(HitTestMode.None)
  }
}

@Builder
function createQnScreenshotView(view: KuiklyRenderBaseView) {
  KRQnScreenshot({ renderView: view as KRQnScreenshotView })
}
