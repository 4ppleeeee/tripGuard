/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPPlayerErrorCode.m
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 17/3/27
 Description :
 History     : 17/3/27 初始版本
 ***********************************************************/

#import "SPPlayerErrorCode.h"
#import <AVFoundation/AVFoundation.h>
// static const int SP_HTTP_Response_Cancel        = -999; // URLSession等网络请求工具的错误码前缀,所有URLSession的错误
static const int SPHTTP_Response_End = -80000;  // URLSession等网络请求工具的错误码前缀,所有URLSession的错误

@implementation SPPlayerErrorCode

+ (NSInteger)convertServerErrorCode:(NSInteger)code {
  NSString *codeStr = [NSString
      stringWithFormat:@"%lu%ld", (unsigned long)SPErrorCategoryServer,
                       (long)code];
  return codeStr.integerValue;
}

+ (NSInteger)convertNetworkErrorCode:(NSInteger)code {
    if (code < SPHTTP_Response_End) {
        NSInteger newCode = -1 * (code - SPHTTP_Response_End);
        return newCode + 140000;
    } else {
        return labs(code) + 140000;
    }
}

+ (NSInteger)convertSystemPlayerErrorCode:(NSInteger)code {
  NSString *codeStr = [NSString
      stringWithFormat:@"%lu%ld", (unsigned long)SPErrorCategoryAVPlayer,
                       (long)code];
  return codeStr.integerValue;
}

+ (NSInteger)convertLogicErrorCode:(NSInteger)code {
  NSString *codeStr =
      [NSString stringWithFormat:@"%lu%ld", (unsigned long)SPErrorCategoryLogic,
                                 (long)code];
  return codeStr.integerValue;
}

+ (NSInteger)convertDeviceCaptureErrorCode:(NSInteger)code {
  NSString *codeStr = [NSString
      stringWithFormat:@"%lu%ld", (unsigned long)SPErrorCategoryDeviceCapture,
                       (long)code];
  return codeStr.integerValue;
}
+ (NSInteger)convertEncoderErrorCode:(NSInteger)code {
  NSString *codeStr = [NSString
      stringWithFormat:@"%lu%ld", (unsigned long)SPErrorCategoryEncoder,
                       (long)code];
  return codeStr.integerValue;
}

+ (NSInteger)convertAVErrorcode:(NSInteger)code {
    NSInteger newErrorCode = code;
    if (code < AVErrorUnknown) {
        newErrorCode = -1 * code;
    }
    return newErrorCode;
}

+ (NSInteger)convertWidgetErrorcode:(NSInteger)code {
  NSString *codeStr = [NSString
      stringWithFormat:@"%lu%ld", (unsigned long)SPErrorCategoryWidget,
                       (long)code];
  return codeStr.integerValue;
}

+ (NSString *)fullErrorCodeStrWithModule:(SPModule)module errorCode:(NSInteger)errorCode {
  return [NSString stringWithFormat:@"%d%lu.%ld", [self platformCode], module,
                                    (long)errorCode];
}

+ (NSString *)fullErrorCodeStrWithModule:(SPModule)module detailErrCodeStr:(NSString *)errCodeStr {
    return [NSString stringWithFormat:@"%d%lu.%@", [self platformCode], module, errCodeStr];
}

+ (NSString *)fullErrorCodeStrWithModule:(SPModule)module errorCode:(NSInteger)errorCode exCode:(NSInteger)exCode {
  return [NSString stringWithFormat:@"%d%lu.%ld.%ld", [self platformCode],
                                    module, (long)errorCode, (long)exCode];
}

+ (NSString *)stringOfPlatformAndModule:(SPModule)module {
    return [NSString stringWithFormat:@"%d%lu", [self platformCode], module];
}

+ (NSError *)buildFullErrorCodeWithError:(NSError *)error {
    NSMutableDictionary *userInfo = [[NSMutableDictionary alloc] init];
    if (error.userInfo.count) {
        [userInfo addEntriesFromDictionary:error.userInfo];
    }
    
    NSString *errorCodeStr = [userInfo objectForKey:SP_FULL_ERROR_CODE_STR_KEY];
    if (errorCodeStr.length > 0) {
        NSString *fullErrorCodeStr = [NSString stringWithFormat:@"%d%@", [self platformCode], errorCodeStr];
        [userInfo setObject:fullErrorCodeStr forKey:SP_FULL_ERROR_CODE_STR_KEY];
    }
    NSString *module = [userInfo objectForKey:SP_ERROR_MODEL_ID];
    if (module.length > 0) {
        NSString *fullModule = [NSString stringWithFormat:@"%d%@", [self platformCode], module];
        [userInfo setObject:fullModule forKey:SP_ERROR_MODEL_ID];
    }
    
    NSError *newError = [NSError errorWithDomain:error.domain code:error.code userInfo:userInfo];
    return newError;
}

