import {
  ErrorReason,
  KRAny,
  KRRecord,
  Kuikly,
  KuiklyRenderBaseView,
  KuiklyRenderCallback,
} from '@kuikly/render';
import { ComponentContent } from '@kit.ArkUI';
import { hilog } from '@kit.PerformanceAnalysisKit';
import { KuiklyViewDelegate } from '../KuiklyViewDelegate';
import globalNativeManager from '../WsNativeManager';

// 与 DSL 侧 KuiklyPageViewAttr 中的属性名一致
const PROP_PAGE_NAME = 'pageName';
const PROP_PAGE_DATA = 'pageData';
const TAG = 'KuiklyPageView';

/**
 * 鸿蒙端 KuiklyPageView 自定义组件
 *
 * 与 Android 端 `KuiklyPageView`（FrameLayout）等价：在外层 Kuikly Pager 中
 * 以 View 粒度承载一个独立的子 Pager。内部使用 `Kuikly()` 组件加载由
 * DSL 侧通过 pageName / pageData 指定的子页面。
 */
@Observed
export class KRKuiklyPageView extends KuiklyRenderBaseView {
  static readonly VIEW_NAME = 'KuiklyPageView';

  pageName: string = '';
  pageData: KRRecord = {};
  /** 子 Pager 自己的 delegate；与外层 Pager 隔离 */
  readonly innerDelegate: KuiklyViewDelegate = new KuiklyViewDelegate();
  /** 错误信息 */
  exception: string = '';

  createArkUIView(): ComponentContent<KuiklyRenderBaseView> {
    const uiContext = this.getUIContext() as UIContext;
    return new ComponentContent<KuiklyRenderBaseView>(
      uiContext,
      wrapBuilder<[KuiklyRenderBaseView]>(createKuiklyPageView),
      this
    );
  }

  setProp(propKey: string, propValue: KRAny | KuiklyRenderCallback): boolean {
    switch (propKey) {
      case PROP_PAGE_NAME: {
        const newName = typeof propValue === 'string' ? propValue : '';
        if (newName.length > 0 && newName !== this.pageName) {
          this.pageName = newName;
          hilog.info(0, TAG, `setProp pageName=${newName}`);
        }
        return true;
      }
      case PROP_PAGE_DATA: {
        this.pageData = this.parsePageData(propValue);
        return true;
      }
      default:
        return super.setProp(propKey, propValue);
    }
  }

  call(method: string, params: KRAny, callback: KuiklyRenderCallback | null): KRAny {
    return null;
  }

  private parsePageData(value: KRAny | KuiklyRenderCallback): KRRecord {
    if (value && typeof value === 'object') {
      return value as KRRecord;
    }
    if (typeof value === 'string' && value.length > 0) {
      try {
        const obj = JSON.parse(value) as KRRecord;
        return obj ?? {};
      } catch (e) {
        hilog.warn(0, TAG, `parsePageData failed: ${JSON.stringify(e)}`);
      }
    }
    return {};
  }

  /** 子 Pager 渲染异常回调（写入字段后由 ArkUI 重绘成错误占位） */
  onChildRenderException(exception: Error, _reason: ErrorReason): void {
    this.exception = `${exception.name}:\n${exception.message}`;
    hilog.error(0, TAG, this.exception);
  }
}

@Component
export struct KRKuiklyPage {
  @ObjectLink renderView: KRKuiklyPageView;

  build() {
    Stack() {
      if (this.renderView.pageName.length > 0) {
        Kuikly({
          pagerName: this.renderView.pageName,
          pagerData: this.renderView.pageData,
          delegate: this.renderView.innerDelegate,
          nativeManager: globalNativeManager,
          onRenderException: (exception: Error, reason: ErrorReason) => {
            this.renderView.onChildRenderException(exception, reason);
          },
        })
      }
      if (this.renderView.exception.length > 0) {
        Text(this.renderView.exception)
          .fontSize(12)
          .fontColor(Color.Red)
      }
    }
    .width('100%')
    .height('100%')
  }
}

@Builder
function createKuiklyPageView(view: KuiklyRenderBaseView) {
  KRKuiklyPage({ renderView: view as KRKuiklyPageView })
}
