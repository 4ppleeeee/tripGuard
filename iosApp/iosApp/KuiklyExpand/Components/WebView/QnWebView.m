#import "QnWebView.h"
#import "QnWebViewJSBridge.h"
#import "iosApp-Swift.h"
#import <KuiklyIOSRender/KRComponentDefine.h>

static NSString *const kNativeHandlerName = @"KuiklyNativeHandler";
/** H5 旧 JSAPI 协议 scheme，原生层需优先拦截并透传给 common。 */
static NSString *const kJsBridgeScheme = @"jsbridge";
/** 微视业务 scheme，需拦截并透传给 common 层统一路由处理。 */
static NSString *const kWeishiScheme = @"weishi";
/** AiSee/DCL 反馈页日志回调 scheme，需拦截并透传给 common 层消费。 */
static NSString *const kAiseeScheme = @"aisee";
/** Kuikly `callMethod` 布尔回调结果字段名，common 层按该字段读取结果。 */
static NSString *const kCallbackResultKey = @"value";
/** 微视 WebView 旧版 UA 中的 QQ JS SDK 标识，需在业务默认 UA 中补齐。 */
static NSString *const kExtraQQJSSDK = @"QQJSSDK/1.3";
/** 微视 WebView 业务 UA 标识，仅追加一次。 */
static NSString *const kExtraTencentVideoUnion = @"TenvideoUnion/1.0.0";
/** 腾讯新闻 JSAPI 依赖 qqnews UA 判断客户端环境。 */
static NSString *const kExtraQQNews = @"qqnews/8.0.00";
/** 重置密码实名认证媒体权限只允许 Webank/FaceID 域及其子域申请。 */
static NSString *const kTeenResetAuthWebankHost = @"webank.com";
static NSString *const kTeenResetAuthFaceIdHost = @"faceid.qq.com";
/** AiSee/DCL 反馈页媒体附件采集域名。 */
static NSString *const kDclFeedbackTrustedHost = @"h5.dcl.qq.com";

static BOOL QnHostMatchesTeenResetTrustedHost(NSString *host, NSString *trustedHost) {
    return [host isEqualToString:trustedHost] ||
        [host hasSuffix:[NSString stringWithFormat:@".%@", trustedHost]];
}

static NSArray<NSString *> *QnDownloadURLPathExtensions(void) {
    static NSArray<NSString *> *extensions = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        extensions = @[@".apk", @".ipa", @".zip", @".rar", @".7z", @".gz", @".tar", @".tgz",
                       @".dmg", @".pkg", @".exe", @".msi", @".bin"];
    });
    return extensions;
}

/**
 * 共享 WKProcessPool 单例
 * 多个 QnWebView 实例共享同一 Web 进程池，减少进程数量和内存开销
 * 同时实现实例间 Cookie/SessionStorage 共享
 */
static WKProcessPool *_sharedProcessPool = nil;
static dispatch_once_t _processPoolOnceToken;

@interface QnWebView ()

@property (nonatomic, strong, readwrite) WKWebView *webView;
@property (nonatomic, strong, readwrite) QnWebViewJSBridge *jsBridge;
@property (nonatomic, strong) QnWebViewFileInputCoordinator *fileInputCoordinator;

// KVO 注册状态标记，避免 dealloc 中使用 @try/@catch（异常处理有栈展开开销）
@property (nonatomic, assign) BOOL isObservingProgress;
@property (nonatomic, assign) BOOL isObservingTitle;

// 事件回调
@property (nonatomic, copy, nullable) KuiklyRenderCallback css_onPageStarted;
@property (nonatomic, copy, nullable) KuiklyRenderCallback css_onPageFinished;
@property (nonatomic, copy, nullable) KuiklyRenderCallback css_onError;
@property (nonatomic, copy, nullable) KuiklyRenderCallback css_onReceiveTitle;
@property (nonatomic, copy, nullable) KuiklyRenderCallback css_onProgressChanged;
@property (nonatomic, copy, nullable) KuiklyRenderCallback css_onJsBridgeRequest;
@property (nonatomic, copy, nullable) KuiklyRenderCallback css_onSchemeRequest;
@property (nonatomic, copy, nullable) KuiklyRenderCallback css_onMessage;