+ (NSError *)rebuildError:(NSError *)error errorCode:(NSInteger)errorCode module:(SPModule)module {
    NSMutableDictionary *userInfo = [[NSMutableDictionary alloc] init];
    if (error.userInfo.count) {
        [userInfo addEntriesFromDictionary:error.userInfo];
    }

    NSString *fullErrorCodeStr = [self fullErrorCodeStrWithModule:module errorCode:errorCode];
    [userInfo setObject:fullErrorCodeStr forKey:SP_FULL_ERROR_CODE_STR_KEY];
    [userInfo setObject:[self stringOfPlatformAndModule:module] forKey:SP_ERROR_MODEL_ID];
    [userInfo setObject:error.localizedDescription forKey:SP_ERROR_MESSAGE_KEY];
    NSError *newError = [NSError errorWithDomain:error.domain code:errorCode userInfo:userInfo];
    return newError;
}

+ (NSError *)rebuildNetWorkError:(NSError *)error module:(SPModule)module {
    NSInteger errorCode = [self convertNetworkErrorCode:error.code];
    return [self rebuildError:error errorCode:errorCode module:module];
}

+ (NSError *)buildErrorWithErrorCode:(NSInteger)errorCode
                              errMsg:(NSString *)errMsg
                            userInfo:(NSDictionary *)userInfo
                              module:(SPModule)module
                              domain:(NSString *)domain {
    NSMutableDictionary *newUserInfo = [[NSMutableDictionary alloc] init];
    if (userInfo.count) {
        [newUserInfo addEntriesFromDictionary:userInfo];
    }

    NSNumber *exErrNum = [newUserInfo objectForKey:SP_EX_ERROR_CODE_KEY];
    NSInteger exErrCode = [exErrNum integerValue];
    NSString *fullErrorCodeStr;
    if (exErrCode != 0) {
        fullErrorCodeStr = [self fullErrorCodeStrWithModule:module errorCode:errorCode exCode:exErrCode];
    } else {
        fullErrorCodeStr = [self fullErrorCodeStrWithModule:module errorCode:errorCode];
    }
    [newUserInfo setObject:fullErrorCodeStr forKey:SP_FULL_ERROR_CODE_STR_KEY];
    [newUserInfo setObject:[self stringOfPlatformAndModule:module] forKey:SP_ERROR_MODEL_ID];
    if (errMsg) {
        [newUserInfo setObject:errMsg forKey:SP_ERROR_MESSAGE_KEY];
    }

    NSError *newError = [NSError errorWithDomain:domain code:errorCode userInfo:newUserInfo];
    return newError;
}

+ (NSError *)buildErrorWithErrorCode:(NSInteger)errorCode
                              errMsg:(NSString *)errMsg
                              module:(SPModule)module
                              domain:(NSString *)domain {
    return [self buildErrorWithErrorCode:errorCode
                                  errMsg:errMsg
                                  module:module
                                  domain:domain
                                    data:nil];
}

+ (NSError *)buildErrorWithErrorCode:(NSInteger)errorCode
                              errMsg:(NSString *)errMsg
                              module:(SPModule)module
                              domain:(NSString *)domain
                                data:(id)data {
    NSMutableDictionary *userInfo = [[NSMutableDictionary alloc] init];

    NSString *fullErrorCodeStr = [self fullErrorCodeStrWithModule:module errorCode:errorCode];
    [userInfo setObject:fullErrorCodeStr forKey:SP_FULL_ERROR_CODE_STR_KEY];
    [userInfo setObject:[self stringOfPlatformAndModule:module] forKey:SP_ERROR_MODEL_ID];
    if (errMsg) {
        [userInfo setObject:errMsg forKey:SP_ERROR_MESSAGE_KEY];
    }
    if (data) {
        [userInfo setObject:data forKey:SP_EX_ERROR_DATA_KEY];
    }

    NSError *newError = [NSError errorWithDomain:domain code:errorCode userInfo:userInfo];
    return newError;
}

+ (NSError *)buildErrorWithDetailErrorCodeStr:(NSString *)errCodeStr
                                       errMsg:(NSString *)errMsg
                                       module:(SPModule)module
                                       domain:(NSString *)domain {
    NSMutableDictionary *userInfo = [[NSMutableDictionary alloc] init];

    NSString *fullErrorCodeStr = [self fullErrorCodeStrWithModule:module detailErrCodeStr:errCodeStr];
    NSInteger errorCode = errCodeStr.integerValue;
    NSInteger exCode = 0;
    NSRange range = [errCodeStr rangeOfString:@"."];
    if (range.location != NSNotFound) {
        NSRange range1 = NSMakeRange(0, range.location);
        NSString *subStr = [errCodeStr substringWithRange:range1];
        errorCode = subStr.integerValue;
        if (range.location + 1 < errCodeStr.length) {
            subStr = [errCodeStr substringFromIndex:range.location + 1];
            exCode = subStr.integerValue;
            [userInfo setObject:@(exCode) forKey:SP_EX_ERROR_CODE_KEY];
        }
    }

    [userInfo setObject:fullErrorCodeStr forKey:SP_FULL_ERROR_CODE_STR_KEY];
    [userInfo setObject:[self stringOfPlatformAndModule:module] forKey:SP_ERROR_MODEL_ID];
    if (errMsg) {
        [userInfo setObject:errMsg forKey:SP_ERROR_MESSAGE_KEY];
    }

    NSError *newError = [NSError errorWithDomain:domain code:errorCode userInfo:userInfo];
    return newError;
}

