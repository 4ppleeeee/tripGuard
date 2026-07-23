#import "QnWebViewJSBridge.h"

// JSBridge 协议常量 - 与 Kotlin 共享层 JSBridgeProtocol 一致
static NSString *const kBridgeName = @"KuiklyBridge";
static NSString *const kNativeHandlerName_Bridge = @"KuiklyNativeHandler";
static NSString *const kKeyCallId = @"callId";
static NSString *const kKeyMethod = @"method";
static NSString *const kKeyParams = @"params";
static NSString *const kKeyType = @"type";
static NSString *const kTypeCall = @"call";

@interface QnWebViewJSBridge ()

@property (nonatomic, weak) WKWebView *webView;
@property (nonatomic, copy) void (^onMessage)(NSString *message);
@property (nonatomic, strong) NSMutableDictionary<NSString *, QnNativeMethodHandler> *nativeHandlers;

@end

// 预缓存拼接好的完整 Bridge 脚本，避免每次 injectBridgeScript 时拼接字符串
static NSString *_cachedFullBridgeScript = nil;

@implementation QnWebViewJSBridge

+ (NSString *)cachedFullBridgeScript {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        NSString *bridgeJS = @"(function(){if(window.KuiklyBridge)return;var _callId=0;var _callbacks={};var _handlers={};var CALLBACK_TIMEOUT=30000;function cleanupCallback(id){if(_callbacks[id]){_callbacks[id].reject(new Error('Bridge call timeout'));delete _callbacks[id];}}window.KuiklyBridge={callNative:function(method,params){return new Promise(function(resolve,reject){var id='cb_'+(_callId++);_callbacks[id]={resolve:resolve,reject:reject};var timer=setTimeout(function(){cleanupCallback(id);},CALLBACK_TIMEOUT);_callbacks[id]._timer=timer;var message=JSON.stringify({type:'call',callId:id,method:method,params:params||{}});window.KuiklyBridge._postMessage(message);});},_onNativeResponse:function(callId,result,error){var cb=_callbacks[callId];if(cb){if(cb._timer)clearTimeout(cb._timer);if(error){cb.reject(new Error(error));}else{cb.resolve(result);}delete _callbacks[callId];}},_onNativeEvent:function(data){var eventName=data.method||'message';var handler=_handlers[eventName];if(handler){handler(data.params);}},registerHandler:function(name,handler){_handlers[name]=handler;},_postMessage:function(message){}};})();";
        NSString *tencentNewsCompatJS =
            @"(function(){"
            @"if(window.TencentNews&&window.TencentNews.__weseeCompat)return;"
            @"var callbackSeed=1;"
            @"var apiMethods=['openLogin','openAccountBind','getAppInfo','getUserInfo','getDeviceInfo','getNetworkInfo','getAppStoreInstallList','checkAppStatus','downloadApp','downloadApp_gdt','downloadApp_yyb','installApp','launchApp','deleteDownloadApp','deleteGameItem','openAppStore','pauseDownloadApp','installDownloadedApp','appDownloadStatus','getLocation','openUrl','openUrlWithTarget','openWebViewWithType','openWebviewUrl','closeWebView','setTitle','login','changeLoginAccount','getAdMobstr','setTmpObj','getTmpObj','setShareInfo','setShareArticleInfo','showActionMenu','dismissShareDialog','share','setActionBtnStyle','setActionBtn','setStatusBarColor','registerLifecycle','openMiniProgram','openQzoneMiniGame','startYunGame','getApkChannel','quicklyUpdate','quicklyUpdateInstall','checkCalendarAuthority','requestCalendarAuthority','tryAddCalendarEvent','tryRemoveCalendarEvent','getCalendarEvent','getEventIdFromGameId','addAutoDownloadGameEvent','removeAutoDownloadGameEvent','getAutoDownloadGameEvent','addReserveGameEvent','removeReserveGameEvent','getReserveGameEvent','reserveGame','showNativeLoading','hideNativeLoading','showToast','reportBeacon'];"
            @"function isObject(value){return value&&typeof value==='object'&&!Array.isArray(value);}"
            @"function normalizeResult(result){if(!isObject(result)){return{errCode:0,errStr:'',data:result};}var code=typeof result.code==='number'?result.code:(typeof result.errCode!=='undefined'?result.errCode:0);var msg=typeof result.msg==='string'?result.msg:(typeof result.errStr==='string'?result.errStr:'');var data=isObject(result.data)?result.data:{};var output={};Object.keys(data).forEach(function(key){output[key]=data[key];});Object.keys(result).forEach(function(key){if(key!=='code'&&key!=='msg'&&key!=='data'){output[key]=result[key];}});output.errCode=code;output.errStr=msg;return output;}"
            @"function dispatchUrl(url){var root=document.documentElement||document.body;if(!root){setTimeout(function(){dispatchUrl(url);},0);return;}var iframe=document.createElement('iframe');iframe.style.display='none';iframe.src=url;root.appendChild(iframe);setTimeout(function(){if(iframe.parentNode){iframe.parentNode.removeChild(iframe);}},0);}"
            @"function resolveGlobalCallback(callbackName){if(!callbackName||typeof callbackName!=='string')return null;var parts=callbackName.split('.');var target=window;for(var i=0;i<parts.length;i++){target=target&&target[parts[i]];}return typeof target==='function'?target:null;}"
            @"function normalizePayload(params){if(isObject(params))return params;if(typeof params==='string'&&params.length>0){try{var parsed=JSON.parse(params);if(isObject(parsed))return parsed;}catch(ignore){}}return{};}"
            @"function wrapParams(method,params,explicitCallback){var payload=normalizePayload(params);var callback=explicitCallback||payload.onCallback||payload.callback;if(typeof callback==='string'){var copiedPayload={};Object.keys(payload).forEach(function(key){copiedPayload[key]=payload[key];});copiedPayload.callback=callback;copiedPayload.onCallback=callback;copiedPayload.__legacyPositionCallback=true;return copiedPayload;}if(typeof callback!=='function'){return payload;}var callbackName='__wesee_tencent_news_cb_'+method+'_'+(callbackSeed++);window[callbackName]=function(result){try{callback(normalizeResult(result));}finally{try{delete window[callbackName];}catch(ignore){window[callbackName]=undefined;}}};var nextPayload={};Object.keys(payload).forEach(function(key){if(key!=='onCallback'&&key!=='callback'){nextPayload[key]=payload[key];}});nextPayload.callback=callbackName;nextPayload.onCallback=callbackName;return nextPayload;}"
            @"function dispatchBridge(pkg,method,params,callback){if(!pkg||!method)return;var payload=wrapParams(method,params,callback);var url='jsbridge://'+encodeURIComponent(pkg)+'/'+encodeURIComponent(method)+'?p='+encodeURIComponent(JSON.stringify(payload));dispatchUrl(url);}"
            @"function dispatchTencentNews(method,params,callback){if(!method)return;dispatchBridge('TencentNews',method,params,callback);}"
            @"function queryValue(url,key){var queryIndex=url.indexOf('?');if(queryIndex<0)return'';var pairs=url.substring(queryIndex+1).split('&');for(var i=0;i<pairs.length;i++){var pair=pairs[i];var equalIndex=pair.indexOf('=');var name=equalIndex>=0?pair.substring(0,equalIndex):pair;if(decodeURIComponent(name)===key){var value=equalIndex>=0?pair.substring(equalIndex+1):'';return decodeURIComponent(value.replace(/\\+/g,' '));}}return'';}"
            @"function dispatchLegacyUrl(url){var raw=queryValue(url,'json');if(!raw)return false;try{var request=JSON.parse(raw);var method=request.method||'';var payload={};if(request.args&&request.args.length>0){try{payload=JSON.parse(request.args[0]);}catch(ignore){payload={};}}if(request.callbackId){payload.callback=request.callbackId;payload.onCallback=request.callbackId;}dispatchTencentNews(method,payload);return true;}catch(ignore){return false;}}"
            @"function invoke(method,params,callback){dispatchTencentNews(method,params,callback);}"
            @"function invokeMqq(pkg,method,params,callback){dispatchBridge(pkg,method,params,callback);}"
            @"var tencentNews=window.TencentNews||{};tencentNews.invoke=invoke;tencentNews.callback=function(callbackId,result){var callback=window[callbackId];if(typeof callback==='function'){callback(result);}};tencentNews.removeCallback=function(callbackId){try{delete window[callbackId];}catch(ignore){window[callbackId]=undefined;}};"
            @"apiMethods.forEach(function(method){if(!tencentNews[method]){tencentNews[method]=function(params,callback){invoke(method,params,callback);};}});"
            @"tencentNews.injectionComplete=true;tencentNews.__weseeCompat=true;window.TencentNews=tencentNews;"
            @"window.TencentNewsJsBridge=window.TencentNewsJsBridge||{};if(typeof window.TencentNewsJsBridge.bridgeCall!=='function'){window.TencentNewsJsBridge.bridgeCall=function(url){if(typeof url==='string'&&url.indexOf('jsbridge://')===0){if(url.indexOf('jsbridge://get_with_json_data')===0){dispatchLegacyUrl(url);}else{dispatchUrl(url);}}return JSON.stringify({code:200,result:''});};}"
            @"window.TencentNewsScript=window.TencentNewsScript||tencentNews;window.TencentNewsScript.invoke=invoke;window.TencentNewsScript.injectionComplete=true;"
            @"var mqq=window.mqq||{};if(typeof mqq.invoke!=='function'){mqq.invoke=invokeMqq;}if(typeof mqq.execGlobalCallback!=='function'){mqq.execGlobalCallback=function(callbackName){var callback=resolveGlobalCallback(callbackName);if(callback){callback.apply(window,Array.prototype.slice.call(arguments,1));}};}mqq.version=mqq.version||20140616002;window.mqq=mqq;"
            @"try{var event=document.createEvent('Event');event.initEvent('TencentNewsJSInjectionComplete',false,false);document.dispatchEvent(event);}catch(ignore){}"
            @"if(typeof window.TencentNewsJsReady==='function'){window.TencentNewsJsReady();}"
            @"})();";
        NSString *bonbonIOSCompatJS =
            @"(function(){"
            @"if(window.__weseeBonBonIOSDownloadCompat)return;window.__weseeBonBonIOSDownloadCompat=true;"
            @"function isTarget(){try{return /\\/vise\\/bonbon\\//.test(location.pathname)&&/[?&]pageType=gameHallDetail(?:&|$)/.test(location.search)&&/(iPhone|iPad|iPod)/i.test(navigator.userAgent);}catch(e){return false;}}"
            @"if(!isTarget())return;"
            @"function query(name){var reg=new RegExp('(?:^|&)'+name+'=([^&]*)');var hit=location.search.replace(/^\\?/,'').match(reg);return hit?decodeURIComponent(hit[1]):'';}"
            @"function findGame(value,gameId,depth,seen){if(!value||depth>8)return null;if(typeof value!=='object')return null;if(seen.indexOf(value)>=0)return null;seen.push(value);if(String(value.gameId||'')===String(gameId||'')&&value.downloadInfo){return value;}if(Array.isArray(value)){for(var i=0;i<value.length;i++){var item=findGame(value[i],gameId,depth+1,seen);if(item)return item;}}else{var keys=Object.keys(value);for(var j=0;j<keys.length;j++){var found=findGame(value[keys[j]],gameId,depth+1,seen);if(found)return found;}}return null;}"
            @"function getGame(){var state=window.Vise&&window.Vise.initState;return findGame(state,query('gameId'),0,[]);}"
            @"function showToast(text){if(window.TencentNews&&typeof window.TencentNews.showToast==='function'){window.TencentNews.showToast({text:text});}else{try{window.alert(text);}catch(ignore){}}}"
            @"function openIOSGame(){var game=getGame()||{};var info=game.downloadInfo||{};var iosUrl=info.iosUrl||'';if(!iosUrl){showToast('iOS版本敬请期待');return;}var params={url:iosUrl,iosUrl:iosUrl,sceneID:1001,packageID:info.bundleId||'',gameId:String(game.gameId||query('gameId')||''),reportGameAppID:info.qqAppId||'',pageType:'gameHallDetail',scheme:game.scheme||''};if(window.TencentNews&&typeof window.TencentNews.openAppStore==='function'){window.TencentNews.openAppStore(params);}else{location.href=iosUrl;}}"
            @"function patchText(){var nodes=document.querySelectorAll('.bottom-info-container .reserve-or-download-button .progress-area');for(var i=0;i<nodes.length;i++){var node=nodes[i];var text=(node.textContent||'').trim();if(!text){node.textContent='打开游戏';}}}"
            @"document.addEventListener('click',function(event){var target=event.target;var button=target&&target.closest&&target.closest('.bottom-info-container .reserve-or-download-button .single-button');if(!button)return;var text=(button.textContent||'').trim();if(/预约|取消|暂停/.test(text))return;event.preventDefault();event.stopPropagation();event.stopImmediatePropagation();openIOSGame();},true);"
            @"var count=0;var timer=setInterval(function(){patchText();count++;if(count>40){clearInterval(timer);}},250);"
            @"if(document.readyState==='loading'){document.addEventListener('DOMContentLoaded',patchText);}else{patchText();}"
            @"})();";
        NSString *iosPostMessageJS = @"(function(){window.KuiklyBridge._postMessage=function(message){window.webkit.messageHandlers.KuiklyNativeHandler.postMessage(message);};})();";
        _cachedFullBridgeScript = [NSString stringWithFormat:@"%@%@%@%@", bridgeJS, tencentNewsCompatJS, bonbonIOSCompatJS, iosPostMessageJS];
    });
    return _cachedFullBridgeScript;
}