// 属性
@property (nonatomic, copy, nullable) NSString *css_src;
@property (nonatomic, copy, nullable) NSString *css_htmlContent;
@property (nonatomic, assign) BOOL css_teenResetAuthScene;
@property (nonatomic, assign) BOOL hasExplicitUserAgent;
@property (nonatomic, assign) BOOL didApplyBusinessUserAgent;
@property (nonatomic, assign) BOOL isResolvingBusinessUserAgent;
@property (nonatomic, strong) NSMutableArray<dispatch_block_t> *pendingBusinessUserAgentBlocks;

@end

@implementation QnWebView

@synthesize hr_rootView;

- (instancetype)init {
    self = [super init];
    if (self) {
        _pendingBusinessUserAgentBlocks = [NSMutableArray array];
        [self setupWebView];
    }
    return self;
}

- (void)dealloc {
    [self.jsBridge dispose];
    [self.webView.configuration.userContentController removeScriptMessageHandlerForName:kNativeHandlerName];
    // 使用布尔标记安全移除 KVO，避免 @try/@catch 的栈展开开销
    if (self.isObservingProgress) {
        [self.webView removeObserver:self forKeyPath:@"estimatedProgress"];
        self.isObservingProgress = NO;
    }
    if (self.isObservingTitle) {
        [self.webView removeObserver:self forKeyPath:@"title"];
        self.isObservingTitle = NO;
    }
    self.webView.navigationDelegate = nil;
    self.webView.UIDelegate = nil;
    [self.fileInputCoordinator cancel];
}

#pragma mark - Layout

- (void)layoutSubviews {
    [super layoutSubviews];
    self.webView.frame = self.bounds;
}

#pragma mark - Setup

- (void)setupWebView {
    WKWebViewConfiguration *config = [[WKWebViewConfiguration alloc] init];
    config.userContentController = [[WKUserContentController alloc] init];
    config.allowsInlineMediaPlayback = YES;
    config.mediaTypesRequiringUserActionForPlayback = WKAudiovisualMediaTypeNone;
    
    // 共享 WKProcessPool：减少进程数量和内存开销，实现 Cookie/SessionStorage 共享
    dispatch_once(&_processPoolOnceToken, ^{
        _sharedProcessPool = [[WKProcessPool alloc] init];
    });
    config.processPool = _sharedProcessPool;

    WKUserScript *bridgeScript = [[WKUserScript alloc] initWithSource:[QnWebViewJSBridge cachedFullBridgeScript]
                                                        injectionTime:WKUserScriptInjectionTimeAtDocumentStart
                                                     forMainFrameOnly:NO];
    [config.userContentController addUserScript:bridgeScript];

    self.webView = [[WKWebView alloc] initWithFrame:self.bounds configuration:config];
    self.webView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    self.webView.navigationDelegate = self;
    self.webView.UIDelegate = self;
    [self addSubview:self.webView];

    // 初始化 JSBridge
    __weak typeof(self) weakSelf = self;
    self.jsBridge = [[QnWebViewJSBridge alloc] initWithWebView:self.webView
                                                     onMessage:^(NSString *message) {
        __strong typeof(weakSelf) strongSelf = weakSelf;
        if (strongSelf.css_onMessage) {
            strongSelf.css_onMessage(@{@"message": message ?: @""});
        }
    }];

    // 注册 ScriptMessageHandler
    [self.webView.configuration.userContentController addScriptMessageHandler:self.jsBridge
                                                                         name:kNativeHandlerName];

    // KVO 监听进度和标题（记录注册状态，dealloc 时安全移除）
    [self.webView addObserver:self forKeyPath:@"estimatedProgress" options:NSKeyValueObservingOptionNew context:nil];
    self.isObservingProgress = YES;
    [self.webView addObserver:self forKeyPath:@"title" options:NSKeyValueObservingOptionNew context:nil];
    self.isObservingTitle = YES;
}

#pragma mark - KuiklyRenderViewExportProtocol

