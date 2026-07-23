//
//  SPClipInfo.h
//  SPPlayer
//
//  Created by hemanli on 2019/10/1.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface SPClipInfo : NSObject

@property (nonatomic, assign) int index;

@property (nonatomic, assign) NSTimeInterval clipDuration;

@property (nonatomic, assign) int64_t clipSize;

@property (nonatomic, copy) NSString *md5;

@property (nonatomic, copy) NSString *keyID;

@property (nonatomic, copy) NSString *vkey;

@property (nonatomic, copy) NSString *sha;

/**
 * 只有分片MP4+免流的情况会使用到该property，因为免流情况下，getvbkey直接返回每个分片的地址，不需要拼接
 */
@property (nonatomic, copy) NSArray<NSString *> *urlList;

@end

NS_ASSUME_NONNULL_END