- (instancetype)initWithWebView:(WKWebView *)webView
                      onMessage:(void (^)(NSString *))onMessage {
    self = [super init];
    if (self) {
        _webView = webView;
        _onMessage = [onMessage copy];
        _nativeHandlers = [NSMutableDictionary dictionary];
    }
    return self;
}

#pragma mark - Public

- (void)registerNativeHandler:(NSString *)name handler:(QnNativeMethodHandler)handler {
    self.nativeHandlers[name] = [handler copy];
}

- (void)injectBridgeScript {
    // 使用预缓存的完整脚本，避免每次页面加载时拼接字符串
    [self.webView evaluateJavaScript:[QnWebViewJSBridge cachedFullBridgeScript] completionHandler:nil];
}

- (void)sendEventToJS:(NSString *)method params:(NSString *)params {
    NSString *escapedMethod = [self escapeJSString:method];
    NSString *script = [NSString stringWithFormat:
                        @"window.KuiklyBridge._onNativeEvent({method:'%@',params:%@});",
                        escapedMethod, params];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.webView evaluateJavaScript:script completionHandler:nil];
    });
}

- (void)dispose {
    [self.nativeHandlers removeAllObjects];
    self.onMessage = nil;
}

#pragma mark - WKScriptMessageHandler

- (void)userContentController:(WKUserContentController *)userContentController
      didReceiveScriptMessage:(WKScriptMessage *)message {
    if (![message.name isEqualToString:kNativeHandlerName_Bridge]) {
        return;
    }

    NSString *messageBody = nil;
    if ([message.body isKindOfClass:[NSString class]]) {
        messageBody = message.body;
    } else {
        return;
    }

    [self handleMessage:messageBody];
}