- (void)hrv_setPropWithKey:(NSString *)propKey propValue:(id)propValue {
    if ([propKey isEqualToString:@"teenResetAuthScene"]) {
        self.css_teenResetAuthScene = [propValue respondsToSelector:@selector(boolValue)] ? [propValue boolValue] : NO;
        return;
    }
    // 设置通用样式（布局、背景色等）
    KUIKLY_SET_CSS_COMMON_PROP;
    // 自定义属性和事件通过 setCss_xxx: 方法自动分发（运行时匹配）
}

- (void)hrv_callWithMethod:(NSString *)method
                    params:(NSString *)params
                  callback:(KuiklyRenderCallback)callback {
    KUIKLY_CALL_CSS_METHOD;
}

#pragma mark - CSS Properties (由 KUIKLY_SET_CSS_COMMON_PROP 运行时分发)

- (void)setCss_src:(NSString *)css_src {
    if ([_css_src isEqualToString:css_src]) return;
    _css_src = css_src;
    if (css_src.length > 0) {
        NSURL *url = [NSURL URLWithString:css_src];
        if (url) {
            __weak typeof(self) weakSelf = self;
            [self performAfterBusinessUserAgentReady:^{
                __strong typeof(weakSelf) strongSelf = weakSelf;
                if (!strongSelf) return;
                [strongSelf syncCookiesToWKStoreForURL:url completion:^{
                    [strongSelf.webView loadRequest:[NSURLRequest requestWithURL:url]];
                }];
            }];
        }
    }
}

- (void)setCss_htmlContent:(NSString *)css_htmlContent {
    if ([_css_htmlContent isEqualToString:css_htmlContent]) return;
    _css_htmlContent = css_htmlContent;
    if (css_htmlContent.length > 0) {
        __weak typeof(self) weakSelf = self;
        [self performAfterBusinessUserAgentReady:^{
            __strong typeof(weakSelf) strongSelf = weakSelf;
            if (!strongSelf) return;
            [strongSelf.webView loadHTMLString:css_htmlContent baseURL:nil];
        }];
    }
}

- (void)setCss_javaScriptEnabled:(NSString *)enabled {
    // WKWebView 默认启用 JS，iOS 14+ 无法禁用
    // 此属性保留以保持跨端 API 一致性
}

- (void)setCss_userAgent:(NSString *)userAgent {
    if (userAgent.length > 0) {
        self.hasExplicitUserAgent = YES;
        self.didApplyBusinessUserAgent = YES;
        self.webView.customUserAgent = userAgent;
    }
}

- (void)setCss_domStorageEnabled:(NSString *)enabled {
    // WKWebView 默认启用 DOM Storage，无需额外设置
}

- (void)setCss_allowsInlineMediaPlayback:(NSString *)allowed {
    // 已在初始化时配置，运行时不可更改
}

#pragma mark - CSS Methods (由 KUIKLY_CALL_CSS_METHOD 运行时分发)

- (void)css_loadUrl:(NSDictionary *)args {
    NSString *params = args[KRC_PARAM_KEY];
    if (params.length > 0) {
        NSURL *url = [NSURL URLWithString:params];
        if (url) {
            __weak typeof(self) weakSelf = self;
            [self performAfterBusinessUserAgentReady:^{
                __strong typeof(weakSelf) strongSelf = weakSelf;
                if (!strongSelf) return;
                [strongSelf syncCookiesToWKStoreForURL:url completion:^{
                    [strongSelf.webView loadRequest:[NSURLRequest requestWithURL:url]];
                }];
            }];
        }
    }
}

- (void)css_postUrl:(NSDictionary *)args {
    NSString *params = args[KRC_PARAM_KEY];
    if (params.length == 0) return;

    NSData *jsonData = [params dataUsingEncoding:NSUTF8StringEncoding];
    NSError *error = nil;
    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:jsonData options:0 error:&error];
    if (![json isKindOfClass:[NSDictionary class]]) return;

    NSString *urlString = json[@"url"] ?: @"";
    NSString *postData = json[@"postData"] ?: @"";
    NSURL *url = [NSURL URLWithString:urlString];
    if (!url) return;

    __weak typeof(self) weakSelf = self;
    [self performAfterBusinessUserAgentReady:^{
        __strong typeof(weakSelf) strongSelf = weakSelf;
        if (!strongSelf) return;
        [strongSelf syncCookiesToWKStoreForURL:url completion:^{
            NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
            [request setHTTPMethod:@"POST"];
            [request setHTTPBody:[postData dataUsingEncoding:NSUTF8StringEncoding]];
            [request setValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"Content-Type"];
            [strongSelf.webView loadRequest:request];
        }];
    }];
}

