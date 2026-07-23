/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPSDKDefaultConfig.m
 Author      : liyukuan
 Version     : 1.0
 Date        : 2017/7/31
 Description : app通过彩蛋设置更改配置，更改播放器执行逻辑
 History     : 2017/7/31 初始版本
 ***********************************************************/

#import "SPSDKDefaultConfig.h"

typedef NS_ENUM(NSUInteger, SPConfigValueType) {
    SPConfigValueTypeBool,
    SPConfigValueTypeInt,
    SPConfigValueTypeString,
};

@interface SPSDKDefaultConfig () {
    NSMutableDictionary *_configDict;
    NSString *_defaultPlistPath;
    NSMutableDictionary<NSString *, NSNumber *> *_customConfigList;
}
@end

@implementation SPSDKDefaultConfig

+ (SPSDKDefaultConfig *)sharedInstance {
    static SPSDKDefaultConfig *s_defaultConfig = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        s_defaultConfig = [[SPSDKDefaultConfig alloc] init];
    });

    return s_defaultConfig;
}

- (instancetype)init {
    if ((self = [super init])) {
        _defaultPlistPath = [self defaultPlistPath];
        [self readDefaultConfig];
        [self createDefaultPlist];
    }

    return self;
}

- (void)readDefaultConfig {
    if ([[NSFileManager defaultManager] fileExistsAtPath:_defaultPlistPath]) {
        _configDict = [[NSMutableDictionary alloc] initWithContentsOfFile:_defaultPlistPath];
    }

    if (_configDict == nil) {
        _configDict = [[NSMutableDictionary alloc] init];
    }
}

- (NSString *)defaultPlistPath {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *docDirectory = [paths objectAtIndex:0];
    NSString *plistPath = [docDirectory stringByAppendingPathComponent:@"sp_default.plist"];
    return plistPath;
}

- (void)createDefaultPlist {
    if (![[NSFileManager defaultManager] fileExistsAtPath:_defaultPlistPath]) {
        [[NSFileManager defaultManager] createFileAtPath:_defaultPlistPath contents:nil attributes:nil];
    }
}

- (void)setBool:(BOOL)value forKey:(NSString *)key {
    [self setObject:@(value) forKey:key];
}

- (void)setInt:(int)value forKey:(NSString *)key {
    [self setObject:@(value) forKey:key];
}

- (void)setObject:(id)obj forKey:(NSString *)key {
    if (!obj || !key) {
        return;
    }

    [_configDict setObject:obj forKey:key];
    [_configDict writeToFile:_defaultPlistPath atomically:YES];
}

- (BOOL)hasValueForKey:(NSString *)key {
    if (!key) {
        return NO;
    }

    return [_configDict objectForKey:key] != nil;
}

- (int)intValueForKey:(NSString *)key {
    id obj = [_configDict objectForKey:key];
    SPLOGI(@"SPDefConfig", @"obj=%@, key=%@", obj, key);
    if ([obj isKindOfClass:[NSNumber class]]) {
        return [(NSNumber *)obj intValue];
    } else if ([obj isKindOfClass:[NSString class]]) {
        return [(NSString *)obj intValue];
    }

    return 0;
}

- (BOOL)boolValueForKey:(NSString *)key {
    id obj = [_configDict objectForKey:key];
    SPLOGI(@"SPDefConfig", @"obj=%@, key=%@", obj, key);
    if ([obj isKindOfClass:[NSNumber class]]) {
        return [(NSNumber *)obj boolValue];
    } else if ([obj isKindOfClass:[NSString class]]) {
        return [(NSString *)obj boolValue];
    }

    return 0;
}

- (NSDictionary *)dictValueForKey:(NSString *)key {
    id obj = [_configDict objectForKey:key];
    if ([obj isKindOfClass:[NSDictionary class]]) {
        return (NSDictionary *)obj;
    }

    return nil;
}

- (NSArray *)arrayValueForKey:(NSString *)key {
    id obj = [_configDict objectForKey:key];
    if ([obj isKindOfClass:[NSArray class]]) {
        return (NSArray *)obj;
    }

    return nil;
}

- (void)removeValueForKey:(NSString *)key {
    if (!key) {
        return;
    }

    [_configDict removeObjectForKey:key];
    [_configDict writeToFile:_defaultPlistPath atomically:YES];
}

- (NSArray<NSString *> *)customKeyList {
    return [_customConfigList allKeys];
}

- (void)setValue:(NSString *)value customKey:(NSString *)key {
    if (key == nil) {
        return;
    }
    SPLOGI(@"SPDefConfig", @"%@:value=%@, key=%@", NSStringFromSelector(_cmd), value, key);
    SPConfigValueType eValueType = [self valueTypeForKey:key];

    switch (eValueType) {
        case SPConfigValueTypeBool: {
            BOOL boolVal = [value boolValue];
            [self setBool:boolVal forKey:key];
        } break;
        case SPConfigValueTypeInt: {
            int iVal = [value intValue];
            [self setInt:iVal forKey:key];
        } break;
        case SPConfigValueTypeString:
            // not impl
            break;
        default:
            break;
    }
}

- (NSString *)valueForCustomKey:(NSString *)key {
    SPConfigValueType eValueType = [self valueTypeForKey:key];
    NSString *value = @"";
    if ([self hasValueForKey:key]) {
        switch (eValueType) {
            case SPConfigValueTypeBool: {
                BOOL boolVal = [self boolValueForKey:key];
                value = [@(boolVal) stringValue];
            } break;
            case SPConfigValueTypeInt: {
                int iVal = [self intValueForKey:key];
                value = [@(iVal) stringValue];
            } break;
            case SPConfigValueTypeString:
                // not impl
                break;
            default:
                break;
        }
    } else {
        switch (eValueType) {
            case SPConfigValueTypeBool: {
                BOOL boolVal = SPSDKCONF_BOOL(key);
                value = [@(boolVal) stringValue];
            } break;
            case SPConfigValueTypeInt: {
                int iVal = SPSDKCONF_INT(key);
                value = [@(iVal) stringValue];
            } break;
            case SPConfigValueTypeString:
                value = SPSDKCONF_STRING(key);
                break;
            default:
                break;
        }
    }

    return value;
}

- (SPConfigValueType)valueTypeForKey:(NSString *)key {
    NSNumber *valType = [_customConfigList objectForKey:key];
    SPConfigValueType eValueType = SPConfigValueTypeInt;
    if (valType != nil) {
        eValueType = (SPConfigValueType)(valType.intValue);
    }

    return eValueType;
}
@end
