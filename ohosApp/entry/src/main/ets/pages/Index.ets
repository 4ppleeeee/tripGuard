import router from '@ohos.router';
import {
  COMPOSE_EVENT_ON_PAGE_NEW_INTENT,
  ErrorReason,
  getHarmonyPageStackManager,
  globalNativeManager,
  KRRecord,
  Kuikly,
  KuiklyViewDelegate,
  OhosComposePageRegistry,
  WsKmmContext
} from '@kmm/core';
import { window } from '@kit.ArkUI';

@Entry
@Component
struct Index {
  kuiklyViewDelegate: KuiklyViewDelegate = new KuiklyViewDelegate();
  pageName?: string;
  pageData?: KRRecord;
  kmmContext?: WsKmmContext;
  private pageInstanceId: number = OhosComposePageRegistry.nextPageId();
  @State exception?: string = "";

  aboutToAppear() {
    AppStorage.setOrCreate<UIContext>("context", this.getUIContext());
    const params = router.getParams() as Record<string, Object>;
    this.pageName = (params?.pageName as string) ?? '/page/demo_home';
    this.pageData = (params?.pageData as KRRecord) ?? {};
    OhosComposePageRegistry.register(this.pageInstanceId, this.pageName, (pageData: KRRecord): boolean => {
      return this.dispatchPageNewIntent(pageData);
    });

    window.getLastWindow(this.getUIContext().getHostContext()).then((win) => {
      const properties = win.getWindowProperties();
      this.kmmContext = new WsKmmContext(properties.id);
      getHarmonyPageStackManager().onPageCreated(this.kmmContext)
    }).catch((err: Error) => {
      console.error('Failed: ' + JSON.stringify(err));
    });
  }

  build() {
    Stack() {
      Kuikly({
        pagerName: this.pageName ?? '/page/demo_home',
        pagerData: this.pageData ?? {},
        delegate: this.kuiklyViewDelegate,
        nativeManager: globalNativeManager,
        onRenderException: (exception: Error, reason: ErrorReason) => {
          this.exception = `${exception.name}:\n${exception.message}`;
        },
      })
      if (this.exception) {
        Text(this.exception)
      }
    }
    .width('100%')
    .height('100%')
    .expandSafeArea([SafeAreaType.KEYBOARD])
  }

  onPageShow(): void {
    OhosComposePageRegistry.markVisible(this.pageInstanceId);
    this.kuiklyViewDelegate.pageDidAppear();
  }

  onPageHide(): void {
    OhosComposePageRegistry.clearVisible(this.pageInstanceId);
    this.kuiklyViewDelegate.pageDidDisappear();
  }

  aboutToDisappear(): void {
    OhosComposePageRegistry.unregister(this.pageInstanceId);
    if (this.kmmContext) {
      getHarmonyPageStackManager().onPageDestroyed(this.kmmContext)
    }
  }

  private dispatchPageNewIntent(pageData: KRRecord): boolean {
    this.pageData = pageData ?? {};
    return this.kuiklyViewDelegate.sendPageEvent(COMPOSE_EVENT_ON_PAGE_NEW_INTENT, this.pageData);
  }
}
