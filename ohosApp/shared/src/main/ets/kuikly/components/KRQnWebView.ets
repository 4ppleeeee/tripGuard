import { KRAny, KuiklyRenderBaseView, KuiklyRenderCallback } from '@kuikly/render';
import { ComponentContent } from '@kit.ArkUI';
import { webview } from '@kit.ArkWeb';
import { common, Want } from '@kit.AbilityKit';
import { KRQnWebViewJSBridge, QnJSProxyObject } from './KRQnWebViewJSBridge';
import { WsAppRuntimeInfo } from '../../kmm/WsAppRuntimeInfo';

/**
 * javaScriptProxy 配置接口
 */
interface JavaScriptProxyConfig {
  object: QnJSProxyObject;
  name: string;
  methodList: Array<string>;
  controller: webview.WebviewController;
}

/**
 * Kuikly 方法回调的布尔结果模型。
 *
 * 适用于 `canGoBack`、`canGoForward` 这类需要把布尔值透传回 common 层的方法回调。
 */
interface BooleanCallbackResult {
  /** 查询结果，true 表示当前 WebView 支持对应操作，false 表示不支持。 */
  value: boolean;
}

/** POST 加载参数模型。 */
interface PostUrlParams {
  /** 待加载的 URL。 */
  url: string;
  /** POST 请求体内容。 */
  postData: string;
}

// 默认的空 proxy 对象
const defaultProxyObject: QnJSProxyObject = {
  postMessage: (_msg: string) => {}
};

// H5 旧 JSAPI 协议 scheme，必须优先拦截，避免被 Web 组件按普通 URL 加载。
const SCHEME_JS_BRIDGE: string = 'jsbridge://';
// App 业务 scheme，拦截后交给 common 层统一路由处理。
const SCHEME_APP: string = 'demo://';
// AiSee/DCL 反馈页日志回调 scheme，拦截后交给 common 层消费。
const SCHEME_AISEE: string = 'aisee://';
// WebView 旧版 UA 中的 QQ JS SDK 标识，需在业务默认 UA 中补齐。
const EXTRA_QQ_JS_SDK: string = 'QQJSSDK/1.3';
// WebView 业务 UA 标识，仅追加一次。
const EXTRA_TENCENT_VIDEO_UNION: string = 'TenvideoUnion/1.0.0';
const DOWNLOAD_URL_PATH_EXTENSIONS: Array<string> = [
  '.apk', '.ipa', '.zip', '.rar', '.7z', '.gz', '.tar', '.tgz',
  '.dmg', '.pkg', '.exe', '.msi', '.bin'
];

/**
 * KRQnWebView - OHOS 端 WebView 渲染组件
 * 继承 KuiklyRenderBaseView，将鸿蒙 Web 组件暴露给 Kuikly 框架
 */
@Observed
export class KRQnWebView extends KuiklyRenderBaseView {
  static readonly VIEW_NAME = 'QnWebView';

  // Web 组件控制器
  controller: webview.WebviewController = new webview.WebviewController();

  // JSBridge 实例
  jsBridge: KRQnWebViewJSBridge | null = null;

  // 属性
  cssSrc: string = '';
  cssHtmlContent: string = '';
  cssJavaScriptEnabled: boolean = true;
  cssUserAgent: string = '';
  hasExplicitUserAgent: boolean = false;
  cssDomStorageEnabled: boolean = true;

  // 事件回调
  onPageStartedCallback: KuiklyRenderCallback | null = null;
  onPageFinishedCallback: KuiklyRenderCallback | null = null;
  onErrorCallback: KuiklyRenderCallback | null = null;
  onReceiveTitleCallback: KuiklyRenderCallback | null = null;
  onProgressChangedCallback: KuiklyRenderCallback | null = null;
  onJsBridgeRequestCallback: KuiklyRenderCallback | null = null;
  onSchemeRequestCallback: KuiklyRenderCallback | null = null;
  onMessageCallback: KuiklyRenderCallback | null = null;

