//
//  NSMutableDictionary+SPSafeMDictionary.h
//  SPPlayer
//
//  Created by ethanyxliu on 2019/9/29.
//  Copyright © 2019 tencent. All rights reserved.
//


#import <Foundation/Foundation.h>


@interface NSMutableDictionary<KeyType, ObjectType> (SPSafeMDictionary)

/**
 * 该方法内部会对anObject和aKey判空，如果为空则直接返回
 */
- (void)spSetObject:(ObjectType)anObject forKey:(KeyType <NSCopying>)aKey;

/**
 * 专用于anObject为NSString的情况，如果anObject为空，则会用一个空字符串来代替，其他同spSetObject:forKey
 */
- (void)spSetString:(NSString *)anObject forKey:(KeyType <NSCopying>)aKey;

@end