#pragma mark - Private

- (void)handleMessage:(NSString *)message {
    NSError *error = nil;
    NSData *data = [message dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:data options:0 error:&error];

    if (error || !json) {
        if (self.onMessage) {
            self.onMessage(message);
        }
        return;
    }

    NSString *type = json[kKeyType];
    if ([type isEqualToString:kTypeCall]) {
        [self handleNativeCall:json];
    } else {
        if (self.onMessage) {
            self.onMessage(message);
        }
    }
}

- (void)handleNativeCall:(NSDictionary *)json {
    NSString *callId = json[kKeyCallId] ?: @"";
    NSString *method = json[kKeyMethod] ?: @"";
    NSDictionary *params = json[kKeyParams] ?: @{};

    QnNativeMethodHandler handler = self.nativeHandlers[method];
    if (handler) {
        __weak typeof(self) weakSelf = self;
        QnBridgeResponseCallback callback = ^(NSString *result, NSString *error) {
            __strong typeof(weakSelf) strongSelf = weakSelf;
            if (!strongSelf) return;

            NSString *script = [strongSelf buildResponseScript:callId result:result error:error];
            dispatch_async(dispatch_get_main_queue(), ^{
                [strongSelf.webView evaluateJavaScript:script completionHandler:nil];
            });
        };

        @try {
            handler(params, callback);
        } @catch (NSException *exception) {
            NSString *script = [self buildResponseScript:callId result:nil error:exception.reason];
            [self.webView evaluateJavaScript:script completionHandler:nil];
        }
    } else {
        NSString *errorMsg = [NSString stringWithFormat:@"Method '%@' not registered", method];
        NSString *script = [self buildResponseScript:callId result:nil error:errorMsg];
        [self.webView evaluateJavaScript:script completionHandler:nil];
    }
}