  // 是否已初始化 controller
  private isControllerInitialized: boolean = false;

  // 缓存的 javaScriptProxy 配置（避免重复创建）
  cachedProxyConfig: JavaScriptProxyConfig | null = null;

  private pendingPostUrlParams: PostUrlParams | null = null;
  private currentMainDocumentUrl: string = '';

  // javaScriptProxy 暴露给 JS 的方法列表
  static readonly PROXY_METHOD_LIST: Array<string> = ['postMessage'];

  initJSBridge(): void {
    if (!this.jsBridge) {
      this.jsBridge = new KRQnWebViewJSBridge(this.controller, (message: string) => {
        if (this.onMessageCallback) {
          this.onMessageCallback({ 'message': message });
        }
      });
    }
  }

  /**
   * 构造 Kuikly 方法回调的布尔结果。
   *
   * common 层会把 native 方法回调按 JSONObject 解析，直接返回 true/false 或字符串会导致桥接解析失败。
   */
  private buildBooleanCallbackResult(value: boolean): BooleanCallbackResult {
    return { value: value };
  }

  createArkUIView(): ComponentContent<KuiklyRenderBaseView> {
    this.initJSBridge();
    this.getJavaScriptProxyConfig();
    const uiContext = this.getUIContext() as UIContext;
    return new ComponentContent<KuiklyRenderBaseView>(
      uiContext,
      wrapBuilder<[KuiklyRenderBaseView]>(createKRQnWebView),
      this
    );
  }

  setProp(propKey: string, propValue: KRAny | KuiklyRenderCallback): boolean {
    switch (propKey) {
      case 'src':
        this.cssSrc = propValue as string;
        if (this.isControllerInitialized && this.cssSrc.length > 0) {
          try {
            this.ensureBusinessUserAgent();
            this.updateMainDocumentUrl(this.cssSrc);
            this.controller.loadUrl(this.cssSrc);
          } catch (e) {
            console.error('KRQnWebView: Failed to load URL', JSON.stringify(e));
          }
        }
        return true;

      case 'htmlContent':
        this.cssHtmlContent = propValue as string;
        if (this.isControllerInitialized && this.cssHtmlContent.length > 0) {
          try {
            this.ensureBusinessUserAgent();
            this.currentMainDocumentUrl = '';
            this.controller.loadData(this.cssHtmlContent, 'text/html', 'UTF-8');
          } catch (e) {
            console.error('KRQnWebView: Failed to load HTML', JSON.stringify(e));
          }
        }
        return true;

      case 'javaScriptEnabled':
        this.cssJavaScriptEnabled = (propValue as string) !== 'false';
        return true;

      case 'userAgent':
        this.cssUserAgent = propValue as string;
        if (this.isControllerInitialized && this.cssUserAgent.length > 0) {
          try {
            this.hasExplicitUserAgent = true;
            this.controller.setCustomUserAgent(this.cssUserAgent);
          } catch (e) {
            console.error('KRQnWebView: Failed to set user agent', JSON.stringify(e));
          }
        }
        return true;

      case 'domStorageEnabled':
        this.cssDomStorageEnabled = (propValue as string) !== 'false';
        return true;

      case 'allowsInlineMediaPlayback':
        // OHOS Web 组件默认支持内联播放
        return true;

      // 事件回调
      case 'onPageStarted':
        this.onPageStartedCallback = propValue as KuiklyRenderCallback;
        return true;

      case 'onPageFinished':
        this.onPageFinishedCallback = propValue as KuiklyRenderCallback;
        return true;

      case 'onError':
        this.onErrorCallback = propValue as KuiklyRenderCallback;
        return true;

      case 'onReceiveTitle':
        this.onReceiveTitleCallback = propValue as KuiklyRenderCallback;
        return true;

      case 'onProgressChanged':
        this.onProgressChangedCallback = propValue as KuiklyRenderCallback;
        return true;

      case 'onJsBridgeRequest':
        this.onJsBridgeRequestCallback = propValue as KuiklyRenderCallback;
        return true;

      case 'onSchemeRequest':
        this.onSchemeRequestCallback = propValue as KuiklyRenderCallback;
        return true;

      case 'onMessage':
        this.onMessageCallback = propValue as KuiklyRenderCallback;
        return true;

      default:
        return super.setProp(propKey, propValue);
    }
  }

