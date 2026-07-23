//
//  SPPlayerMediaSource.h
//  SPPlayer
//
//  Created by 郭力 on 2019/9/27.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPMediaInfo.h"
#import "SPNetVideoInfo.h"
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

//打开播放器资源的类型
typedef NS_ENUM(NSInteger, SPMediaSourceType) {
    SPMediaSourceTypeVid = 0x01,
    SPMediaSourceTypeUrl = 0x02,
    SPMediaSourceTypeAsset = 0x03,
};

@interface SPPlayerMediaSource : NSObject

@property (nonatomic) SPMediaSourceType type;
@property (nonatomic, strong) NSString *url;
@property (nonatomic, strong) NSMutableArray *backupUrls;
@property (nonatomic, strong) NSString *captureUrl;
@property (nonatomic, strong) SPMediaInfo *mediaInfo;
@property (nonatomic, strong) NSMutableDictionary *headers;
@property (nonatomic, assign) int64_t serverTime;

- (BOOL)isValid;

@end

NS_ASSUME_NONNULL_END