+ (NSError *)buildErrorWithP2pDetailErrorCodeStr:(NSString *)errCodeStr errMsg:(NSString *)errMsg {
    NSInteger errorCode = errCodeStr.integerValue;
    NSInteger exCode = 0;
    NSInteger modelId = 0;
    NSRange modelIdRange = [errCodeStr rangeOfString:@";"];

    if (modelIdRange.location != NSNotFound) {
        NSRange range1 = NSMakeRange(0, modelIdRange.location);
        NSString *modelIdStr = [errCodeStr substringWithRange:range1];
        modelId = modelIdStr.integerValue;

        NSRange errorCodeRange = [errCodeStr rangeOfString:@"."];
        if (errorCodeRange.location != NSNotFound && errorCodeRange.location > (modelIdRange.location + 1)) {
            NSRange range2 = NSMakeRange(modelIdRange.location + 1, (errorCodeRange.location - modelIdRange.location - 1));
            NSString *errorCodeStr = [errCodeStr substringWithRange:range2];
            errorCode = errorCodeStr.integerValue;

            if ((errorCodeRange.location + 1) < errCodeStr.length) {  //有额外细分错误码
                NSString *exCodeStr = [errCodeStr substringFromIndex:errorCodeRange.location + 1];
                exCode = exCodeStr.integerValue;
            }
        } else if ((modelIdRange.location + 1) < errCodeStr.length) {
            NSString *errorCodeStr = [errCodeStr substringFromIndex:modelIdRange.location + 1];
            errorCode = errorCodeStr.integerValue;
        }
    }

    return [self buildErrorWithErrorCode:errorCode errMsg:SPSafeString(errMsg) exErrCode:exCode exErrMsg:@"" module:modelId domain:@"p2p"];
}

+ (NSError *)buildErrorWithErrorCode:(NSInteger)errorCode
                              errMsg:(NSString *)errMsg
                           exErrCode:(NSInteger)exErrCode
                            exErrMsg:(NSString *)exErrMsg
                              module:(SPModule)module
                              domain:(NSString *)domain {
    NSMutableDictionary *userInfo = [[NSMutableDictionary alloc] init];

    NSString *fullErrorCodeStr = [self fullErrorCodeStrWithModule:module errorCode:errorCode exCode:exErrCode];
    [userInfo setObject:fullErrorCodeStr forKey:SP_FULL_ERROR_CODE_STR_KEY];
    [userInfo setObject:[self stringOfPlatformAndModule:module] forKey:SP_ERROR_MODEL_ID];
    [userInfo setObject:errMsg forKey:SP_ERROR_MESSAGE_KEY];
    [userInfo setObject:@(exErrCode) forKey:SP_EX_ERROR_CODE_KEY];
    [userInfo setObject:exErrMsg forKey:SP_EX_ERROR_MSG_KEY];

    NSError *newError = [NSError errorWithDomain:domain code:errorCode userInfo:userInfo];
    return newError;
}

+ (BOOL)isPlayerSpecialErrorCode:(int)errorCode {
//    NSArray *specialErrorList = SPSDKCONF_player_special_error_list;
    NSArray *specialErrorList = @[@(11800), @(11839)];
    if (![specialErrorList isKindOfClass:[NSArray class]]) {
        SPLOGS(SP_CGI_LOG_FILTER, @"not array");
        return NO;
    }

    BOOL hitted = NO;
    for (id elem in specialErrorList) {
        if (![elem isKindOfClass:[NSNumber class]]) {
            continue;
        }

        if ([(NSNumber *)elem intValue] == errorCode) {
            hitted = YES;
            break;
        }
    }

    return hitted;
}

+ (int)platformCode {
    UIUserInterfaceIdiom idiom = [[UIDevice currentDevice] userInterfaceIdiom];
    int platform = 0;
    switch (idiom) {
        case UIUserInterfaceIdiomPhone:
            platform = SPErrorIPhonePlatformCode;
            break;
        case UIUserInterfaceIdiomPad:
            platform = SPErrorIPadPlatformCode;
            break;
        case UIUserInterfaceIdiomTV:
            platform = SPTVPlatformCode;
            break;
        default:
            break;
    }

    return platform;
}
@end