  call(method: string, params: KRAny, callback: KuiklyRenderCallback | null): KRAny {
    switch (method) {
      case 'loadUrl': {
        const url = params as string;
        if (url && this.isControllerInitialized) {
          try {
            this.ensureBusinessUserAgent();
            this.updateMainDocumentUrl(url);
            this.controller.loadUrl(url);
          } catch (e) {
            console.error('KRQnWebView: Failed to load URL', JSON.stringify(e));
          }
        }
        return null;
      }

      case 'postUrl': {
        const paramsStr = params as string;
        if (paramsStr) {
          try {
            const postUrlParams = JSON.parse(paramsStr) as PostUrlParams;
            if (postUrlParams.url.length > 0) {
              if (this.isControllerInitialized) {
                this.postUrl(postUrlParams);
              } else {
                this.pendingPostUrlParams = postUrlParams;
              }
            }
          } catch (e) {
            console.error('KRQnWebView: Failed to post URL', JSON.stringify(e));
          }
        }
        return null;
      }

      case 'loadHtml': {
        const paramsStr = params as string;
        if (paramsStr && this.isControllerInitialized) {
          try {
            const json: Record<string, string> = JSON.parse(paramsStr) as Record<string, string>;
            const html = json['html'] || '';
            this.ensureBusinessUserAgent();
            this.currentMainDocumentUrl = '';
            this.controller.loadData(html, 'text/html', 'UTF-8');
          } catch (e) {
            try {
              this.ensureBusinessUserAgent();
              this.currentMainDocumentUrl = '';
              this.controller.loadData(paramsStr, 'text/html', 'UTF-8');
            } catch (e2) {
              console.error('KRQnWebView: Failed to load HTML', JSON.stringify(e2));
            }
          }
        }
        return null;
      }

      case 'evaluateJavaScript': {
        const script = params as string;
        if (script && this.isControllerInitialized) {
          try {
            this.controller.runJavaScript(script)
              .then((result) => {
                if (callback) {
                  callback(result || '');
                }
              })
              .catch((error: Error) => {
                if (callback) {
                  callback('');
                }
              });
          } catch (e) {
            console.error('KRQnWebView: Failed to evaluate JS', JSON.stringify(e));
          }
        }
        return null;
      }

      case 'goBack': {
        if (this.isControllerInitialized) {
          try {
            if (this.controller.accessBackward()) {
              this.controller.backward();
            }
          } catch (e) {
            console.error('KRQnWebView: Failed to go back', JSON.stringify(e));
          }
        }
        return null;
      }

      case 'goForward': {
        if (this.isControllerInitialized) {
          try {
            if (this.controller.accessForward()) {
              this.controller.forward();
            }
          } catch (e) {
            console.error('KRQnWebView: Failed to go forward', JSON.stringify(e));
          }
        }
        return null;
      }

      case 'reload': {
        if (this.isControllerInitialized) {
          try {
            this.controller.refresh();
          } catch (e) {
            console.error('KRQnWebView: Failed to reload', JSON.stringify(e));
          }
        }
        return null;
      }

      case 'canGoBack': {
        if (callback && this.isControllerInitialized) {
          try {
            callback(this.buildBooleanCallbackResult(this.controller.accessBackward()));
          } catch (e) {
            callback(this.buildBooleanCallbackResult(false));
          }
        }
        return null;
      }

      case 'canGoForward': {
        if (callback && this.isControllerInitialized) {
          try {
            callback(this.buildBooleanCallbackResult(this.controller.accessForward()));
          } catch (e) {
            callback(this.buildBooleanCallbackResult(false));
          }
        }
        return null;
      }

      default: {
        return null;
      }
    }
  }