- (void)css_loadHtml:(NSDictionary *)args {
    NSString *params = args[KRC_PARAM_KEY];
    if (params.length > 0) {
        NSError *error = nil;
        NSDictionary *json = [NSJSONSerialization JSONObjectWithData:[params dataUsingEncoding:NSUTF8StringEncoding]
                                                             options:0
                                                               error:&error];
        if (json) {
            NSString *html = json[@"html"] ?: @"";
            NSString *baseUrlStr = json[@"baseUrl"];
            NSURL *baseUrl = baseUrlStr.length > 0 ? [NSURL URLWithString:baseUrlStr] : nil;
            __weak typeof(self) weakSelf = self;
            [self performAfterBusinessUserAgentReady:^{
                __strong typeof(weakSelf) strongSelf = weakSelf;
                if (!strongSelf) return;
                [strongSelf syncCookiesToWKStoreForURL:baseUrl completion:^{
                    [strongSelf.webView loadHTMLString:html baseURL:baseUrl];
                }];
            }];
        } else {
            __weak typeof(self) weakSelf = self;
            [self performAfterBusinessUserAgentReady:^{
                __strong typeof(weakSelf) strongSelf = weakSelf;
                if (!strongSelf) return;
                [strongSelf.webView loadHTMLString:params baseURL:nil];
            }];
        }
    }
}

#pragma mark - Cookie Sync

/**
 * 将 NSHTTPCookieStorage 中与目标 URL 匹配的 Cookie 同步到 WKHTTPCookieStore。
 *
 * 背景：
 * iOS 的 NSHTTPCookieStorage（Foundation 层）和 WKHTTPCookieStore（WebKit 层）是两个独立容器，
 * 不会自动同步。KMM 侧 CookieService 写入 NSHTTPCookieStorage 后，WKWebView 发起请求时
 * 读取的是 WKHTTPCookieStore，如果此时 WK 侧尚未写入，请求就不会携带 Cookie。
 *
 * 方案（与 microvision 旧工程 registerCookieByJsScriptWithCompletionHandler 一致）：
 * 使用 dispatch_group 等待所有 WKHTTPCookieStore.setCookie 的 completionHandler 回调，
 * 确保 Cookie 全部写入完成后再执行页面加载。
 */
- (void)syncCookiesToWKStoreForURL:(NSURL *)url completion:(dispatch_block_t)completion {
    if (!url || !completion) {
        if (completion) completion();
        return;
    }

    NSArray<NSHTTPCookie *> *cookies = [[NSHTTPCookieStorage sharedHTTPCookieStorage] cookiesForURL:url];
    if (cookies.count == 0) {
        completion();
        return;
    }

    WKHTTPCookieStore *wkStore = self.webView.configuration.websiteDataStore.httpCookieStore;
    dispatch_group_t group = dispatch_group_create();
    for (NSHTTPCookie *cookie in cookies) {
        dispatch_group_enter(group);
        [wkStore setCookie:cookie completionHandler:^{
            dispatch_group_leave(group);
        }];
    }
    dispatch_group_notify(group, dispatch_get_main_queue(), ^{
        completion();
    });
}

#pragma mark - UserAgent

