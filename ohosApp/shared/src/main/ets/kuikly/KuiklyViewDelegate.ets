import {
  IKuiklyViewDelegate,
  KRRecord,
  KRRenderModuleExportCreator,
  KRRenderViewExportCreator
} from '@kuikly/render';
import { KRMyView } from './components/KRMyView';
import { KRWSVideoView } from './components/KRWSVideoView';
import { KRQnLottieView } from './components/KRQnLottieView';
import { KRQnQrCodeView } from './components/KRQnQrCodeView';
import { KRQnScreenshotView } from './components/KRQnScreenshotView';
import { KRQnWebView } from './components/KRQnWebView';
import { KRKuiklyPageView } from './components/KRKuiklyPageView';
import { KRBridgeModule } from './modules/KRBridgeModule';
import { KRMyModule } from './modules/KRMyModule';

interface PageEventSender {
  sendWithEvent?: (eventName: string, data: KRRecord) => void;
  sendEvent?: (eventName: string, data: KRRecord) => void;
}

export class KuiklyViewDelegate extends IKuiklyViewDelegate {
  getCustomRenderViewCreatorRegisterMap(): Map<string, KRRenderViewExportCreator> {
    const map: Map<string, KRRenderViewExportCreator> = new Map();
    map.set(KRMyView.VIEW_NAME, () => new KRMyView());
    map.set(KRWSVideoView.VIEW_NAME, () => new KRWSVideoView());
    map.set(KRQnLottieView.VIEW_NAME, () => new KRQnLottieView());
    map.set(KRQnQrCodeView.VIEW_NAME, () => new KRQnQrCodeView());
    map.set(KRQnScreenshotView.VIEW_NAME, () => new KRQnScreenshotView());
    map.set(KRQnWebView.VIEW_NAME, () => new KRQnWebView());
    map.set(KRKuiklyPageView.VIEW_NAME, () => new KRKuiklyPageView());
    return map;
  }

  getCustomRenderModuleCreatorRegisterMap(): Map<string, KRRenderModuleExportCreator> {
    const map: Map<string, KRRenderModuleExportCreator> = new Map();
    map.set(KRBridgeModule.MODULE_NAME, () => new KRBridgeModule());
    map.set(KRMyModule.MODULE_NAME, () => new KRMyModule())
    return map;
  }

  sendPageEvent(eventName: string, data: KRRecord): boolean {
    const delegate = this as PageEventSender;
    try {
      if (typeof delegate.sendWithEvent === 'function') {
        delegate.sendWithEvent(eventName, data ?? {});
        return true;
      }
      if (typeof delegate.sendEvent === 'function') {
        delegate.sendEvent(eventName, data ?? {});
        return true;
      }
    } catch (e) {
      console.error(`[KuiklyViewDelegate] sendPageEvent failed: ${eventName}, ${JSON.stringify(e)}`);
    }
    console.warn(`[KuiklyViewDelegate] sendPageEvent unsupported: ${eventName}`);
    return false;
  }
}
