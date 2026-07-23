//
//  NSMutableDictionary+SPSafeMDictionary.m
//  SPPlayer
//
//  Created by ethanyxliu on 2019/9/29.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "NSMutableDictionary+SPSafeMDictionary.h"

@implementation NSMutableDictionary (SPSafeMDictionary)

- (void)spSetObject:(id)anObject forKey:(id)aKey {
    if (anObject == nil || aKey == nil) {
        return;
    }

    [self setObject:anObject forKey:aKey];
}

- (void)spSetString:(NSString *)anObject forKey:(id)aKey {
    NSString *newObj = anObject;
    if (newObj == nil) {
        newObj = @"";
    }

    [self spSetObject:newObj forKey:aKey];
}

@end
