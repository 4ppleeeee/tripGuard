//
//  SPSection.h
//  SPPlayer
//
//  Created by liyukuan on 2019/10/3.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 * 播放地址封装，每个section包含一个url List，一般第一个主播放地址，其余的作为备份地址，如果主地址失败，则使用备份地址重试。
 * 对于HLS或整片MP4，一般只有一个section，对于分配MP4，则有多个section
 */
@interface SPSection : NSObject

@property (nonatomic, copy) NSString *url;  // 当前正在使用的url，开始时一般为urlList中的第一个。

@property (nonatomic, strong) NSArray<NSString *> *urlList;

@property (nonatomic, assign) NSTimeInterval duration;

@property (nonatomic, assign) int index;  // 当前正在使用的url的索引

@property (nonatomic, strong) NSArray<NSString *> *vtList;  // cdn id列表，仅用于数据上报，与urlList

@property (nonatomic, assign) int64_t clipSize;  // 分片大小，单位字节

@property (nonatomic, copy) NSString *clipMD5;  // 分片的MD5

@property (nonatomic, copy) NSString *keyID;  // 分片的keyID

@end

NS_ASSUME_NONNULL_END
