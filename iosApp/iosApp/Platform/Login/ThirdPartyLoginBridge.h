#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ThirdPartyLoginBridge : NSObject

+ (void)setupQQWithAppId:(NSString *)appId;
+ (void)setupWXWithAppId:(NSString *)appId universalLink:(NSString *)universalLink;
+ (void)setupWeiboWithAppKey:(NSString *)appKey universalLink:(NSString *)universalLink;
+ (void)setupWeComWithShareAppId:(NSString *)shareAppId
                         corpId:(NSString *)corpId
                        agentId:(NSString *)agentId;

+ (BOOL)handleQQOpenURL:(NSURL *)url;
+ (BOOL)handleQQUniversalLink:(NSURL *)url;
+ (BOOL)handleWXOpenURL:(NSURL *)url;
+ (BOOL)handleWXUniversalLink:(NSUserActivity *)userActivity;
+ (BOOL)handleWeiboOpenURL:(NSURL *)url;
+ (BOOL)handleWeiboUniversalLink:(NSUserActivity *)userActivity;
+ (BOOL)handleWeComOpenURL:(NSURL *)url;

@end

NS_ASSUME_NONNULL_END
