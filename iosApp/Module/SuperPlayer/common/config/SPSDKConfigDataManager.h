/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : QLConfigDataManager.h
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 14/7/15
 Description : 获取播放相关配置
 History     : 14/7/15 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>
// 简化调用宏，获取配置中指定参数的值，并进行转化成指定的类型
#define SPSDKCONF_INT(property) [[SPSDKConfigDataManager instance] getConfigPropertyInt:property]
#define SPSDKCONF_FLOAT(property) [[SPSDKConfigDataManager instance] getConfigPropertyFloat:property]
#define SPSDKCONF_DOUBLE(property) [[SPSDKConfigDataManager instance] getConfigPropertyDouble:property]
#define SPSDKCONF_BOOL(property) [[SPSDKConfigDataManager instance] getConfigPropertyBool:property]
#define SPSDKCONF_STRING(property) [[SPSDKConfigDataManager instance] getConfigPropertyString:property]
#define SPSDKCONF_OBJECT(property) [[SPSDKConfigDataManager instance] getConfigPropertyObject:property]
#define SPSDKCONF_ARRAY(property) [[SPSDKConfigDataManager instance] getConfigPropertyArray:property]
// 获取指定的语言
#define SP_SDK_LANG_STRING(str) [[SPSDKConfigDataManager instance] getLangString:str]
// 获取key对应的url
#define SP_SDK_CONF_STRING(str) [[SPSDKConfigDataManager instance] getUrlConfString:str]

@interface SPSDKConfigDataManager : NSObject

+ (SPSDKConfigDataManager*)instance;

/**
 更新在线配置
 */
- (void)updateOnlineConfig;

/**
 获取配置中指定key的value值，并转化为int类型的值

 @param propString key
 @return 返回值
 */
- (int)getConfigPropertyInt:(NSString*)propString;

/**
 获取配置中指定key的value值，并转化为float类型的值

 @param propString key
 @return 返回值
 */
- (float)getConfigPropertyFloat:(NSString*)propString;

/**
 获取配置中指定key的value值，并转化为double类型的值

 @param propString key
 @return 返回值
 */
- (double)getConfigPropertyDouble:(NSString*)propString;

/**
 获取配置中指定key的value值，并转化为BOOL类型的值

 @param propString key
 @return 返回值
 */
- (BOOL)getConfigPropertyBool:(NSString*)propString;

/**
 获取配置中指定key的value值，并转化为NSString类型的值

 @param propString key
 @return 返回值
 */
- (NSString*)getConfigPropertyString:(NSString*)propString;

/**
 获取配置中指定key的value值，并转化为NSDictionary类型的值

 @param propString key
 @return 返回值
 */
- (NSDictionary*)getConfigPropertyObject:(NSString*)propString;

/**
 获取配置中指定key的value值，并转化为NSArray类型的值

 @param propString key
 @return 返回值
 */
- (NSArray*)getConfigPropertyArray:(NSString*)propString;

/**
 获取lang配置中指定key的value值，

 @param string key
 @return 返回值
 */
- (NSString*)getLangString:(NSString*)string;

/**
 获取url配置中指定key的value值

 @param serverKey key
 @return 返回值
 */
- (NSString*)getUrlConfString:(NSString*)serverKey;

/**
设置指定key的value值

 @param propString key
 @param object value值
 */
- (void)setConfigObjectForKey:(NSString *)propString
                       object:(id)object;

@end