- (void)performAfterBusinessUserAgentReady:(dispatch_block_t)action {
    if (!action) return;
    if (self.hasExplicitUserAgent || self.didApplyBusinessUserAgent) {
        action();
        return;
    }

    [self.pendingBusinessUserAgentBlocks addObject:[action copy]];
    if (self.isResolvingBusinessUserAgent) {
        return;
    }

    self.isResolvingBusinessUserAgent = YES;
    __weak typeof(self) weakSelf = self;
    [self.webView evaluateJavaScript:@"navigator.userAgent" completionHandler:^(id result, NSError *error) {
        __strong typeof(weakSelf) strongSelf = weakSelf;
        if (!strongSelf) return;

        NSString *defaultUserAgent = [result isKindOfClass:[NSString class]] ? result : @"";
        NSString *qua = [strongSelf buildBusinessQua];
        if (defaultUserAgent.length > 0 &&
            qua.length > 0 &&
            (![defaultUserAgent containsString:kExtraQQJSSDK] ||
             ![defaultUserAgent containsString:kExtraTencentVideoUnion] ||
             ![defaultUserAgent containsString:kExtraQQNews])) {
            strongSelf.webView.customUserAgent = [NSString stringWithFormat:@"%@/%@ %@ %@ %@",
                                                   qua,
                                                   defaultUserAgent,
                                                   kExtraQQJSSDK,
                                                   kExtraTencentVideoUnion,
                                                   kExtraQQNews];
        }

        strongSelf.didApplyBusinessUserAgent = YES;
        strongSelf.isResolvingBusinessUserAgent = NO;
        NSArray<dispatch_block_t> *blocks = [strongSelf.pendingBusinessUserAgentBlocks copy];
        [strongSelf.pendingBusinessUserAgentBlocks removeAllObjects];
        for (dispatch_block_t block in blocks) {
            block();
        }
    }];
}

- (NSString *)buildBusinessQua {
    NSBundle *bundle = [NSBundle mainBundle];
    NSString *releaseVersion = [bundle objectForInfoDictionaryKey:@"CFBundleShortVersionString"] ?: @"";
    NSString *bundleVersion = [bundle objectForInfoDictionaryKey:@"CFBundleVersion"] ?: @"";
    NSString *marketVersion = [bundle objectForInfoDictionaryKey:@"Market version"] ?: @"";
    NSString *channelVersion = [bundle objectForInfoDictionaryKey:@"Channel version"] ?: @"";
    if (releaseVersion.length == 0) {
        return @"";
    }
    return [NSString stringWithFormat:@"V1_IPH_WEISHI_%@_%@_%@_%@", releaseVersion, bundleVersion, marketVersion, channelVersion];
}

- (void)css_evaluateJavaScript:(NSDictionary *)args {
    NSString *script = args[KRC_PARAM_KEY];
    KuiklyRenderCallback callback = args[KRC_CALLBACK_KEY];

    if (script.length > 0) {
        [self.webView evaluateJavaScript:script completionHandler:^(id result, NSError *error) {
            if (callback) {
                NSString *resultStr = nil;
                if (result) {
                    if ([result isKindOfClass:[NSString class]]) {
                        resultStr = result;
                    } else {
                        resultStr = [NSString stringWithFormat:@"%@", result];
                    }
                }
                callback(resultStr ?: @"");
            }
        }];
    }
}

- (void)css_goBack:(NSDictionary *)args {
    if (self.webView.canGoBack) {
        [self.webView goBack];
    }
}

- (void)css_goForward:(NSDictionary *)args {
    if (self.webView.canGoForward) {
        [self.webView goForward];
    }
}

- (void)css_reload:(NSDictionary *)args {
    [self.webView reload];
}

- (void)css_canGoBack:(NSDictionary *)args {
    KuiklyRenderCallback callback = args[KRC_CALLBACK_KEY];
    if (callback) {
        callback(@{kCallbackResultKey : @(self.webView.canGoBack)});
    }
}

- (void)css_canGoForward:(NSDictionary *)args {
    KuiklyRenderCallback callback = args[KRC_CALLBACK_KEY];
    if (callback) {
        callback(@{kCallbackResultKey : @(self.webView.canGoForward)});
    }
}

#pragma mark - WKNavigationDelegate

/**
 * 拦截 URL 加载，处理非标准 scheme（如 baiduboxapp://, weixin:// 等）
 * 避免加载未知 scheme 的 URL 导致错误
 */