  /**
   * 组件销毁时清理资源
   */
  dispose(): void {
    this.jsBridge?.dispose();
    this.jsBridge = null;
    this.cachedProxyConfig = null as JavaScriptProxyConfig | null;
    this.pendingPostUrlParams = null as PostUrlParams | null;
    this.onPageStartedCallback = null;
    this.onPageFinishedCallback = null;
    this.onErrorCallback = null;
    this.onReceiveTitleCallback = null;
    this.onProgressChangedCallback = null;
    this.onJsBridgeRequestCallback = null;
    this.onSchemeRequestCallback = null;
    this.onMessageCallback = null;
  }

  /**
   * 处理 Web 组件拦截到的 URL。
   *
   * @param url 待加载 URL。
   * @returns true 表示当前组件已消费该 URL，不再交给 Web 组件继续加载。
   */
  handleLoadIntercept(url: string): boolean {
    if (url.startsWith(SCHEME_JS_BRIDGE)) {
      if (this.onJsBridgeRequestCallback) {
        this.onJsBridgeRequestCallback({ 'url': url });
      }
      return true;
    }
    const isAiseeUrl = url.startsWith(SCHEME_AISEE);
    if (url.startsWith(SCHEME_APP) || isAiseeUrl) {
      if (this.onSchemeRequestCallback) {
        this.onSchemeRequestCallback({ 'url': url });
      }
      return true;
    }
    if (this.isDownloadUrl(url)) {
      this.openDownloadUrl(url, 'loadIntercept');
      return true;
    }
    return false;
  }

  // ArkWeb 下载回调和 URL 拦截互为兜底，避免 APK 等下载链接被当成普通页面留在 WebView 内。
  handleDownloadStart(
    url: string,
    userAgent: string,
    contentDisposition: string,
    mimetype: string,
    contentLength: number
  ): void {
    if (url.length === 0) {
      console.warn('KRQnWebView: download skipped, blank url');
      return;
    }
    console.info(
      `KRQnWebView: download start url=${url}, ` +
        `userAgentLength=${userAgent.length}, contentDisposition=${contentDisposition}, ` +
        `mimetype=${mimetype}, contentLength=${contentLength}`
    );
    this.openDownloadUrl(url, 'downloadStart');
  }

  /**
   * 获取 javaScriptProxy 配置对象（缓存，避免重复创建）
   */
  getJavaScriptProxyConfig(): JavaScriptProxyConfig {
    if (this.cachedProxyConfig === null) {
      this.initJSBridge();
      this.cachedProxyConfig = {
        object: this.jsBridge?.getJavaScriptProxyObject() ?? defaultProxyObject,
        name: 'KuiklyNativeHandler',
        methodList: KRQnWebView.PROXY_METHOD_LIST,
        controller: this.controller
      };
    }
    return this.cachedProxyConfig;
  }

  /**
   * Web 组件 onControllerAttached 回调中调用
   * 标记 controller 已就绪，处理延迟的 URL/HTML 加载
   */
  onWebControllerReady(): void {
    this.isControllerInitialized = true;
    this.initJSBridge();

    if (this.cssUserAgent.length > 0) {
      try {
        this.hasExplicitUserAgent = true;
        this.controller.setCustomUserAgent(this.cssUserAgent);
      } catch (e) {
        console.error('KRQnWebView: Failed to set user agent', JSON.stringify(e));
      }
    }
    if (this.pendingPostUrlParams !== null) {
      this.postUrl(this.pendingPostUrlParams);
      this.pendingPostUrlParams = null as PostUrlParams | null;
    } else if (this.cssSrc.length > 0) {
      try {
        this.ensureBusinessUserAgent();
        this.updateMainDocumentUrl(this.cssSrc);
        this.controller.loadUrl(this.cssSrc);
      } catch (e) {
        console.error('KRQnWebView: Failed to load URL', JSON.stringify(e));
      }
    } else if (this.cssHtmlContent.length > 0) {
      try {
        this.ensureBusinessUserAgent();
        this.currentMainDocumentUrl = '';
        this.controller.loadData(this.cssHtmlContent, 'text/html', 'UTF-8');
      } catch (e) {
        console.error('KRQnWebView: Failed to load HTML', JSON.stringify(e));
      }
    }
  }

