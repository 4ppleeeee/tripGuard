import { KRAny, KuiklyRenderBaseView, KuiklyRenderCallback } from '@kuikly/render';
import { ComponentContent } from '@kit.ArkUI';

const CONTENT_PROP = 'content';
const FOREGROUND_COLOR_PROP = 'foregroundColor';
const BACKGROUND_COLOR_PROP = 'backgroundColor';

@Observed
export class KRQnQrCodeView extends KuiklyRenderBaseView {
  static readonly VIEW_NAME = 'QnQrCodeView';
  content: string = '';
  foregroundColor: ResourceColor = '#000000';
  qrBackgroundColor: ResourceColor = '#FFFFFF';

  createArkUIView(): ComponentContent<KuiklyRenderBaseView> {
    const uiContext = this.getUIContext() as UIContext;
    return new ComponentContent<KuiklyRenderBaseView>(
      uiContext,
      wrapBuilder<[KuiklyRenderBaseView]>(createQnQrCodeView),
      this
    );
  }

  setProp(propKey: string, propValue: KRAny | KuiklyRenderCallback): boolean {
    switch (propKey) {
      case CONTENT_PROP:
        this.content = this.toStringValue(propValue);
        return true;
      case FOREGROUND_COLOR_PROP:
        this.foregroundColor = this.toColorValue(propValue, '#000000');
        return true;
      case BACKGROUND_COLOR_PROP:
        this.qrBackgroundColor = this.toColorValue(propValue, '#FFFFFF');
        return true;
      default:
        return super.setProp(propKey, propValue);
    }
  }

  call(method: string, params: KRAny, callback: KuiklyRenderCallback | null): KRAny {
    return null;
  }

  private toStringValue(value: KRAny | KuiklyRenderCallback): string {
    return typeof value === 'string' ? value : `${value ?? ''}`;
  }

  private toColorValue(value: KRAny | KuiklyRenderCallback, defaultColor: ResourceColor): ResourceColor {
    if (typeof value !== 'string' || value.length === 0) {
      return defaultColor;
    }
    return value as ResourceColor;
  }
}

@Component
export struct KRQnQrCode {
  @ObjectLink renderView: KRQnQrCodeView;

  build() {
    Stack() {
      if (this.renderView.content.length > 0) {
        QRCode(this.renderView.content)
          .width('100%')
          .height('100%')
          .color(this.renderView.foregroundColor)
          .backgroundColor(this.renderView.qrBackgroundColor)
      } else {
        Stack()
          .width('100%')
          .height('100%')
          .backgroundColor(this.renderView.qrBackgroundColor)
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor(this.renderView.qrBackgroundColor)
    .hitTestBehavior(HitTestMode.None)
  }
}

@Builder
function createQnQrCodeView(view: KuiklyRenderBaseView) {
  KRQnQrCode({ renderView: view as KRQnQrCodeView })
}