- (void)webView:(WKWebView *)webView decidePolicyForNavigationAction:(WKNavigationAction *)navigationAction decisionHandler:(void (^)(WKNavigationActionPolicy))decisionHandler {
    NSURL *url = navigationAction.request.URL;
    NSString *scheme = url.scheme.lowercaseString;

    if ([scheme isEqualToString:kJsBridgeScheme]) {
        NSLog(@"QnWebView jsbridge url=%@", url.absoluteString ?: @"");
        if (self.css_onJsBridgeRequest) {
            self.css_onJsBridgeRequest(@{@"url": url.absoluteString ?: @""});
        }
        decisionHandler(WKNavigationActionPolicyCancel);
        return;
    }

    if ([scheme isEqualToString:kAiseeScheme]) {
        if (self.css_onSchemeRequest) {
            self.css_onSchemeRequest(@{@"url": url.absoluteString ?: @""});
        }
        decisionHandler(WKNavigationActionPolicyCancel);
        return;
    }

    if ([scheme isEqualToString:kWeishiScheme]) {
        if (self.css_onSchemeRequest) {
            self.css_onSchemeRequest(@{@"url": url.absoluteString ?: @""});
        }
        decisionHandler(WKNavigationActionPolicyCancel);
        return;
    }

    if ([self shouldOpenDownloadURLForNavigationAction:navigationAction]) {
        [self openDownloadURLExternally:url source:@"navigationAction"];
        decisionHandler(WKNavigationActionPolicyCancel);
        return;
    }
    
    if ([scheme isEqualToString:@"http"] || [scheme isEqualToString:@"https"] || [scheme isEqualToString:@"about"] || [scheme isEqualToString:@"file"]) {
        // 标准协议，允许 WebView 正常加载
        decisionHandler(WKNavigationActionPolicyAllow);
        return;
    }
    
    // 非标准 scheme，尝试通过系统打开对应 App
    @try {
        if ([[UIApplication sharedApplication] canOpenURL:url]) {
            [[UIApplication sharedApplication] openURL:url options:@{} completionHandler:nil];
        }
    } @catch (NSException *exception) {
        // 静默忽略
    }
    decisionHandler(WKNavigationActionPolicyCancel);
}

- (void)webView:(WKWebView *)webView decidePolicyForNavigationResponse:(WKNavigationResponse *)navigationResponse decisionHandler:(void (^)(WKNavigationResponsePolicy))decisionHandler {
    if ([self shouldOpenDownloadURLForNavigationResponse:navigationResponse]) {
        [self openDownloadURLExternally:navigationResponse.response.URL source:@"navigationResponse"];
        decisionHandler(WKNavigationResponsePolicyCancel);
        return;
    }
    decisionHandler(WKNavigationResponsePolicyAllow);
}

#pragma mark - Download

// WKWebView 对附件/不可展示 MIME 没有系统下载 UI，这里保持与旧 Android 一致，外跳系统处理。
- (BOOL)shouldOpenDownloadURLForNavigationAction:(WKNavigationAction *)navigationAction {
    NSURL *url = navigationAction.request.URL;
    if (![self isHTTPOrHTTPSURL:url]) return NO;
    if ([self isDownloadURL:url]) return YES;
    if (@available(iOS 14.5, *)) {
        return navigationAction.shouldPerformDownload;
    }
    return NO;
}

- (BOOL)shouldOpenDownloadURLForNavigationResponse:(WKNavigationResponse *)navigationResponse {
    NSURL *url = navigationResponse.response.URL;
    if (![self isHTTPOrHTTPSURL:url]) return NO;
    if ([self isDownloadURL:url]) return YES;
    if ([navigationResponse.response isKindOfClass:[NSHTTPURLResponse class]]) {
        NSHTTPURLResponse *response = (NSHTTPURLResponse *)navigationResponse.response;
        NSString *contentDisposition = [self headerValueForName:@"Content-Disposition" response:response].lowercaseString;
        if ([contentDisposition containsString:@"attachment"]) return YES;
        NSString *contentType = [self headerValueForName:@"Content-Type" response:response].lowercaseString;
        if ([contentType containsString:@"application/octet-stream"] ||
            [contentType containsString:@"application/vnd.android.package-archive"] ||
            [contentType containsString:@"application/x-msdownload"] ||
            [contentType containsString:@"application/zip"]) {
            return YES;
        }
    }
    return !navigationResponse.canShowMIMEType;
}

