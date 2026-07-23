/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : NSDictionary+SafeDictionary.h
 Author      : Snow
 Version     : 1.0
 Date        : 4/28/14
 Description :
 History     : 4/28/14 初始版本
 ***********************************************************/
//

#import <Foundation/Foundation.h>

/**
 * 用法：在原来的使用objectForKey之上重新组合两组更安全的方法
 * SafeModel系列 保证返回的对象是有效对象，不会为空
 * ForKey系列 只判断类型的合法性，无效会返回空
 */
@interface NSDictionary (SPSafeDictionary)

#pragma mark - 合法性判断的基础方法
- (Class)spObjectClassForKey:(id)aKey;

- (id)spObjectForKey:(id)aKey verifyClass:(Class)aClass;

- (BOOL)spFindForKey:(id)aKey;

#pragma mark - SafeModel系列

/**
 * 默认值 @“”, 兼容object是number的情况，会转成对应的string
 *
 * @param aKey key值
 * @return 结果
 */
- (NSString*)spStringForKeySafeModel:(id)aKey;

/**
 * 默认值 [NSNumber numberWithInt:0], 兼容object是string的情况，会按整型来默认转换
 *
 * @param aKey key值
 * @return 结果
 */
- (NSNumber*)spNumberForKeySafeModel:(id)aKey;

/**
 * 默认值 [NSArray array]
 *
 * @param aKey key值
 * @return 结果
 */
- (NSArray*)spArrayForKeySafeModel:(id)aKey;

/**
 * 默认值 [NSDictionary dictionary]
 *
 * @param aKey key值
 * @return 结果
 */
- (NSDictionary*)spDictionaryForKeySafeModel:(id)aKey;

/**
* 默认值 0, 兼容number和string两种情况，会按float来默认转换
*
* @param aKey key值
* @return int64_t值
*/
- (int64_t)spInt64ValueForKeySafeModel:(id)aKey;

/**
* 默认值 0.0, 兼容number和string两种情况，会按float来默认转换
*
* @param aKey key值
* @return float值
*/
- (float)spFloatValueForKeySafeModel:(id)aKey;

/**
 * 默认值 NO, 兼容number和string两种情况，会按整型来默认转换
 *
 * @param aKey key值
 * @return 结果
 */
- (BOOL)spBoolForKeySafeModel:(id)aKey;

#pragma mark - ForKey系列

- (NSArray*)spArrayForKey:(id)aKey;

- (NSDictionary*)spDictionaryForKey:(id)aKey;

- (NSString*)spStringForKey:(id)aKey;

- (NSNumber*)spNumberForKey:(id)aKey;

- (NSData*)spDataForKey:(id)aKey;

- (NSDate*)spDateForKey:(id)aKey;

- (NSURL*)spUrlForKey:(id)aKey;

@end