  /**
   * 页面加载前设置业务 UA，确保 H5 首次读取 navigator.userAgent 时已生效。
   */
  private ensureBusinessUserAgent(): void {
    const currentUserAgent = this.controller.getUserAgent().trim();
    if (
      this.hasExplicitUserAgent ||
      (currentUserAgent.includes(EXTRA_QQ_JS_SDK) && currentUserAgent.includes(EXTRA_TENCENT_VIDEO_UNION))
    ) {
      return;
    }
    const qua = this.buildBusinessQua();
    if (qua.length === 0 || currentUserAgent.length === 0) {
      return;
    }
    this.controller.setCustomUserAgent(`${qua}/${currentUserAgent} ${EXTRA_QQ_JS_SDK} ${EXTRA_TENCENT_VIDEO_UNION}`);
  }

  private buildBusinessQua(): string {
    const versionName = WsAppRuntimeInfo.appVersion.trim();
    return versionName.length > 0 ? `V1_HAR_KMM_${versionName}` : '';
  }

  private postUrl(params: PostUrlParams): void {
    this.ensureBusinessUserAgent();
    this.updateMainDocumentUrl(params.url);
    this.controller.postUrl(
      params.url,
      this.stringToUtf8ArrayBuffer(params.postData || '')
    );
  }

  updateMainDocumentUrl(url: string): void {
    const targetUrl = this.normalizeMainDocumentUrl(url);
    if (targetUrl.length > 0) {
      this.currentMainDocumentUrl = targetUrl;
    }
  }

  private normalizeMainDocumentUrl(url: string): string {
    const targetUrl = url.trim();
    const fragmentIndex = targetUrl.indexOf('#');
    return fragmentIndex >= 0 ? targetUrl.substring(0, fragmentIndex) : targetUrl;
  }

  // 主文档网络请求不会携带 fragment，先剥离 `#` 及之后片段再对齐 OHOS 主资源错误判断。
  isMainDocumentError(failedUrl: string): boolean {
    const targetUrl = this.normalizeMainDocumentUrl(failedUrl);
    if (targetUrl.length === 0 || this.currentMainDocumentUrl.length === 0) {
      console.info(
        `KRQnWebView: ignore error without main url, failedUrl=${targetUrl}, ` +
          `mainUrl=${this.currentMainDocumentUrl}`
      );
      return false;
    }
    const isMainDocument = targetUrl === this.currentMainDocumentUrl;
    if (!isMainDocument) {
      console.info(
        `KRQnWebView: ignore subresource error, failedUrl=${targetUrl}, ` +
          `mainUrl=${this.currentMainDocumentUrl}`
      );
    }
    return isMainDocument;
  }

  private isDownloadUrl(url: string): boolean {
    if (!this.isHttpOrHttpsUrl(url)) return false;
    const path = url.split('?')[0].split('#')[0].toLowerCase();
    return DOWNLOAD_URL_PATH_EXTENSIONS.some((extension: string) => path.endsWith(extension));
  }

  private isHttpOrHttpsUrl(url: string): boolean {
    const lowerUrl = url.toLowerCase();
    return lowerUrl.startsWith('http://') || lowerUrl.startsWith('https://');
  }