- (BOOL)isHTTPOrHTTPSURL:(NSURL *)url {
    NSString *scheme = url.scheme.lowercaseString;
    return [scheme isEqualToString:@"http"] || [scheme isEqualToString:@"https"];
}

- (BOOL)isDownloadURL:(NSURL *)url {
    NSString *path = url.path.lowercaseString;
    if (path.length == 0) return NO;
    for (NSString *extension in QnDownloadURLPathExtensions()) {
        if ([path hasSuffix:extension]) {
            return YES;
        }
    }
    return NO;
}

- (NSString *)headerValueForName:(NSString *)name response:(NSHTTPURLResponse *)response {
    NSDictionary *headers = response.allHeaderFields;
    for (id key in headers) {
        if ([[key description] caseInsensitiveCompare:name] == NSOrderedSame) {
            return [headers[key] description] ?: @"";
        }
    }
    return @"";
}

- (void)openDownloadURLExternally:(NSURL *)url source:(NSString *)source {
    if (![self isHTTPOrHTTPSURL:url]) return;
    NSLog(@"QnWebView open download url source=%@ url=%@", source ?: @"", url.absoluteString ?: @"");
    [[UIApplication sharedApplication] openURL:url options:@{} completionHandler:^(BOOL success) {
        if (!success) {
            NSLog(@"QnWebView open download url failed url=%@", url.absoluteString ?: @"");
        }
    }];
}

- (void)webView:(WKWebView *)webView didStartProvisionalNavigation:(WKNavigation *)navigation {
    if (self.css_onPageStarted) {
        self.css_onPageStarted(@{@"url": webView.URL.absoluteString ?: @""});
    }
}

- (void)webView:(WKWebView *)webView didFinishNavigation:(WKNavigation *)navigation {
    // 页面加载完成后注入 JSBridge 脚本
    [self.jsBridge injectBridgeScript];
    NSLog(@"QnWebView didFinish inject bridge url=%@", webView.URL.absoluteString ?: @"");

    if (self.css_onPageFinished) {
        self.css_onPageFinished(@{@"url": webView.URL.absoluteString ?: @""});
    }
}

- (void)webView:(WKWebView *)webView didFailProvisionalNavigation:(WKNavigation *)navigation
      withError:(NSError *)error {
    if (self.css_onError) {
        self.css_onError(@{
            @"errorCode": @(error.code),
            @"description": error.localizedDescription ?: @"Unknown error"
        });
    }
}

- (void)webView:(WKWebView *)webView didFailNavigation:(WKNavigation *)navigation
      withError:(NSError *)error {
    if (self.css_onError) {
        self.css_onError(@{
            @"errorCode": @(error.code),
            @"description": error.localizedDescription ?: @"Unknown error"
        });
    }
}

#pragma mark - WKUIDelegate

- (void)webView:(WKWebView *)webView
runOpenPanelWithParameters:(WKOpenPanelParameters *)parameters
initiatedByFrame:(WKFrameInfo *)frame
completionHandler:(void (^)(NSArray<NSURL *> * _Nullable URLs))completionHandler {
    UIViewController *vc = [self findViewController];
    if (!vc) {
        NSLog(@"QnWebView open panel failed: viewController is nil");
        completionHandler(nil);
        return;
    }
    if (!self.fileInputCoordinator) {
        self.fileInputCoordinator = [[QnWebViewFileInputCoordinator alloc] init];
    }
    [self.fileInputCoordinator openPanelFromViewController:vc
                                   allowsMultipleSelection:parameters.allowsMultipleSelection
                                         completionHandler:completionHandler];
}

#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 150000
- (void)webView:(WKWebView *)webView
requestMediaCapturePermissionForOrigin:(WKSecurityOrigin *)origin
initiatedByFrame:(WKFrameInfo *)frame
           type:(WKMediaCaptureType)type
