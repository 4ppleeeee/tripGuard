#import "ThirdPartyLoginBridge.h"
#import <TencentOpenAPI/TencentOpenApiUmbrellaHeader.h>
#import <WeChatOpenSDK-XCFramework/WXApi.h>
#import "WWKApi.h"
#import <Weibo_SDK/WeiboSDK.h>

@implementation ThirdPartyLoginBridge

+ (void)setupQQWithAppId:(NSString *)appId {
    [TencentOAuth setIsUserAgreedAuthorization:YES];
    TencentOAuth *oauth = [TencentOAuth sharedInstance];
    [oauth setupAppId:appId
    enableUniveralLink:YES
         universalLink:nil
              delegate:nil];
    oauth.authMode = kAuthModeServerSideCode;
}

+ (void)setupWXWithAppId:(NSString *)appId universalLink:(NSString *)universalLink {
    [WXApi registerApp:appId universalLink:universalLink];
}

+ (void)setupWeiboWithAppKey:(NSString *)appKey universalLink:(NSString *)universalLink {
    BOOL result = [WeiboSDK registerApp:appKey universalLink:universalLink];
    NSLog(@"[Startup][Social][iOS] WeiboSDK registered, appKey=%@, universalLink=%@, result=%d",
          appKey, universalLink, result);
}

+ (void)setupWeComWithShareAppId:(NSString *)shareAppId
                          corpId:(NSString *)corpId
                         agentId:(NSString *)agentId {
    BOOL result = [WWKApi registerApp:shareAppId corpId:corpId agentId:agentId];
    NSLog(@"[Startup][Social][iOS] WWKApi registered, shareAppId=%@, corpId=%@, agentId=%@, result=%d",
          shareAppId, corpId, agentId, result);
}

+ (BOOL)handleQQOpenURL:(NSURL *)url {
    [QQApiInterface handleOpenURL:url delegate:nil];
    if ([TencentOAuth CanHandleOpenURL:url]) {
        return [TencentOAuth HandleOpenURL:url];
    }
    return YES;
}

+ (BOOL)handleQQUniversalLink:(NSURL *)url {
    if (![TencentOAuth CanHandleUniversalLink:url]) {
        return NO;
    }
    [QQApiInterface handleOpenUniversallink:url delegate:nil];
    return [TencentOAuth HandleUniversalLink:url];
}

+ (BOOL)handleWXOpenURL:(NSURL *)url {
    return [WXApi handleOpenURL:url delegate:nil];
}

+ (BOOL)handleWXUniversalLink:(NSUserActivity *)userActivity {
    return [WXApi handleOpenUniversalLink:userActivity delegate:nil];
}

+ (BOOL)handleWeiboOpenURL:(NSURL *)url {
    return [WeiboSDK handleOpenURL:url delegate:nil];
}

+ (BOOL)handleWeiboUniversalLink:(NSUserActivity *)userActivity {
    return [WeiboSDK handleOpenUniversalLink:userActivity delegate:nil];
}

+ (BOOL)handleWeComOpenURL:(NSURL *)url {
    return [WWKApi handleOpenURL:url delegate:nil];
}

@end
