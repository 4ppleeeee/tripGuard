/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : NSDictionary+SafeDictionary.m
 Author      : Snow
 Version     : 1.0
 Date        : 4/28/14
 Description :
 History     : 4/28/14 初始版本
 ***********************************************************/

#import "NSDictionary+SPSafeDictionary.h"

@implementation NSDictionary (SPSafeDictionary)

#pragma mark - 合法性判断的基础方法

- (Class)spObjectClassForKey:(id)aKey {
    if (nil == aKey) {
        return nil;
    }
    id resultObject = [self objectForKey:aKey];
    if (nil != resultObject) {
        @try {
            return [resultObject class];
        } @catch (NSException* exception) {
            NSLog(@"NSException info %@", exception);
            return nil;
        }
    }
    return nil;
}

- (id)spObjectForKey:(id)aKey verifyClass:(Class)aClass {
    if (nil == aKey) {
        return nil;
    }
    id resultObject = [self objectForKey:aKey];
    if (nil != resultObject) {
        @try {
            if ([resultObject isKindOfClass:aClass]) {
                return resultObject;
            }
        } @catch (NSException* exception) {
            NSLog(@"NSException info %@", exception);
            return nil;
        }
    }
    return nil;
}

- (BOOL)spFindForKey:(id)aKey {
    if (nil == aKey) {
        return NO;
    }
    id resultObject = [self objectForKey:aKey];
    if (nil == resultObject) {
        return NO;
    }
    return YES;
}

#pragma mark -  SafeModel系列

- (NSArray*)spArrayForKeySafeModel:(id)aKey {
    NSArray* resultObject = [self spArrayForKey:aKey];
    if (nil == resultObject) {
        return [NSArray array];
    }
    return resultObject;
}

- (NSDictionary*)spDictionaryForKeySafeModel:(id)aKey {
    NSDictionary* resultObject = [self spDictionaryForKey:aKey];
    if (nil == resultObject) {
        return [NSDictionary dictionary];
    }
    return resultObject;
}

- (NSString*)spStringForKeySafeModel:(id)aKey {
    NSString* resultObject = [self spStringForKey:aKey];
    if (nil == resultObject) {
        NSNumber* num = [self spNumberForKey:aKey];
        if (nil != num) {
            resultObject = [NSString stringWithFormat:@"%@", num];
            return resultObject;
        }
        return @"";
    }
    return resultObject;
}

- (NSNumber*)spNumberForKeySafeModel:(id)aKey {
    NSNumber* resultObject = [self spNumberForKey:aKey];
    if (nil == resultObject) {
        NSString* str = [self spStringForKey:aKey];
        if (nil != str) {
            NSNumber* num = [NSNumber numberWithInt:[str intValue]];
            return num;
        }
        return [NSNumber numberWithInt:0];
    }
    return resultObject;
}

- (int64_t)spInt64ValueForKeySafeModel:(id)aKey {
    NSNumber* resultObject = [self spNumberForKey:aKey];
    if (nil == resultObject) {
        NSString* str = [self spStringForKey:aKey];
        if (nil != str) {
            return str.longLongValue;
        }
        return 0;
    }

    return resultObject.longLongValue;
}

- (float)spFloatValueForKeySafeModel:(id)aKey {
    NSNumber* resultObject = [self spNumberForKey:aKey];
    if (nil == resultObject) {
        NSString* str = [self spStringForKey:aKey];
        if (nil != str) {
            return str.floatValue;
        }
        return 0.0;
    }

    return resultObject.floatValue;
}

- (BOOL)spBoolForKeySafeModel:(id)aKey {
    NSNumber* num = [self spNumberForKeySafeModel:aKey];
    if (nil != num) {
        return [num boolValue];
    }
    return NO;
}

#pragma mark - ForKey系列

- (NSArray*)spArrayForKey:(id)aKey {
    NSArray* resultObject = [self spObjectForKey:aKey verifyClass:[NSArray class]];
    return resultObject;
}

- (NSDictionary*)spDictionaryForKey:(id)aKey {
    NSDictionary* resultObject = [self spObjectForKey:aKey verifyClass:[NSDictionary class]];
    return resultObject;
}

- (NSString*)spStringForKey:(id)aKey {
    NSString* resultObject = [self spObjectForKey:aKey verifyClass:[NSString class]];
    return resultObject;
}

- (NSNumber*)spNumberForKey:(id)aKey {
    NSNumber* resultObject = [self spObjectForKey:aKey verifyClass:[NSNumber class]];
    return resultObject;
}

- (NSData*)spDataForKey:(id)aKey {
    NSData* resultObject = [self spObjectForKey:aKey verifyClass:[NSData class]];
    return resultObject;
}

- (NSDate*)spDateForKey:(id)aKey {
    NSDate* resultObject = [self spObjectForKey:aKey verifyClass:[NSDate class]];
    return resultObject;
}

- (NSURL*)spUrlForKey:(id)aKey {
    NSURL* resultObject = [self spObjectForKey:aKey verifyClass:[NSURL class]];
    return resultObject;
}

@end