decisionHandler:(void (^)(WKPermissionDecision decision))decisionHandler API_AVAILABLE(ios(15.0)) {
    if (![self isTrustedWebMediaOrigin:origin]) {
        NSLog(@"QnWebView deny media permission scene=%d host=%@", self.css_teenResetAuthScene, origin.host ?: @"");
        decisionHandler(WKPermissionDecisionDeny);
        return;
    }
    BOOL needsVideo = type != WKMediaCaptureTypeMicrophone;
    BOOL needsAudio = type != WKMediaCaptureTypeCamera;
    [QnWebViewMediaPermissionCoordinator requestPermissionForVideo:needsVideo
                                                             audio:needsAudio
                                                        completion:^(BOOL granted) {
        decisionHandler(granted ? WKPermissionDecisionGrant : WKPermissionDecisionDeny);
    }];
}
#endif

- (void)webView:(WKWebView *)webView
runJavaScriptAlertPanelWithMessage:(NSString *)message
initiatedByFrame:(WKFrameInfo *)frame
completionHandler:(void (^)(void))completionHandler {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:nil
                                                                  message:message
                                                           preferredStyle:UIAlertControllerStyleAlert];
    [alert addAction:[UIAlertAction actionWithTitle:@"OK" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        completionHandler();
    }]];

    UIViewController *vc = [self findViewController];
    if (vc) {
        [vc presentViewController:alert animated:YES completion:nil];
    } else {
        completionHandler();
    }
}

- (void)webView:(WKWebView *)webView
runJavaScriptConfirmPanelWithMessage:(NSString *)message
initiatedByFrame:(WKFrameInfo *)frame
completionHandler:(void (^)(BOOL))completionHandler {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:nil
                                                                  message:message
                                                           preferredStyle:UIAlertControllerStyleAlert];
    [alert addAction:[UIAlertAction actionWithTitle:@"Cancel" style:UIAlertActionStyleCancel handler:^(UIAlertAction *action) {
        completionHandler(NO);
    }]];
    [alert addAction:[UIAlertAction actionWithTitle:@"OK" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        completionHandler(YES);
    }]];

    UIViewController *vc = [self findViewController];
    if (vc) {
        [vc presentViewController:alert animated:YES completion:nil];
    } else {
        completionHandler(NO);
    }
}

#pragma mark - KVO

- (void)observeValueForKeyPath:(NSString *)keyPath
                      ofObject:(id)object
                        change:(NSDictionary<NSKeyValueChangeKey,id> *)change
                       context:(void *)context {
    if ([keyPath isEqualToString:@"estimatedProgress"]) {
        if (self.css_onProgressChanged) {
            int progress = (int)(self.webView.estimatedProgress * 100);
            self.css_onProgressChanged(@{@"progress": @(progress)});
        }
    } else if ([keyPath isEqualToString:@"title"]) {
        if (self.css_onReceiveTitle) {
            self.css_onReceiveTitle(@{@"title": self.webView.title ?: @""});
        }
    }
}

#pragma mark - Helper

- (BOOL)isTrustedTeenResetAuthOrigin:(WKSecurityOrigin *)origin {
    NSString *host = origin.host.lowercaseString ?: @"";
    return QnHostMatchesTeenResetTrustedHost(host, kTeenResetAuthWebankHost) ||
        QnHostMatchesTeenResetTrustedHost(host, kTeenResetAuthFaceIdHost);
}

- (BOOL)isTrustedDclFeedbackOrigin:(WKSecurityOrigin *)origin {
    NSString *host = origin.host.lowercaseString ?: @"";
    return [host isEqualToString:kDclFeedbackTrustedHost];
}

- (BOOL)isTrustedWebMediaOrigin:(WKSecurityOrigin *)origin {
    return [self isTrustedDclFeedbackOrigin:origin] ||
        (self.css_teenResetAuthScene && [self isTrustedTeenResetAuthOrigin:origin]);
}

- (UIViewController *)findViewController {
    UIResponder *responder = self;
    while (responder) {
        if ([responder isKindOfClass:[UIViewController class]]) {
            return (UIViewController *)responder;
        }
        responder = [responder nextResponder];
    }
    return nil;
}

@end