- (NSString *)buildResponseScript:(NSString *)callId
                           result:(NSString *)result
                            error:(NSString *)error {
    NSString *resultStr = result ?: @"null";
    NSString *errorStr = error ? [NSString stringWithFormat:@"'%@'", [self escapeJSString:error]] : @"null";
    return [NSString stringWithFormat:
            @"window.KuiklyBridge._onNativeResponse('%@',%@,%@);",
            [self escapeJSString:callId], resultStr, errorStr];
}

/**
 * 单次遍历转义 JS 字符串（O(n) 复杂度）
 * 替代 10 次 stringByReplacingOccurrencesOfString 链式调用，避免产生 10 个中间 NSString 对象
 */
- (NSString *)escapeJSString:(NSString *)str {
    NSUInteger len = str.length;
    if (len == 0) return str;
    
    NSMutableString *result = [NSMutableString stringWithCapacity:len + (len >> 3) + 16];
    unichar buffer[1024];
    NSUInteger offset = 0;
    
    while (offset < len) {
        NSUInteger chunkLen = MIN(sizeof(buffer) / sizeof(unichar), len - offset);
        [str getCharacters:buffer range:NSMakeRange(offset, chunkLen)];
        
        for (NSUInteger i = 0; i < chunkLen; i++) {
            unichar c = buffer[i];
            switch (c) {
                case '\\': [result appendString:@"\\\\"]; break;
                case '\'': [result appendString:@"\\'"]; break;
                case '"':  [result appendString:@"\\\""]; break;
                case '\n': [result appendString:@"\\n"]; break;
                case '\r': [result appendString:@"\\r"]; break;
                case '\t': [result appendString:@"\\t"]; break;
                case 0x2028: [result appendString:@"\\u2028"]; break;
                case 0x2029: [result appendString:@"\\u2029"]; break;
                case 0x0000: [result appendString:@"\\0"]; break;
                case '<': {
                    // 检测 </script>（不区分大小写）
                    NSUInteger absPos = offset + i;
                    if (absPos + 8 < len) {
                        NSRange range = NSMakeRange(absPos, 9);
                        NSString *substr = [str substringWithRange:range];
                        if ([substr caseInsensitiveCompare:@"</script>"] == NSOrderedSame) {
                            [result appendString:@"<\\/script>"];
                            i += 8; // 跳过 "/script>" 共 8 个字符
                            break;
                        }
                    }
                    [result appendFormat:@"%C", c];
                    break;
                }
                default:
                    [result appendFormat:@"%C", c];
                    break;
            }
        }
        offset += chunkLen;
    }
    return [result copy];
}

@end