  private openDownloadUrl(url: string, source: string): void {
    if (!this.isHttpOrHttpsUrl(url)) {
      console.warn(`KRQnWebView: download skipped, unsupported url=${url}`);
      return;
    }
    try {
      const uiContext = this.getUIContext();
      if (uiContext === null) {
        console.warn(`KRQnWebView: download skipped, UIContext is null, source=${source}`);
        return;
      }
      const context = uiContext.getHostContext() as common.UIAbilityContext;
      const want: Want = { uri: url };
      context.startAbility(want)
        .then(() => {
          console.info(`KRQnWebView: open download url success, source=${source}`);
        })
        .catch((error: Error) => {
          console.error(`KRQnWebView: open download url failed, source=${source}, error=${JSON.stringify(error)}`);
        });
    } catch (error) {
      console.error(`KRQnWebView: open download url error, source=${source}, error=${JSON.stringify(error)}`);
    }
  }

  private stringToUtf8ArrayBuffer(value: string): ArrayBuffer {
    const encoded = encodeURIComponent(value);
    const bytes: number[] = [];
    for (let index = 0; index < encoded.length; index++) {
      const char = encoded.charAt(index);
      if (char === '%' && index + 2 < encoded.length) {
        bytes.push(parseInt(encoded.substring(index + 1, index + 3), 16));
        index += 2;
      } else {
        bytes.push(char.charCodeAt(0));
      }
    }

    const buffer = new ArrayBuffer(bytes.length);
    const view = new Uint8Array(buffer);
    bytes.forEach((byte, index) => {
      view[index] = byte;
    });
    return buffer;
  }
}

@Builder
function createKRQnWebView(view: KuiklyRenderBaseView) {
  KRQnWebViewComponent({ renderView: view as KRQnWebView })
}

@Component
struct KRQnWebViewComponent {
  @ObjectLink renderView: KRQnWebView;

  build() {
    Web({
      src: '',
      controller: this.renderView.controller
    })
      .javaScriptAccess(this.renderView.cssJavaScriptEnabled)
      .domStorageAccess(this.renderView.cssDomStorageEnabled)
      .javaScriptProxy(this.renderView.getJavaScriptProxyConfig())
      .onControllerAttached(() => {
        this.renderView.onWebControllerReady();
      })
      .onPageBegin((event) => {
        this.renderView.updateMainDocumentUrl(event?.url ?? '');
        if (this.renderView.onPageStartedCallback) {
          this.renderView.onPageStartedCallback({ 'url': event?.url ?? '' });
        }
      })
      .onPageEnd((event) => {
        // 页面加载完成后注入 JSBridge 脚本
        this.renderView.jsBridge?.injectBridgeScript();
        this.renderView.updateMainDocumentUrl(event?.url ?? '');
        if (this.renderView.onPageFinishedCallback) {
          this.renderView.onPageFinishedCallback({ 'url': event?.url ?? '' });
        }
      })
      .onErrorReceive((event) => {
        if (this.renderView.onErrorCallback) {
          const failedUrl = event?.request?.getRequestUrl() ?? '';
          if (!this.renderView.isMainDocumentError(failedUrl)) {
            return;
          }
          const error = event?.error;
          this.renderView.onErrorCallback({
            'errorCode': error?.getErrorCode() ?? -1,
            'description': error?.getErrorInfo() ?? 'Unknown error'
          });
        }
      })
      .onProgressChange((event) => {
        if (this.renderView.onProgressChangedCallback) {
          this.renderView.onProgressChangedCallback({ 'progress': event?.newProgress ?? 0 });
        }
      })
      .onTitleReceive((event) => {
        if (this.renderView.onReceiveTitleCallback) {
          this.renderView.onReceiveTitleCallback({ 'title': event?.title ?? '' });
        }
      })
      .onDownloadStart((event) => {
        this.renderView.handleDownloadStart(
          event?.url ?? '',
          event?.userAgent ?? '',
          event?.contentDisposition ?? '',
          event?.mimetype ?? '',
          event?.contentLength ?? 0
        );
      })
      .onLoadIntercept((event) => {
        const url = event.data.getRequestUrl();
        return this.renderView.handleLoadIntercept(url);
      })
      .width('100%')
      .height('100%')
  }
}
